package com.worm.seckill.service;

import VO.GoodsVO;
import com.worm.seckill.entity.MiaoshaOrder;
import com.worm.seckill.entity.OrderInfo;
import com.worm.seckill.entity.SeckillUser;
import com.worm.seckill.mapper.OrderMapper;
import com.worm.seckill.redis.OrderKey;
import com.worm.seckill.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class OrderService {

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    RedisService redisService;

    //查询秒杀订单
    public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(long userId, long goodsId) {
        //return orderMapper.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
        //小优化（没必要查数据库，查缓存
        return redisService.get(OrderKey.getMiaoshaOrderByUidGid, ""+userId+"_"+goodsId, MiaoshaOrder.class);
    }

    public OrderInfo getOrderById(long orderId) {
        return orderMapper.getOrderById(orderId);
    }

    //创建订单
    public OrderInfo createOrder(SeckillUser user, GoodsVO goods){
        //下订单
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getMiaoshaPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());
        orderMapper.insert(orderInfo);
        //写入秒杀订单
        MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
        miaoshaOrder.setGoodsId(goods.getId());
        miaoshaOrder.setOrderId(orderInfo.getId());
        miaoshaOrder.setUserId(user.getId());
        orderMapper.insertMiaoshaOrder(miaoshaOrder);

        //写入缓存（小优化
        redisService.set(OrderKey.getMiaoshaOrderByUidGid, ""+user.getId()+"_"+goods.getId(), miaoshaOrder);
        return orderInfo;
    }

    public void deleteOrders() {
        orderMapper.deleteOrders();
        orderMapper.deleteMiaoshaOrders();
    }
}
