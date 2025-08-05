package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.User;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    @Select("select * from user where openid = #{openid}")
    User getUserByOpenid(String openid);

    void insert(User user);

    @Select("select * from user where id = #{userId}")
    User getUserByUserId(Long userId);

    Integer countByMap(Map map);
}
