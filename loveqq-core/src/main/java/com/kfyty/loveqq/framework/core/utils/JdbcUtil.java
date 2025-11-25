package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import com.kfyty.loveqq.framework.core.jdbc.JdbcTransaction;
import com.kfyty.loveqq.framework.core.jdbc.transaction.Transaction;
import com.kfyty.loveqq.framework.core.jdbc.type.TypeHandler;
import com.kfyty.loveqq.framework.core.reflect.DefaultParameterizedType;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

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

    @SuppressWarnings("unchecked")
    public static <T> List<T> queryList(DataSource dataSource, Class<T> returnType, String sql, Object... params) throws SQLException {
        return (List<T>) query(dataSource, SimpleGeneric.from(new DefaultParameterizedType(List.class, returnType)), sql, params);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> queryMap(DataSource dataSource, Class<K> keyType, Class<V> valueType, String sql, Object... params) throws SQLException {
        return (Map<K, V>) query(dataSource, SimpleGeneric.from(new DefaultParameterizedType(Map.class, keyType, valueType)), sql, params);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List<Map<Object, Object>> queryListMap(DataSource dataSource, String sql, Object... params) throws SQLException {
        return (List) queryList(dataSource, Map.class, sql, params);
    }

    public static Object query(DataSource dataSource, SimpleGeneric returnType, String sql, Object... params) throws SQLException {
        return query(new JdbcTransaction(dataSource), returnType, sql, params);
    }

    public static Object query(Transaction transaction, SimpleGeneric returnType, String sql, Object... params) throws SQLException {
        Connection connection = transaction.getConnection();
        try (PreparedStatement preparedStatement = getPreparedStatement(connection, sql, params);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            Object obj = ResultSetUtil.processObject(resultSet, returnType);
            LogUtil.logIfDebugEnabled(log, log -> log.debug("\t\t<==         total: {} {}", CommonUtil.size(obj), obj == null ? null : obj.getClass()));
            return obj;
        } catch (SQLException e) {
            transaction.rollback();
            log.error("Execute SQL Statement Error: {} --> parameters: {}", sql, Arrays.toString(params));
            throw e;
        } finally {
            commitTransactionIfNecessary(transaction);
        }
    }

    public static int execute(DataSource dataSource, String sql, Object... params) throws SQLException {
        return execute(new JdbcTransaction(dataSource), sql, params);
    }

    public static int execute(Transaction transaction, String sql, Object... params) throws SQLException {
        try {
            return execute(transaction, getPreparedStatement(transaction.getConnection(), sql, params));
        } catch (SQLException e) {
            transaction.rollback();
            log.error("Execute SQL Statement error: {} --> parameters: {}", sql, Arrays.toString(params));
            throw e;
        }
    }

    public static int execute(Transaction transaction, PreparedStatement preparedStatement) throws SQLException {
        try (PreparedStatement ps = preparedStatement) {
            int[] updateCount = ps.executeBatch();
            LogUtil.logIfDebugEnabled(log, log -> log.debug("\t\t<== affected rows: {}", updateCount));
            return Arrays.stream(updateCount).sum();
        } finally {
            commitTransactionIfNecessary(transaction);
        }
    }

    public static PreparedStatement getPreparedStatement(Connection connection, String sql, Object... params) throws SQLException {
        return getPreparedStatement(connection, sql, JdbcUtil::preparedStatement, params);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static PreparedStatement getPreparedStatement(Connection connection, String sql, BiFunction<Connection, String, PreparedStatement> preparedStatementFactory, Object... params) throws SQLException {
        PreparedStatement preparedStatement = preparedStatementFactory.apply(connection, sql);

        for (int i = 0; params != null && i < params.length; i++) {
            Object parameter = params[i];

            if (parameter == null) {
                preparedStatement.setNull(i + 1, Types.OTHER);
                continue;
            }

            TypeHandler typeHandler = ResultSetUtil.getTypeHandler(parameter.getClass());

            if (typeHandler != null) {
                typeHandler.setParameter(preparedStatement, i + 1, parameter);
                continue;
            }

            preparedStatement.setObject(i + 1, parameter);
        }

        preparedStatement.addBatch();

        if (log.isDebugEnabled()) {
            log.debug("\t\t==>     preparing: {}", sql);
            log.debug("\t\t==>    parameters: {}", Arrays.toString(params));
        }

        return preparedStatement;
    }

    public static PreparedStatement getPreparedStatement(Connection connection, String sql, BiFunction<Connection, String, PreparedStatement> preparedStatementFactory, Object[][] params) throws SQLException {
        PreparedStatement preparedStatement = preparedStatementFactory.apply(connection, sql);
        for (Object[] param : params) {
            getPreparedStatement(connection, sql, (c, s) -> preparedStatement, param);
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
