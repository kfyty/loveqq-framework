package com.kfyty.database.test.mapper;

import com.kfyty.database.jdbc.BaseMapper;
import com.kfyty.database.test.entity.User;
import com.kfyty.database.jdbc.annotation.Param;
import com.kfyty.database.jdbc.annotation.Query;

import java.util.List;
import java.util.Map;

public interface UserMapper extends BaseMapper<Integer, User> {
    @Query("select * from user where id = #{id}")
    User findById(@Param("id") Integer id);

    @Query("select username from user where id = #{id}")
    String findNameById(@Param("id") Integer id);

    @Query("select * from user")
    List<User> findAll();

    @Query("select id from user")
    int[] findAllIds();

    @Query("select * from user where id = #{id}")
    Map<String, Object> findMapById(@Param("id") Integer id);

    @Query(value = "select * from user", key = "id")
    Map<String, User> findUserMap();

    @Query("select * from user")
    List<Map<String, Object>> findAllMap();
}
