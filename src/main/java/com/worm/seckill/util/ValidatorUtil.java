package com.worm.seckill.util;

import org.thymeleaf.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//验证手机号码合法性
public class ValidatorUtil {

    //合法手机号:开头1+10位十进制数
    private static final Pattern mobile_pattern = Pattern.compile("1\\d{10}");

    public static boolean isMobile(String str){
        //手机号为空
        if (StringUtils.isEmpty(str)){
            return false;
        }
        //合法格式匹配
        Matcher m = mobile_pattern.matcher(str);
        return m.matches();
    }

//    public static void main(String[] args) {
//        System.out.println(isMobile("18912341234")); //true
//        System.out.println(isMobile("1891234123")); //false
//        System.out.println(isMobile("28912341234")); //false
//    }
}
