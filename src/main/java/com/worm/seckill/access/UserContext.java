package com.worm.seckill.access;


/**
 * ThreadLocal：多线程的时候保证线程安全的一种方式
 * 和当前线程绑定，每个线程独有一份
 */
import com.worm.seckill.entity.SeckillUser;

public class UserContext {
	
	private static ThreadLocal<SeckillUser> userHolder = new ThreadLocal<SeckillUser>();
	
	public static void setUser(SeckillUser user) {
		userHolder.set(user);  //线程安全
	}
	
	public static SeckillUser getUser() {
		return userHolder.get();
	}

}
