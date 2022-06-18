package com.kfyty.support.jdbc;

import com.kfyty.support.jdbc.transaction.Transaction;
import com.kfyty.support.jdbc.transaction.TransactionIsolationLevel;
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
    private final DataSource dataSource;
    private final TransactionIsolationLevel level;

    private Connection connection;
    private boolean autoCommit;
    private boolean curAutoCommit;

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
        if (this.connection != null && !this.connection.getAutoCommit()) {
            if (log.isDebugEnabled()) {
                log.debug("Committing JDBC Connection [" + this.connection + "]");
            }
            this.connection.commit();
        }
    }

    @Override
    public void rollback() throws SQLException {
        if (this.connection != null && !this.connection.getAutoCommit()) {
            if (log.isDebugEnabled()) {
                log.debug("Rolling back JDBC Connection [" + this.connection + "]");
            }
            this.connection.rollback();
        }
    }

    @Override
    public void close() throws SQLException {
        if (this.connection != null) {
            this.connection.setAutoCommit(autoCommit);
            if (log.isDebugEnabled()) {
                log.debug("Closing JDBC Connection [" + connection + "]");
            }
            this.connection.close();
            this.connection = null;
        }
    }

    @Override
    public boolean isAutoCommit() {
        return this.curAutoCommit;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        if (this.connection == null) {
            this.openConnection();
        }
        this.connection.setAutoCommit(autoCommit);
        this.curAutoCommit = autoCommit;
    }

    private Connection openConnection() throws SQLException {
        this.connection = dataSource.getConnection();
        this.autoCommit = this.connection.getAutoCommit();
        this.curAutoCommit = this.autoCommit;
        if (this.level != null) {
            this.connection.setTransactionIsolation(this.level.getLevel());
        }
        return this.connection;
    }
}
