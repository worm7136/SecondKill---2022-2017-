package com.worm.seckill.controller;

import VO.LoginVO;
import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.extension.api.R;
import com.worm.seckill.redis.RedisService;
import com.worm.seckill.result.CodeMsg;
import com.worm.seckill.result.Result;
import com.worm.seckill.service.SeckillUserService;
import com.worm.seckill.service.UserService;
import com.worm.seckill.util.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;


@Controller
@RequestMapping("/login")
public class LoginController {

    private static Logger log = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    SeckillUserService seckillUserService;

    @Autowired
    RedisService redisService;

    // 跳转到 login 页面
    @RequestMapping("to_login")
    public String toLogin(){
        return "login";
    }

    // 做登录操作
    @RequestMapping("do_login")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response, @Valid LoginVO loginVo){
        log.info(loginVo.toString());
        //登录
        String token = seckillUserService.login(response, loginVo);
        return Result.success(token);
    }

}
