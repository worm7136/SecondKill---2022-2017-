package com.worm.seckill.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {

    public static String md5(String src){
        return DigestUtils.md5Hex(src);
    }

    private static final String salt = "1a2b3c4d";

    //第一次MD5 网络传输
    public static String inputPassToFormPass(String inputPass){
        String str = "" + salt.charAt(0) + salt.charAt(2) + inputPass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    //第二次MD5
    public static String formPassToDBPass(String formPass, String salt){
        String str = "" + salt.charAt(0)+salt.charAt(2) + formPass +salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    public static String inputPassToDBPass(String inputPass, String saltDB){
        String formPass = inputPassToFormPass(inputPass);
        String DBPass = formPassToDBPass(formPass, saltDB);
        return DBPass;
    }

    public static void main(String[] args) {
        //d3b1294a61a07da9b49b6e22b2cbd7f9
        System.out.println(inputPassToFormPass("123456"));
        //b7797cce01b4b131b433b6acf4add449
        System.out.println(formPassToDBPass("d3b1294a61a07da9b49b6e22b2cbd7f9", "1a2b3c4d"));
        //b7797cce01b4b131b433b6acf4add449
        System.out.println(inputPassToDBPass("123456", "1a2b3c4d"));
    }


}
