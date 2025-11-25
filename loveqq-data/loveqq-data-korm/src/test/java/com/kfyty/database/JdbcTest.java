package com.kfyty.database;

import com.kfyty.database.entity.User;
import com.kfyty.loveqq.framework.core.jdbc.JdbcTransaction;
import com.kfyty.loveqq.framework.core.utils.JdbcUtil;
import com.kfyty.loveqq.framework.core.utils.PropertiesUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class JdbcTest {
    public static final String PATH = "druid.properties";

    @Test
    public void jdbcTest() throws SQLException {
        Properties load = PropertiesUtil.load(JdbcTest.PATH);
        try (HikariDataSource dataSource = new HikariDataSource(new HikariConfig(load))) {
            JdbcTransaction transaction = new JdbcTransaction(dataSource);

            // 删除全部
            JdbcUtil.execute(dataSource, "delete from user");

            // 批量插入
            String sql = "insert into user(username, status, create_time) values(?, ?, ?)";
            PreparedStatement preparedStatement = JdbcUtil.getPreparedStatement(
                    transaction.getConnection(),
                    sql,
                    JdbcUtil::preparedStatement,
                    new Object[][]{
                            new Object[] {"test", 1, LocalDateTime.now()},
                            new Object[] {"sdnkvniosniotestosniovni", 2, LocalDateTime.now()}
                    });
            int execute = JdbcUtil.execute(transaction, preparedStatement);
            Assertions.assertEquals(execute, 2);

            // 查询单个
            User single = JdbcUtil.query(dataSource, User.class, "select * from user where username = ?", "sdnkvniosniotestosniovni");
            Assertions.assertNotNull(single);

            // 单个 map
            Map<String, Object> map = JdbcUtil.queryMap(dataSource, String.class, Object.class, "select * from user where username = ?", "sdnkvniosniotestosniovni");
            Assertions.assertNotNull(map);

            // 查询 list
            List<User> list = JdbcUtil.queryList(dataSource, User.class, "select * from user where username like concat('%', ?, '%')", "test");
            Assertions.assertEquals(list.size(), 2);

            // 查询多个 map
            List<Map<Object, Object>> listMap = JdbcUtil.queryListMap(dataSource, "select * from user where username like concat('%', ?, '%')", "test");
            Assertions.assertEquals(listMap.size(), 2);

            // 删除
            int deleted = JdbcUtil.execute(dataSource, "delete from user where username in (?, ?)", "test", "sdnkvniosniotestosniovni");
            Assertions.assertEquals(execute, 2);
        }
    }
}
