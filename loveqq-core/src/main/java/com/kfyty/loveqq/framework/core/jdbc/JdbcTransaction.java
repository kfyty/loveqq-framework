package com.kfyty.loveqq.framework.core.jdbc;

import com.kfyty.loveqq.framework.core.jdbc.transaction.Transaction;
import com.kfyty.loveqq.framework.core.jdbc.transaction.TransactionIsolationLevel;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 描述: jdbc 事务实现
 *
 * @author kfyty725
 * @date 2021/5/19 9:26
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class JdbcTransaction implements Transaction {
    protected final DataSource dataSource;
    protected final TransactionIsolationLevel level;

    protected Connection connection;
    protected boolean autoCommit;

    public JdbcTransaction(DataSource dataSource) {
        this(dataSource, null);
    }

    public JdbcTransaction(DataSource dataSource, TransactionIsolationLevel level) {
        this.dataSource = dataSource;
        this.level = level;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (this.connection != null) {
            return this.connection;
        }
        return this.openConnection();
    }

    @Override
    public void commit() throws SQLException {
        if (this.connection != null && !this.isAutoCommit()) {
            if (log.isDebugEnabled()) {
                log.debug("Committing JDBC Connection [" + this.connection + "]");
            }
            this.connection.commit();
        }
    }

    @Override
    public void rollback() throws SQLException {
        if (this.connection != null && !this.isAutoCommit()) {
            if (log.isDebugEnabled()) {
                log.debug("Rolling back JDBC Connection [" + this.connection + "]");
            }
            this.connection.rollback();
        }
    }

    @Override
    public void close() throws SQLException {
        if (this.connection != null) {
            this.connection.setAutoCommit(this.autoCommit);
            if (log.isDebugEnabled()) {
                log.debug("Closing JDBC Connection [" + this.connection + "]");
            }
            this.connection.close();
            this.connection = null;
        }
    }

    @Override
    public boolean isAutoCommit() throws SQLException {
        return this.connection.getAutoCommit();
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        if (this.connection == null) {
            this.openConnection();
        }
        this.connection.setAutoCommit(autoCommit);
    }

    protected Connection openConnection() throws SQLException {
        this.connection = this.dataSource.getConnection();
        this.autoCommit = this.connection.getAutoCommit();
        if (this.level != null) {
            this.connection.setTransactionIsolation(this.level.getLevel());
        }
        return this.connection;
    }
}
