package com.kfyty.database;

import com.jfinal.template.Engine;
import com.kfyty.database.entity.User;
import com.kfyty.database.mapper.UserMapper;
import com.kfyty.database.vo.UserVo;
import com.kfyty.loveqq.framework.core.jdbc.JdbcTransaction;
import com.kfyty.loveqq.framework.core.support.io.PathMatchingResourcePatternResolver;
import com.kfyty.loveqq.framework.core.utils.PropertiesUtil;
import com.kfyty.loveqq.framework.data.jdbc.intercept.internal.GeneratedKeysInterceptor;
import com.kfyty.loveqq.framework.data.jdbc.intercept.internal.IfInternalInterceptor;
import com.kfyty.loveqq.framework.data.jdbc.intercept.internal.SubQueryInternalInterceptor;
import com.kfyty.loveqq.framework.data.jdbc.session.Configuration;
import com.kfyty.loveqq.framework.data.jdbc.session.SqlSessionProxyFactory;
import com.kfyty.loveqq.framework.data.jdbc.sql.dynamic.DynamicProvider;
import com.kfyty.loveqq.framework.data.jdbc.sql.dynamic.enjoy.EnjoyDynamicProvider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class QueryTest {
    private static final String PATH = "druid.properties";

    private UserMapper userMapper;

    @BeforeEach
    public void prepare() throws Exception {
        Properties load = PropertiesUtil.load(PATH);
        DataSource dataSource = new HikariDataSource(new HikariConfig(load));
        DynamicProvider<?> dynamicProvider = new EnjoyDynamicProvider().setEngine(Engine.create("test"));
        Configuration configuration = new Configuration()
                .setDataSource(dataSource)
                .setPathMatchingResourcePatternResolver(new PathMatchingResourcePatternResolver())
                .setTransactionFactory(() -> new JdbcTransaction(dataSource))
                .addInterceptor(new GeneratedKeysInterceptor())
                .addInterceptor(new IfInternalInterceptor())
                .addInterceptor(new SubQueryInternalInterceptor())
                .setDynamicProvider(dynamicProvider, "mapper/*.xml");
        SqlSessionProxyFactory proxyFactory = new SqlSessionProxyFactory(configuration);
        this.userMapper = proxyFactory.createProxy(UserMapper.class);
    }

    @Test
    public void test() {
        User newUser = User.create();
        this.userMapper.insert(newUser);
        this.userMapper.insertBatch(Arrays.asList(User.create(), User.create()));
        User one = this.userMapper.selectByPk(newUser.getId());
        List<User> more = this.userMapper.selectByPks(Collections.singletonList(newUser.getId()));
        one.setUsername("update");
        one.setImage(null);
        this.userMapper.updateByPk(one);
        UserVo user = this.userMapper.findById(one.getId());
        String name = this.userMapper.findNameById(one.getId());
        List<UserVo> userVo = this.userMapper.findUserVo();
        List<User> users = this.userMapper.selectAll();
        users.get(0).setUsername("test1");
        users.get(1).setUsername("test2");
        users.get(2).setUsername("test3");
        users.get(2).setImage(null);
        this.userMapper.updateBatch(users);
        int[] ids = this.userMapper.findAllIds("test", Collections.singletonList(one.getId()));
        Map<String, Object> map = this.userMapper.findMapById(UserVo.create(newUser.getId()));
        Map<String, User> userMap = this.userMapper.findUserMap();
        List<Map<String, Object>> maps = this.userMapper.findAllMap();
        List<User> likeName = this.userMapper.findLikeName1("test");
        List<User> likeNull = this.userMapper.findLikeName2(null);
        List<User> likeTest = this.userMapper.findLikeName2("test");
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
        System.out.println(likeName);
        System.out.println(likeNull);
        System.out.println(likeTest);
    }
}
