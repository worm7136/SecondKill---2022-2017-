package com.worm.seckill.controller;

import VO.GoodsDetailVo;
import VO.GoodsVO;
import com.worm.seckill.entity.SeckillUser;
import com.worm.seckill.redis.GoodsKey;
import com.worm.seckill.redis.RedisService;
import com.worm.seckill.result.Result;
import com.worm.seckill.service.GoodsService;
import com.worm.seckill.service.SeckillUserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("goods")
public class GoodsController {

    @Autowired
    SeckillUserService userService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    RedisService redisService;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    ApplicationContext applicationContext;


    /**
     * redis页面缓存优化后
     * QPS:335
     * 并发量：2000*10
     */
    //商品列表
    @RequestMapping(value = "/to_list", produces = "text/html")
    @ResponseBody
    public String list(HttpServletRequest request, HttpServletResponse response, Model model, SeckillUser user){
        model.addAttribute("user", user);
        //取缓存
        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        if (!StringUtils.isEmpty(html)){ //取到了缓存则返回
            return html;
        }
        //没取到则从数据库取
        List<GoodsVO> goodsList = goodsService.goodsVOList();
        model.addAttribute("goodsList", goodsList);

        //这里对应spring4中的SpringWebContext
        IWebContext ctx = new WebContext(request, response
                , request.getServletContext(), request.getLocale(), model.asMap());
        //手动渲染
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
        //添加到缓存
        if (!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsList, "", html);
        }
        return html;
        //设置缓存过期时间60s
    }

    //商品详情页（优化改造为静态页面）
    @RequestMapping(value="/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, Model model,SeckillUser user,
                                        @PathVariable("goodsId")long goodsId) {
        GoodsVO goods = goodsService.getGoodsVOByGoodsId(goodsId);
        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();
        int miaoshaStatus = 0;
        int remainSeconds = 0;
        if(now < startAt ) {//秒杀还没开始，倒计时
            miaoshaStatus = 0;
            remainSeconds = (int)((startAt - now )/1000);
        }else  if(now > endAt){//秒杀已经结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else {//秒杀进行中
            miaoshaStatus = 1;
            remainSeconds = 0;
        }
        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setGoods(goods);
        vo.setUser(user);
        vo.setRemainSeconds(remainSeconds);
        vo.setMiaoshaStatus(miaoshaStatus);
        return Result.success(vo);
    }

    //商品详情页
    @RequestMapping(value = "/to_detail2/{goodsId}", produces = "text/html")
    @ResponseBody
    public String detail2(Model model, SeckillUser user,
                         @PathVariable("goodsId") Long goodsId, HttpServletRequest request, HttpServletResponse response){
        model.addAttribute("user", user);
        //取缓存
        String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
        if (!StringUtils.isEmpty(html)){
            return html;
        }

        //手动渲染：通过商品id获取GoodsVO详情
        GoodsVO goods = goodsService.getGoodsVOByGoodsId(goodsId);
        model.addAttribute("goods", goods);

        //秒杀时间:开始,结束,当前时间
        Long startAt = goods.getStartDate().getTime();
        Long endAt = goods.getEndDate().getTime();
        Long now = System.currentTimeMillis();

        int miaoshaStatus = 0; //秒杀状态
        int remainSeconds = 0; //距离秒杀开始时间(倒计时)

        if (now < startAt){ //秒杀未开始,倒计时
            miaoshaStatus = 0;
            remainSeconds = (int) (startAt - now)/1000;
        }else if (now > endAt){ //秒杀已结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else{ //秒杀进行中
            miaoshaStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("miaoshaStatus", miaoshaStatus);
        model.addAttribute("remainSeconds", remainSeconds);
        //return "goods_detail";

        IWebContext ctx = new WebContext(request, response
                , request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
        if (!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsDetail, ""+goodsId, html);
        }
        return html;
    }
}
