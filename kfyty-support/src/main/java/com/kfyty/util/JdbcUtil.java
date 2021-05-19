package com.kfyty.util;

import com.kfyty.support.jdbc.ReturnType;
import com.kfyty.support.transaction.Transaction;
import lombok.extern.slf4j.Slf4j;

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
    public static <T, K, V> Object query(Transaction transaction, ReturnType<T, K, V> returnType, String sql, Object ... params) throws Exception {
        Connection connection = transaction.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = getPreparedStatement(connection, sql, params);
            resultSet = preparedStatement.executeQuery();
            Object obj = BeanUtil.fillObject(resultSet, returnType);
            if(log.isDebugEnabled()) {
                log.debug(":                <==      Total: {} [{}]", CommonUtil.size(obj), obj == null ? null : obj.getClass());
            }
            return obj;
        } catch(Exception e) {
            transaction.rollback();
            log.error(": failed execute sql statement:[{}] --> parameters:{}", sql, params);
            throw e;
        } finally {
            close(resultSet);
            close(preparedStatement);
            if(transaction.isAutoCommit()) {
                transaction.commit();
                transaction.close();
            }
        }
    }

    public static void execute(Transaction transaction, String sql, Object ... params) throws Exception {
        Connection connection = transaction.getConnection();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = getPreparedStatement(connection, sql, params);
            preparedStatement.execute();
            if(log.isDebugEnabled()) {
                log.debug(": executed sql statement:[{}] --> parameters:{}", sql, params);
            }
        } catch(SQLException e) {
            transaction.rollback();
            log.error(": failed execute sql statement:[{}] --> parameters:{}", sql, params);
            throw e;
        } finally {
            close(preparedStatement);
            if(transaction.isAutoCommit()) {
                transaction.commit();
                transaction.close();
            }
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

    private static <T, K, V> Object subQuery(Transaction transaction, ReturnType<T, K, V> returnType, String sql, Object ... params) throws Exception {
        return query(transaction, returnType, sql, params);
    }

    private static void execute(Transaction transaction, ReturnType non, String sql, Object ... params) throws Exception {
        execute(transaction, sql, params);
    }

    private static void close(Object obj) throws Exception {
        if(obj instanceof AutoCloseable) {
            ((AutoCloseable) obj).close();
        }
    }
}
