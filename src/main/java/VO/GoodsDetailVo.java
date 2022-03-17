package VO;

import com.worm.seckill.entity.SeckillUser;

public class GoodsDetailVo {
	private int miaoshaStatus = 0;
	private int remainSeconds = 0;
	private GoodsVO goods ;
	private SeckillUser user;
	public int getMiaoshaStatus() {
		return miaoshaStatus;
	}
	public void setMiaoshaStatus(int miaoshaStatus) {
		this.miaoshaStatus = miaoshaStatus;
	}
	public int getRemainSeconds() {
		return remainSeconds;
	}
	public void setRemainSeconds(int remainSeconds) {
		this.remainSeconds = remainSeconds;
	}
	public GoodsVO getGoods() {
		return goods;
	}
	public void setGoods(GoodsVO goods) {
		this.goods = goods;
	}
	public SeckillUser getUser() {
		return user;
	}
	public void setUser(SeckillUser user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "GoodsDetailVo{" +
				"miaoshaStatus=" + miaoshaStatus +
				", remainSeconds=" + remainSeconds +
				", goods=" + goods +
				", user=" + user +
				'}';
	}
}
