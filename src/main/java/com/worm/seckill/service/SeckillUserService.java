package com.worm.seckill.service;

import VO.LoginVO;
import com.worm.seckill.entity.SeckillUser;
import com.worm.seckill.exception.GlobalException;
import com.worm.seckill.mapper.SeckillUserMapper;
import com.worm.seckill.redis.MiaoshaUserKey;
import com.worm.seckill.redis.RedisService;
import com.worm.seckill.result.CodeMsg;
import com.worm.seckill.util.MD5Util;
import com.worm.seckill.util.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class SeckillUserService {

    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    private SeckillUserMapper seckillUserMapper;

    @Autowired
    private RedisService redisService;

    public SeckillUser getById(Long id){
        //取缓存
        SeckillUser user = redisService.get(MiaoshaUserKey.getById, ""+id, SeckillUser.class);
        if (user != null){
            return user;
        }
        //取数据库
        user = seckillUserMapper.getById(id);
        if (user != null){ //添加缓存
            redisService.set(MiaoshaUserKey.getById, ""+id, user);
        }
        return user;
    }

    //通过token获取user
    public SeckillUser getByToken(HttpServletResponse response, String token){
        if (StringUtils.isEmpty(token)){ //token验证是否为空
            return null;
        }
        SeckillUser user = redisService.get(MiaoshaUserKey.token, token, SeckillUser.class);
        //延长有效期
        if(user != null) {
            addCookie(response, token, user);
        }
        return user;
    }

    public boolean updatePassword(String token, Long id, String formPass){
        //取user
        SeckillUser user = getById(id);
        if (user == null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //更新数据库
        SeckillUser toBeUpdate = new SeckillUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass, user.getSalt()));
        seckillUserMapper.update(toBeUpdate);
        //处理缓存
        redisService.delete(MiaoshaUserKey.getById, ""+id);
        user.setPassword(toBeUpdate.getPassword());
        redisService.set(MiaoshaUserKey.token, token, user);
        return true;
    }

    public String login(HttpServletResponse response, LoginVO loginVo) {
        //登录信息非空验证
        if (loginVo == null){
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String userName = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        //用户是否存在
        SeckillUser user = seckillUserMapper.getById(Long.parseLong(userName));
        if (user == null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //密码
        String dbPass = user.getPassword();
        String dbSalt = user.getSalt();
        String calPass = MD5Util.formPassToDBPass(formPass, dbSalt);
        if (!calPass.equals(dbPass)){
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //生成cookie
        String token = UUIDUtil.uuid(); //UUID作为token
        addCookie(response, token, user);
        return token;
    }

    //生成cookie
    private void addCookie(HttpServletResponse response, String token, SeckillUser user){
        //存入redis(token作为key,user作为value)
        redisService.set(MiaoshaUserKey.token, token, user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        //设置cookie过期时间
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/");
        //响应时把cookie存回客户端
        response.addCookie(cookie);
    }
}
