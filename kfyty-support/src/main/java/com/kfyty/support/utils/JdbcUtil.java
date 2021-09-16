package com.kfyty.support.utils;

import com.kfyty.support.generic.SimpleGeneric;
import com.kfyty.support.method.MethodParameter;
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
    public static Object query(Transaction transaction, SimpleGeneric returnType, String sql, MethodParameter... params) throws SQLException {
        Connection connection = transaction.getConnection();
        try (PreparedStatement preparedStatement = getPreparedStatement(connection, sql, params);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            Object obj = ResultSetUtil.processObject(resultSet, returnType);
            if (log.isDebugEnabled()) {
                log.debug("<==         total: {} [{}]", CommonUtil.size(obj), obj == null ? null : obj.getClass());
            }
            return obj;
        } catch (SQLException e) {
            transaction.rollback();
            log.error("failed execute SQL statement: {} --> parameters: {}", sql, params == null ? null : Arrays.stream(params).map(MethodParameter::getValue).collect(Collectors.toList()));
            throw e;
        } finally {
            commitTransactionIfNecessary(transaction);
        }
    }

    public static int execute(Transaction transaction, String sql, MethodParameter... params) throws SQLException {
        Connection connection = transaction.getConnection();
        try (PreparedStatement preparedStatement = getPreparedStatement(connection, sql, params)) {
            preparedStatement.execute();
            int updateCount = preparedStatement.getUpdateCount();
            if (log.isDebugEnabled()) {
                log.debug("<== affected rows: {}", updateCount);
            }
            return updateCount;
        } catch (SQLException e) {
            transaction.rollback();
            log.error("failed execute SQL statement: {} --> parameters: {}", sql, params == null ? null : Arrays.stream(params).map(MethodParameter::getValue).collect(Collectors.toList()));
            throw e;
        } finally {
            commitTransactionIfNecessary(transaction);
        }
    }

    public static PreparedStatement getPreparedStatement(Connection connection, String sql, MethodParameter... params) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; params != null && i < params.length; i++) {
            MethodParameter parameter = params[i];
            if (!ResultSetUtil.TYPE_HANDLER.containsKey(parameter.getParamType())) {
                preparedStatement.setObject(i + 1, parameter.getValue());
                continue;
            }
            ResultSetUtil.TYPE_HANDLER.get(parameter.getParamType()).setParameter(preparedStatement, i + 1, parameter.getValue());
        }
        if (log.isDebugEnabled()) {
            log.debug("==>     preparing: {}", sql);
            log.debug("==>    parameters: {}", params == null ? null : Arrays.stream(params).map(MethodParameter::getValue).collect(Collectors.toList()));
        }
        return preparedStatement;
    }

    public static void commitTransactionIfNecessary(Transaction transaction) throws SQLException {
        if (transaction.isAutoCommit()) {
            transaction.commit();
            CommonUtil.close(transaction);
        }
    }

    private static Object subQuery(Transaction transaction, SimpleGeneric returnType, String sql, MethodParameter... params) throws SQLException {
        return query(transaction, returnType, sql, params);
    }

    private static int execute(Transaction transaction, SimpleGeneric non, String sql, MethodParameter... params) throws SQLException {
        return execute(transaction, sql, params);
    }
}
