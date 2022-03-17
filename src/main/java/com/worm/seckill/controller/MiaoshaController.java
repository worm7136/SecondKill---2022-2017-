package com.worm.seckill.controller;

import VO.GoodsVO;
import com.worm.seckill.access.AccessLimit;
import com.worm.seckill.entity.MiaoshaOrder;
import com.worm.seckill.entity.SeckillUser;
import com.worm.seckill.rabbitmq.MQSender;
import com.worm.seckill.rabbitmq.MiaoshaMessage;
import com.worm.seckill.redis.GoodsKey;
import com.worm.seckill.redis.MiaoshaKey;
import com.worm.seckill.redis.OrderKey;
import com.worm.seckill.redis.RedisService;
import com.worm.seckill.result.CodeMsg;
import com.worm.seckill.result.Result;
import com.worm.seckill.service.GoodsService;
import com.worm.seckill.service.MiaoshaService;
import com.worm.seckill.service.OrderService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender sender;

    //内存标记商品是否已经被秒过
    private HashMap<Long, Boolean> localOverMap = new HashMap<>();

    //系统初始化
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVO> goodsList = goodsService.goodsVOList();
        if(goodsList == null) {
            return;
        }
        for(GoodsVO goods : goodsList) {
            redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), goods.getStockCount());
            localOverMap.put(goods.getId(), false);
        }
    }

    //库存重置
    @RequestMapping(value="/reset", method=RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset(Model model) {
        List<GoodsVO> goodsList = goodsService.goodsVOList();
        for(GoodsVO goods : goodsList) {
            goods.setStockCount(10);
            redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), 10);
            localOverMap.put(goods.getId(), false);
        }
        redisService.delete(OrderKey.getMiaoshaOrderByUidGid);
        redisService.delete(MiaoshaKey.isGoodsOver);
        miaoshaService.reset(goodsList);
        return Result.success(true);
    }

    /*
    压测：2000*10
    QPS：
    第五章优化后：630
    第六章优化后：2602
     */
    @RequestMapping(value="/{path}/do_miaosha", method= RequestMethod.POST)
    @ResponseBody
    public Result<Integer> miaosha(Model model, SeckillUser user,
                                   @RequestParam("goodsId") long goodsId,
                                   @PathVariable String path) {
        model.addAttribute("user", user);
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //验证path
        boolean check = miaoshaService.checkPath(user, goodsId, path);
        if(!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        //内存标记，减少redis访问
        boolean over = localOverMap.get(goodsId);
        if (over){ //秒杀已结束
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //预减库存
        long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, ""+goodsId);
        if (stock < 0){ //库存不足
            localOverMap.put(goodsId, true); //内存标记
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null){ //订单不为空，重复秒杀
            return Result.error(CodeMsg.REPEAT_MIAOSHA);
        }
        //请求加入消息队列
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setUser(user);
        mm.setGoodsId(goodsId);
        sender.sendMiaoshaMessage(mm);
        return Result.success(0); //代表排队中状态

        //之前的优化方案，会多次访问redis数据库，现存的方案优化：减少redis的访问
//        //判断库存
//        GoodsVO goods = goodsService.getGoodsVOByGoodsId(goodsId);//10个商品，req1 req2
//        int stock = goods.getStockCount();
//        if(stock <= 0) {
//            return Result.error(CodeMsg.MIAO_SHA_OVER);
//        }
//        //判断是否已经秒杀到了（优化过：查的是缓存而非数据库
//        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
//        if(order != null) {
//            return Result.error(CodeMsg.REPEAT_MIAOSHA);
//        }
//        //减库存 下订单 写入秒杀订单
//        OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
//        return Result.success(orderInfo);
    }

    /**
     * 获取秒杀path
     * @param request
     * @param user
     * @param goodsId
     * @param verifyCode
     * @return
     */
    @AccessLimit(seconds=5, maxCount=5, needLogin=true)
    @RequestMapping(value="/path", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(HttpServletRequest request, SeckillUser user,
                                         @RequestParam("goodsId")long goodsId,
                                         @RequestParam(value="verifyCode", defaultValue="0")int verifyCode
    ) {
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        boolean check = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
        if(!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        String path  =miaoshaService.createMiaoshaPath(user, goodsId);
        return Result.success(path);
    }

    /**
     * 获取验证码
     * @param response
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value="/verifyCode", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCode(HttpServletResponse response, SeckillUser user,
                                              @RequestParam("goodsId")long goodsId) {
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        try {
            BufferedImage image  = miaoshaService.createVerifyCode(user, goodsId);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        }catch(Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }

    /**
     * 秒杀结果
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     * */
    @RequestMapping(value="/result", method=RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(Model model,SeckillUser user,
                                      @RequestParam("goodsId")long goodsId) {
        model.addAttribute("user", user);
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result = miaoshaService.getMiaoshaResult(user.getId(), goodsId);
        return Result.success(result);
    }

}
