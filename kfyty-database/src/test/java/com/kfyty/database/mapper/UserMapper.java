package com.kfyty.database.mapper;

import com.kfyty.database.jdbc.BaseMapper;
import com.kfyty.database.jdbc.annotation.ForEach;
import com.kfyty.database.jdbc.annotation.SubQuery;
import com.kfyty.database.entity.User;
import com.kfyty.database.jdbc.annotation.Param;
import com.kfyty.database.jdbc.annotation.Query;
import com.kfyty.database.vo.UserVo;

import java.util.List;
import java.util.Map;

public interface UserMapper extends BaseMapper<Integer, User> {
    @Query("select id, id `user.id`, username `user.username`, create_time `user.createTime` from user where id = #{id}")
    UserVo findById(Integer id);

    @Query("select username from user where id = #{id}")
    String findNameById(@Param("id") Integer id);

    @Query(value = "select * from user", subQuery = @SubQuery(value = "select * from user where id = #{userId}", paramField = "id", mapperField = "userId", returnField = "user"))
    List<UserVo> findUserVo();

    @Query(value = "select id from user where id in ", forEach = @ForEach(collection = "ids", open = "(", separator = ",", close = ")", item = "id", sqlPart = "#{id}"))
    int[] findAllIds(@Param("ids") List<Integer> ids);

    @Query("select * from user where id = #{vo.id}")
    Map<String, Object> findMapById(@Param("vo") UserVo vo);

    @Query(value = "select * from user", key = "id")
    Map<String, User> findUserMap();

    @Query("select * from user")
    List<Map<String, Object>> findAllMap();

    /** 动态 SQL 测试 **/
    List<User> findLikeName(@Param("name") String name);
}
