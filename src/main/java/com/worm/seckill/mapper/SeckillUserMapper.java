package com.worm.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.worm.seckill.entity.SeckillUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface SeckillUserMapper {

    @Select("select * from miaosha_user where id = #{id}")
    SeckillUser getById(@Param("id") Long id);

    @Update("update miaosha_user set password = #{password} where id = #{id}")
    void update(SeckillUser toBeUpdate);
}
