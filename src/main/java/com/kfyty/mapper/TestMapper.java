package com.kfyty.mapper;

import com.kfyty.annotation.Execute;
import com.kfyty.annotation.Param;
import com.kfyty.annotation.SelectList;
import com.kfyty.annotation.SelectOne;
import com.kfyty.vo.Test;

import java.sql.SQLException;
import java.util.List;

/**
 * 功能描述: mapper 测试
 *
 * @author zhangkun@wisdombud.com
 * @date 2019/6/27 18:02:26
 * @since JDK 1.8
 */
public interface TestMapper {

    @SelectOne("select * from test where id = #{id}")
    @SelectOne("select * from test where name = #{name}")
    public List<Test> findTestById(@Param("id") Integer id, @Param("name") String name) throws SQLException;

    @SelectList("select * from test")
    public List<Test> findAllTest() throws SQLException;

    @SelectList("select * from test where id = #{id} or name like '%${value}%'")
    public List<Test> findTestLike(@Param("id") Integer id, @Param("value") String value) throws SQLException;

    @Execute("insert into test values (#{test.id}, #{test.name})")
    public void saveTest(@Param("test") Test test) throws SQLException;

    @Execute("update test set name = #{name} where id = #{id}")
    public void updateTestById(@Param("name") String name, @Param("id") Integer id) throws SQLException;

    @Execute("delete from test where id = #{id}")
    public void deleteTestById(@Param("id") Integer id) throws SQLException;
}
