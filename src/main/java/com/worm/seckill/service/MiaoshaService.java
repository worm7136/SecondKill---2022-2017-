package com.worm.seckill.service;

import VO.GoodsVO;
import com.worm.seckill.entity.MiaoshaOrder;
import com.worm.seckill.entity.OrderInfo;
import com.worm.seckill.entity.SeckillUser;
import com.worm.seckill.redis.MiaoshaKey;
import com.worm.seckill.redis.RedisService;
import com.worm.seckill.result.CodeMsg;
import com.worm.seckill.result.Result;
import com.worm.seckill.util.MD5Util;
import com.worm.seckill.util.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

@Service
public class MiaoshaService {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;

    @Autowired
    MiaoshaService miaoshaService;

    public OrderInfo miaosha(SeckillUser user, GoodsVO goods){
        //减库存
        if(goodsService.reduceStock(goods) == 0){
            setGoodsOver(goods.getId()); //处理缓存
            return null; //如果减库存失败则直接返回，不执行后面的生成订单（解决了最终生成的订单大于商品库存的情况）
        }
        //order_info miaosha_order
        return orderService.createOrder(user, goods);
    }

    //获取秒杀结果
    public long getMiaoshaResult(Long userId, long goodsId) {
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
        if(order != null) { //有订单生成，秒杀成功
            return order.getOrderId();
        }else {
            boolean isOver = getGoodsOver(goodsId); //查缓存，秒杀是否结束
            if(isOver) {
                return -1; //秒杀结束状态
            }else {
                return 0; //排队中
            }
        }
    }

    //redis缓存 没有库存状态
    private void setGoodsOver(Long goodsId) {
        redisService.set(MiaoshaKey.isGoodsOver, ""+goodsId, true);
    }

    //redis获取 是否没有库存 状态
    private boolean getGoodsOver(long goodsId) {
        return redisService.exists(MiaoshaKey.isGoodsOver, ""+goodsId);
    }

    //重置库存和订单
    public void reset(List<GoodsVO> goodsList) {
        goodsService.resetStock(goodsList);
        orderService.deleteOrders();
    }

    /**
     * 秒杀结果
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     * */
    @RequestMapping(value="/result", method= RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(Model model, SeckillUser user,
                                      @RequestParam("goodsId")long goodsId) {
        model.addAttribute("user", user);
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result = miaoshaService.getMiaoshaResult(user.getId(), goodsId);
        return Result.success(result);
    }

    //生成秒杀path
    public String createMiaoshaPath(SeckillUser user, long goodsId) {
        if(user == null || goodsId <=0) {
            return null;
        }
        String str = MD5Util.md5(UUIDUtil.uuid()+"123456");
        redisService.set(MiaoshaKey.getMiaoshaPath, ""+user.getId() + "_"+ goodsId, str);
        return str;
    }

    //检查path
    public boolean checkPath(SeckillUser user, long goodsId, String path) {
        if(user == null || path == null) {
            return false;
        }
        String pathOld = redisService.get(MiaoshaKey.getMiaoshaPath, ""+user.getId() + "_"+ goodsId, String.class);
        return path.equals(pathOld);
    }

    //生成验证码图片
    public BufferedImage createVerifyCode(SeckillUser user, long goodsId) {
        if(user == null || goodsId <=0) {
            return null;
        }
        int width = 80;
        int height = 32;
        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //把验证码存到redis中
        int rnd = calc(verifyCode);
        redisService.set(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, rnd);
        //输出图片
        return image;
    }

    //检查验证码计算结果
    public boolean checkVerifyCode(SeckillUser user, long goodsId, int verifyCode) {
        if(user == null || goodsId <=0) {
            return false;
        }
        Integer codeOld = redisService.get(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, Integer.class);
        if(codeOld == null || codeOld - verifyCode != 0 ) {
            return false;
        }
        redisService.delete(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId);
        return true;
    }

    private static char[] ops = new char[] {'+', '-', '*'};
    /**
     * 生成 验证码表达式 字符串
     * + - *
     * */
    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = ""+ num1 + op1 + num2 + op2 + num3;
        return exp;
    }

    //计算验证码表达式结果
    private static int calc(String exp) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer)engine.eval(exp);
        }catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
