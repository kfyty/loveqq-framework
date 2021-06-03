package com.kfyty.support.utils;

import com.kfyty.support.jdbc.MethodParameter;
import com.kfyty.support.jdbc.ReturnType;
import com.kfyty.support.transaction.Transaction;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 功能描述: jdbc 工具
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/20 13:41
 * @since JDK 1.8
 */
@Slf4j
public abstract class JdbcUtil {
    public static <T, K, V> Object query(Transaction transaction, ReturnType<T, K, V> returnType, String sql, MethodParameter... params) throws SQLException {
        Connection connection = transaction.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = getPreparedStatement(connection, sql, params);
            resultSet = preparedStatement.executeQuery();
            Object obj = ResultSetUtil.processObject(resultSet, returnType);
            if(log.isDebugEnabled()) {
                log.debug(":                <==      Total: {} [{}]", CommonUtil.size(obj), obj == null ? null : obj.getClass());
            }
            return obj;
        } catch(Exception e) {
            transaction.rollback();
            log.error(": failed execute sql statement:[{}] --> parameters:{}", sql, params == null ? null : Arrays.stream(params).map(MethodParameter::getValue).collect(Collectors.toList()));
            throw new SQLException(e);
        } finally {
            CommonUtil.close(resultSet);
            CommonUtil.close(preparedStatement);
            if(transaction.isAutoCommit()) {
                transaction.commit();
                transaction.close();
            }
        }
    }

    public static void execute(Transaction transaction, String sql, MethodParameter ... params) throws SQLException {
        Connection connection = transaction.getConnection();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = getPreparedStatement(connection, sql, params);
            preparedStatement.execute();
        } catch(SQLException e) {
            transaction.rollback();
            log.error(": failed execute sql statement:[{}] --> parameters:{}", sql, params == null ? null : Arrays.stream(params).map(MethodParameter::getValue).collect(Collectors.toList()));
            throw e;
        } finally {
            CommonUtil.close(preparedStatement);
            if(transaction.isAutoCommit()) {
                transaction.commit();
                transaction.close();
            }
        }
    }

    public static PreparedStatement getPreparedStatement(Connection connection, String sql, MethodParameter ... params) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for(int i = 0; params != null && i < params.length; i++) {
            MethodParameter parameter = params[i];
            if(!ResultSetUtil.TYPE_HANDLER.containsKey(parameter.getParamType())) {
                preparedStatement.setObject(i + 1, parameter.getValue());
                continue;
            }
            ResultSetUtil.TYPE_HANDLER.get(parameter.getParamType()).setParameter(preparedStatement, i + 1, parameter.getValue());
        }
        if(log.isDebugEnabled()) {
            log.debug(": ==>  Preparing: {}", sql);
            log.debug(": ==> Parameters: {}", params == null ? null : Arrays.stream(params).map(MethodParameter::getValue).collect(Collectors.toList()));
        }
        return preparedStatement;
    }

    private static <T, K, V> Object subQuery(Transaction transaction, ReturnType<T, K, V> returnType, String sql, MethodParameter ... params) throws SQLException {
        return query(transaction, returnType, sql, params);
    }

    @SuppressWarnings("rawtypes")
    private static void execute(Transaction transaction, ReturnType non, String sql, MethodParameter ... params) throws SQLException {
        execute(transaction, sql, params);
    }
}
