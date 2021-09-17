package com.kfyty.database.jdbc.intercept;

import com.kfyty.support.generic.SimpleGeneric;
import com.kfyty.support.jdbc.TransactionHolder;
import com.kfyty.support.method.MethodParameter;
import com.kfyty.support.transaction.Transaction;
import com.kfyty.support.utils.JdbcUtil;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.kfyty.support.utils.JdbcUtil.commitTransactionIfNecessary;

/**
 * 描述: SQL 执行拦截器
 * 执行顺序由 {@link com.kfyty.support.autoconfig.annotation.Order}  控制
 *
 * @author kfyty725
 * @date 2021/8/8 10:40
 * @email kfyty725@hotmail.com
 */
public interface Interceptor {
    /**
     * SQL 拦截方法
     *
     * @param sql         经过处理的 SQL 语句
     * @param transaction 事务
     * @param returnType  返回值类型
     * @param params      参数
     * @return 返回值不为空时，将作为返回结果，否则执行下一个拦截器
     */
    default Object intercept(String sql, Transaction transaction, SimpleGeneric returnType, MethodParameter... params) throws SQLException {
        try {
            return this.intercept(sql, returnType, params);
        } catch (SQLException e) {
            transaction.rollback();
            throw e;
        } finally {
            commitTransactionIfNecessary(transaction);
        }
    }

    default Object intercept(String sql, SimpleGeneric returnType, MethodParameter... params) throws SQLException {
        try (PreparedStatement ps = JdbcUtil.getPreparedStatement(TransactionHolder.currentTransaction().getConnection(), sql, params)) {
            return this.intercept(ps, returnType, params);
        }
    }

    default Object intercept(PreparedStatement ps, SimpleGeneric returnType, MethodParameter... params) throws SQLException {
        return null;
    }
}
