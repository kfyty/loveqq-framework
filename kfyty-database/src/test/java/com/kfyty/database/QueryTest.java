package com.kfyty.database;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.kfyty.database.entity.User;
import com.kfyty.database.mapper.UserMapper;
import com.kfyty.database.jdbc.SqlSessionFactory;
import com.kfyty.database.vo.UserVo;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class QueryTest {
    private static final String PATH = "/druid.properties";

    private UserMapper userMapper;

    @Before
    public void prepare() throws Exception {
        Properties properties = new Properties();
        properties.load(QueryTest.class.getResourceAsStream(PATH));
        DataSource dataSource = DruidDataSourceFactory.createDataSource(properties);
        this.userMapper = SqlSessionFactory.createProxy(dataSource, UserMapper.class);
    }

    @Test
    public void test() {
        User newUser = User.create();
        this.userMapper.insert(newUser);
        this.userMapper.insertBatch(Arrays.asList(User.create(), User.create()));
        User one = this.userMapper.selectByPk(newUser.getId());
        List<User> more = this.userMapper.selectByPks(Collections.singletonList(newUser.getId()));
        one.setUsername("update");
        this.userMapper.updateByPk(one);
        UserVo user = this.userMapper.findById(one.getId());
        String name = this.userMapper.findNameById(one.getId());
        List<UserVo> userVo = this.userMapper.findUserVo();
        List<User> users = this.userMapper.selectAll();
        this.userMapper.updateBatch(users);
        int[] ids = this.userMapper.findAllIds(Collections.singletonList(one.getId()));
        Map<String, Object> map = this.userMapper.findMapById(UserVo.create(newUser.getId()));
        Map<String, User> userMap = this.userMapper.findUserMap();
        List<Map<String, Object>> maps = this.userMapper.findAllMap();
        this.userMapper.deleteByPk(newUser.getId());
        this.userMapper.deleteByPks(Collections.singletonList(newUser.getId()));
        this.userMapper.deleteAll();

        System.out.println(more);
        System.out.println(newUser);
        System.out.println(user);
        System.out.println(name);
        System.out.println(userVo);
        System.out.println(users);
        System.out.println(Arrays.toString(ids));
        System.out.println(map);
        System.out.println(userMap);
        System.out.println(maps);
    }
}
