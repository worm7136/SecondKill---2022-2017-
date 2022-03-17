package com.worm.seckill.rabbitmq;


import com.worm.seckill.entity.SeckillUser;

public class MiaoshaMessage {
	private SeckillUser user;
	private long goodsId;
	public SeckillUser getUser() {
		return user;
	}
	public void setUser(SeckillUser user) {
		this.user = user;
	}
	public long getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(long goodsId) {
		this.goodsId = goodsId;
	}
}
