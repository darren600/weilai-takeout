package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {
    /*
    * 根据openid获取用户信息
    * @param openid 微信用户的openid
    * @return 用户信息
    * */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);


    /*
    * 插入用户信息
    * @param user 用户信息
    * */
    void insert(User user);


    /**
     * 根据动态条件统计用户数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
