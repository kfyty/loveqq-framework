package com.kfyty.database.mapper;

import com.kfyty.database.entity.User;
import com.kfyty.database.vo.UserVo;
import com.kfyty.loveqq.framework.data.korm.BaseMapper;
import com.kfyty.loveqq.framework.data.korm.annotation.ForEach;
import com.kfyty.loveqq.framework.data.korm.annotation.If;
import com.kfyty.loveqq.framework.data.korm.annotation.Param;
import com.kfyty.loveqq.framework.data.korm.annotation.Query;
import com.kfyty.loveqq.framework.data.korm.annotation.SubQuery;

import java.util.List;
import java.util.Map;

public interface UserMapper extends BaseMapper<Integer, User> {
    @Query("select id, id `user.id`, username `user.username`, create_time `user.createTime` from user where id = #{id}")
    UserVo findById(Integer id);

    @Query("select username from user where id = #{id}")
    String findNameById(@Param("id") Integer id);

    @Query(value = "select * from user", subQuery = @SubQuery(value = "select * from user where id = #{userId}", paramField = "id", mapperField = "userId", returnField = "user"))
    List<UserVo> findUserVo();

    @Query(value = "select id from user where username = #{username}", _if = @If(test = "ids != null and ids.size() > 0", value = "or id in", forEach = @ForEach(collection = "ids", item = "id", sql = "#{id}")))
    int[] findAllIds(String username, @Param("ids") List<Integer> ids);

    @Query("select * from user where id = #{vo.id}")
    Map<String, Object> findMapById(@Param("vo") UserVo vo);

    @Query(value = "select * from user", key = "id")
    Map<String, User> findUserMap();

    @Query("select * from user")
    List<Map<String, Object>> findAllMap();

    @Query("select * from user where username like '%${name}%' or create_time like '%${name}%'")
    List<User> findLikeName1(@Param("name") String name);

    /** 动态 SQL 测试 **/
    List<User> findLikeName2(@Param("name") String name);
}
