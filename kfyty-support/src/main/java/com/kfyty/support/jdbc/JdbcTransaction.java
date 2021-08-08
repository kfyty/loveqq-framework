package com.kfyty.support.jdbc;

import com.kfyty.support.transaction.Transaction;
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

    private Connection connection;
    private boolean autoCommit;
    private boolean curAutoCommit;

    public JdbcTransaction(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if(connection != null) {
            return connection;
        }
        return this.openConnection();
    }

    @Override
    public void commit() throws SQLException {
        if (connection != null && !connection.getAutoCommit()) {
            if (log.isDebugEnabled()) {
                log.debug("Committing JDBC Connection [" + connection + "]");
            }
            connection.commit();
        }
    }

    @Override
    public void rollback() throws SQLException {
        if (connection != null && !connection.getAutoCommit()) {
            if (log.isDebugEnabled()) {
                log.debug("Rolling back JDBC Connection [" + connection + "]");
            }
            connection.rollback();
        }
    }

    @Override
    public void close() throws SQLException {
        if (connection != null) {
            this.connection.setAutoCommit(autoCommit);
            if (log.isDebugEnabled()) {
                log.debug("Closing JDBC Connection [" + connection + "]");
            }
            ConnectionHolder.removeCurrentConnection();
            connection.close();
            connection = null;
        }
    }

    @Override
    public boolean isAutoCommit() {
        return this.curAutoCommit;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        if(this.connection == null) {
            this.openConnection();
        }
        this.connection.setAutoCommit(autoCommit);
        this.curAutoCommit = autoCommit;
    }

    private Connection openConnection() throws SQLException {
        this.connection = dataSource.getConnection();
        this.autoCommit = this.connection.getAutoCommit();
        this.curAutoCommit = this.autoCommit;
        ConnectionHolder.setCurrentConnection(this.connection);
        return this.connection;
    }
}
