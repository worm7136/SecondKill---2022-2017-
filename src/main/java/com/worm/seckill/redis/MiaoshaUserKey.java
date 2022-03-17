package com.worm.seckill.redis;

public class MiaoshaUserKey extends BasePrefix {

    //过期时间: 2天
    public static final int TOKEN_EXPIRE = 3600 * 24 * 2;

    public MiaoshaUserKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static MiaoshaUserKey token = new MiaoshaUserKey(TOKEN_EXPIRE, "token");
    public static MiaoshaUserKey getById = new MiaoshaUserKey(0, "id");
}
