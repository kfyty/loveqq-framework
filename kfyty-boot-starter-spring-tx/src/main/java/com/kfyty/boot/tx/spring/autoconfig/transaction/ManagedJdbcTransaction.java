package com.kfyty.boot.tx.spring.autoconfig.transaction;

import com.kfyty.core.jdbc.JdbcTransaction;
import com.kfyty.core.jdbc.transaction.TransactionIsolationLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 描述: ioc 管理的事务实现
 *
 * @author kfyty725
 * @date 2021/5/19 9:26
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class ManagedJdbcTransaction extends JdbcTransaction {
    private boolean isConnectionTransactional;

    public ManagedJdbcTransaction(DataSource dataSource) {
        super(dataSource);
    }

    public ManagedJdbcTransaction(DataSource dataSource, TransactionIsolationLevel level) {
        super(dataSource, level);
    }

    @Override
    public void commit() throws SQLException {
        if (this.connection != null && !this.isConnectionTransactional && !this.autoCommit) {
            log.debug("Committing JDBC Connection [" + this.connection + "]");
            this.connection.commit();
        }
    }

    @Override
    public void rollback() throws SQLException {
        if (this.connection != null && !this.isConnectionTransactional && !this.autoCommit) {
            log.debug("Rolling back JDBC Connection [" + this.connection + "]");
            this.connection.rollback();
        }
    }

    @Override
    public Integer getTimeout() {
        ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
        if (holder != null && holder.hasTimeout()) {
            return holder.getTimeToLiveInSeconds();
        }
        return null;
    }

    @Override
    public void close() throws SQLException {
        DataSourceUtils.releaseConnection(this.connection, this.dataSource);
    }

    @Override
    protected Connection openConnection() throws SQLException {
        this.connection = DataSourceUtils.getConnection(this.dataSource);
        this.autoCommit = this.connection.getAutoCommit();
        this.isConnectionTransactional = DataSourceUtils.isConnectionTransactional(this.connection, this.dataSource);
        log.debug("JDBC Connection [" + this.connection + "] will" + (this.isConnectionTransactional ? " " : " not ") + "be managed by K");
        return this.connection;
    }
}
