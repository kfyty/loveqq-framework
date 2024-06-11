package com.kfyty.loveqq.framework.boot.data.orm.mybatis.autoconfig.adapter;

import com.kfyty.loveqq.framework.core.jdbc.transaction.Transaction;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 描述: mybatis 事务适配器
 *
 * @author kfyty725
 * @date 2024/6/03 18:55
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class MybatisTransactionAdapter implements org.apache.ibatis.transaction.Transaction {
    private final Transaction transaction;

    @Override
    public Connection getConnection() throws SQLException {
        return this.transaction.getConnection();
    }

    @Override
    public void commit() throws SQLException {
        this.transaction.commit();
    }

    @Override
    public void rollback() throws SQLException {
        this.transaction.rollback();
    }

    @Override
    public void close() throws SQLException {
        this.transaction.close();
    }

    @Override
    public Integer getTimeout() throws SQLException {
        return this.transaction.getTimeout();
    }
}
