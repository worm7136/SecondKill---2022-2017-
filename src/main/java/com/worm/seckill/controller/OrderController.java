package com.worm.seckill.controller;


import VO.GoodsVO;
import VO.OrderDetailVo;
import com.worm.seckill.entity.OrderInfo;
import com.worm.seckill.entity.SeckillUser;
import com.worm.seckill.redis.RedisService;
import com.worm.seckill.result.CodeMsg;
import com.worm.seckill.result.Result;
import com.worm.seckill.service.GoodsService;
import com.worm.seckill.service.OrderService;
import com.worm.seckill.service.SeckillUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {

	@Autowired
	SeckillUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	GoodsService goodsService;

	//订单详情
    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(Model model, SeckillUser user,
									  @RequestParam("orderId") long orderId) {
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	OrderInfo order = orderService.getOrderById(orderId);
    	if(order == null) {
    		return Result.error(CodeMsg.ORDER_NOT_EXIST);
    	}
    	long goodsId = order.getGoodsId();
    	GoodsVO goods = goodsService.getGoodsVOByGoodsId(goodsId);
    	OrderDetailVo vo = new OrderDetailVo();
    	vo.setOrder(order);
    	vo.setGoods(goods);
    	return Result.success(vo);
    }
    
}
