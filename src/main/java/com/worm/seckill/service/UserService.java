package com.worm.seckill.service;

import com.worm.seckill.entity.User;
import com.worm.seckill.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    @Autowired
    UserMapper userMapper;

    public User getById(int id){
        return userMapper.getById(id);
    }

    //测试事务
    @Transactional
    public boolean insert(User user) {
        User u1 = new User(2,"worm");
        //User u2 = new User(1,"111"); //无法插入，会回滚
        userMapper.insert(u1);
        //userMapper.insert(u2);
        return true;
    }
}
