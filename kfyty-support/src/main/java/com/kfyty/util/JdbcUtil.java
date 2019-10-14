package com.kfyty.util;

import com.kfyty.support.jdbc.ReturnType;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * 功能描述: jdbc 工具
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/20 13:41
 * @since JDK 1.8
 */
@Slf4j
public class JdbcUtil {
    public static <T, K, V> Object query(DataSource dataSource, ReturnType<T, K, V> returnType, String sql, Object ... params) throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = getPreparedStatement(connection, sql, params);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            Object obj = BeanUtil.fillObject(resultSet, returnType);
            if(log.isDebugEnabled()) {
                log.debug(":                <==      Total: {} [{}]", CommonUtil.size(obj), obj.getClass());
            }
            return obj;
        } catch(Exception e) {
            log.error(": failed execute sql statement:[{}] --> parameters:{}", sql, params);
            throw e;
        }
    }

    public static void execute(DataSource dataSource, String sql, Object ... params) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = getPreparedStatement(connection, sql, params)) {
            preparedStatement.execute();
            if(log.isDebugEnabled()) {
                log.debug(": executed sql statement:[{}] --> parameters:{}", sql, params);
            }
        } catch(SQLException e) {
            log.error(": failed execute sql statement:[{}] --> parameters:{}", sql, params);
            throw e;
        }
    }

    public static PreparedStatement getPreparedStatement(Connection connection, String sql, Object ... params) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for(int i = 0; params != null && i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
        if(log.isDebugEnabled()) {
            log.debug(": ==>  Preparing: {}", sql);
            log.debug(": ==> Parameters: {}", Arrays.toString(params));
        }
        return preparedStatement;
    }

    private static <T, K, V> Object subQuery(DataSource dataSource, ReturnType<T, K, V> returnType, String sql, Object ... params) throws Exception {
        return query(dataSource, returnType, sql, params);
    }

    private static void execute(DataSource dataSource, ReturnType non, String sql, Object ... params) throws SQLException {
        execute(dataSource, sql, params);
    }
}
