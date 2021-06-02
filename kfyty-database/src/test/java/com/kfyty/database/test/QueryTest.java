package com.kfyty.database.test;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.kfyty.database.test.entity.User;
import com.kfyty.database.test.mapper.UserMapper;
import com.kfyty.database.jdbc.SqlSessionFactory;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Arrays;
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
        User one = this.userMapper.selectByPk(newUser.getId());
        one.setUsername("update");
        this.userMapper.updateByPk(one);
        User user = this.userMapper.findById(one.getId());
        String name = this.userMapper.findNameById(one.getId());
        List<User> users = this.userMapper.findAll();
        int[] ids = this.userMapper.findAllIds();
        Map<String, Object> map = this.userMapper.findMapById(newUser.getId());
        Map<String, User> userMap = this.userMapper.findUserMap();
        List<Map<String, Object>> maps = this.userMapper.findAllMap();
        this.userMapper.deleteByPk(newUser.getId());

        System.out.println(newUser);
        System.out.println(user);
        System.out.println(name);
        System.out.println(users);
        System.out.println(Arrays.toString(ids));
        System.out.println(map);
        System.out.println(userMap);
        System.out.println(maps);
    }
}
