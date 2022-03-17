package com.worm.seckill.controller;

import com.worm.seckill.entity.User;
import com.worm.seckill.rabbitmq.MQSender;
import com.worm.seckill.redis.RedisService;
import com.worm.seckill.redis.UserKey;
import com.worm.seckill.result.Result;
import com.worm.seckill.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/test")
public class SampleController {

    @Autowired
    UserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender sender;

//    @RequestMapping("mq")
//    @ResponseBody
//    public Result<String> mq(){
//        sender.send("hello, worm");
//        return Result.success("hello, world!");
//    }

    @RequestMapping("/get")
    @ResponseBody
    public User test(){
        User user = userService.getById(1);
        return user;
    }

    @RequestMapping("/insert")
    @ResponseBody
    public Result<Boolean> insert(User user){
        return Result.success(userService.insert(user));
    }

    //redis测试
    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet() {
        User user  = redisService.get(UserKey.getById, ""+1, User.class);
        return Result.success(user);
    }

    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet() {
        User user  = new User();
        user.setId(1);
        user.setName("1111");
        redisService.set(UserKey.getById, ""+1, user);//UserKey:id1
        return Result.success(true);
    }
}
