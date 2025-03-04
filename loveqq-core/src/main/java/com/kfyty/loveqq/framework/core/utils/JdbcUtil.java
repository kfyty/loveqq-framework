package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import com.kfyty.loveqq.framework.core.jdbc.JdbcTransaction;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.jdbc.type.TypeHandler;
import com.kfyty.loveqq.framework.core.jdbc.transaction.Transaction;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.function.BiFunction;
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

    @SuppressWarnings("unchecked")
    public static <T> T query(DataSource dataSource, Class<T> returnType, String sql, Object... params) throws SQLException {
        return (T) query(dataSource, SimpleGeneric.from(returnType), sql, params);
    }

    public static Object query(DataSource dataSource, SimpleGeneric returnType, String sql, Object... params) throws SQLException {
        MethodParameter[] parameters = Arrays.stream(params).map(e -> new MethodParameter(e.getClass(), e)).toArray(MethodParameter[]::new);
        return query(dataSource, returnType, sql, parameters);
    }

    public static Object query(DataSource dataSource, SimpleGeneric returnType, String sql, MethodParameter... params) throws SQLException {
        return query(new JdbcTransaction(dataSource), returnType, sql, params);
    }

    public static Object query(Transaction transaction, SimpleGeneric returnType, String sql, MethodParameter... params) throws SQLException {
        Connection connection = transaction.getConnection();
        try (PreparedStatement preparedStatement = getPreparedStatement(connection, sql, params);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            Object obj = ResultSetUtil.processObject(resultSet, returnType);
            LogUtil.logIfDebugEnabled(log, log -> log.debug("\r\n<==         total: {} {}", CommonUtil.size(obj), obj == null ? null : obj.getClass()));
            return obj;
        } catch (SQLException e) {
            transaction.rollback();
            log.error("execute SQL statement error: {} --> parameters: {}", sql, params == null ? null : Arrays.stream(params).map(MethodParameter::getValue).collect(Collectors.toList()));
            throw e;
        } finally {
            commitTransactionIfNecessary(transaction);
        }
    }

    public static int execute(DataSource dataSource, String sql, Object... params) throws SQLException {
        MethodParameter[] parameters = Arrays.stream(params).map(e -> new MethodParameter(e.getClass(), e)).toArray(MethodParameter[]::new);
        return execute(dataSource, sql, parameters);
    }

    public static int execute(DataSource dataSource, String sql, MethodParameter... params) throws SQLException {
        return execute(new JdbcTransaction(dataSource), sql, params);
    }

    public static int execute(Transaction transaction, String sql, MethodParameter... params) throws SQLException {
        Connection connection = transaction.getConnection();
        try (PreparedStatement preparedStatement = getPreparedStatement(connection, sql, params)) {
            int updateCount = preparedStatement.executeUpdate();
            LogUtil.logIfDebugEnabled(log, log -> log.debug("\r\n<== affected rows: {}", updateCount));
            return updateCount;
        } catch (SQLException e) {
            transaction.rollback();
            log.error("execute SQL statement error: {} --> parameters: {}", sql, params == null ? null : Arrays.stream(params).map(MethodParameter::getValue).collect(Collectors.toList()));
            throw e;
        } finally {
            commitTransactionIfNecessary(transaction);
        }
    }

    public static PreparedStatement getPreparedStatement(Connection connection, String sql, MethodParameter... params) throws SQLException {
        return getPreparedStatement(connection, sql, JdbcUtil::preparedStatement, params);
    }

    @SuppressWarnings("unchecked")
    public static <T> PreparedStatement getPreparedStatement(Connection connection, String sql, BiFunction<Connection, String, PreparedStatement> preparedStatementFactory, MethodParameter... params) throws SQLException {
        PreparedStatement preparedStatement = preparedStatementFactory.apply(connection, sql);
        for (int i = 0; params != null && i < params.length; i++) {
            MethodParameter parameter = params[i];
            TypeHandler<T> typeHandler = (TypeHandler<T>) ResultSetUtil.getTypeHandler(parameter.getParamType());
            if (typeHandler != null) {
                typeHandler.setParameter(preparedStatement, i + 1, (T) parameter.getValue());
                continue;
            }
            preparedStatement.setObject(i + 1, parameter.getValue());
        }
        if (log.isDebugEnabled()) {
            log.debug("\r\n==>     preparing: {}", sql);
            log.debug("\r\n==>    parameters: {}", params == null ? null : Arrays.stream(params).map(MethodParameter::getValue).collect(Collectors.toList()));
        }
        return preparedStatement;
    }

    public static PreparedStatement preparedStatement(Connection connection, String sql) {
        try {
            return connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public static PreparedStatement preparedStatement(Connection connection, String sql, int autoGeneratedKeys) {
        try {
            return connection.prepareStatement(sql, autoGeneratedKeys);
        } catch (SQLException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public static void commitTransactionIfNecessary(Transaction transaction) throws SQLException {
        if (transaction.isAutoCommit()) {
            transaction.commit();
            IOUtil.close(transaction);
        }
    }
}
