package com.kfyty.core.jdbc.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 描述: 事务支持接口
 *
 * @author kfyty725
 * @date 2021/5/19 9:23
 * @email kfyty725@hotmail.com
 */
public interface Transaction extends AutoCloseable {
    /**
     * 获取一个连接
     *
     * @return Connection
     */
    Connection getConnection() throws SQLException;

    /**
     * 如果当前连接不是自动提交，则提交事务
     */
    void commit() throws SQLException;

    /**
     * 如果当前连接不是自动提交则回滚事务
     */
    void rollback() throws SQLException;

    /**
     * 关闭当前事务，并复位连接
     */
    void close() throws SQLException;

    /**
     * 当前连接是否为自动提交，默认 true
     *
     * @return boolean
     */
    boolean isAutoCommit() throws SQLException;

    /**
     * 设置当前连接的自动提交状态，可设置为 false 以支持编程式事务
     *
     * @param autoCommit 是否自动提交
     */
    void setAutoCommit(boolean autoCommit) throws SQLException;

    /**
     * 返回超时时间
     *
     * @return 超时时间
     */
    default Integer getTimeout() {
        return null;
    }
}
