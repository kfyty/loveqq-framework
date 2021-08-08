package com.kfyty.support.jdbc;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;

/**
 * 描述: jdbc connection holder
 *
 * @author kfyty725
 * @date 2021/8/8 12:05
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class ConnectionHolder {
    private static final ThreadLocal<Connection> CURRENT_CONNECTION = new ThreadLocal<>();

    public static void setCurrentConnection(Connection connection) {
        CURRENT_CONNECTION.set(connection);
    }

    public static Connection currentConnection() {
        Connection connection = CURRENT_CONNECTION.get();
        if (connection == null) {
            throw new IllegalStateException("the current thread is not bound to the connection !");
        }
        return connection;
    }

    public static void removeCurrentConnection() {
        try {
            CURRENT_CONNECTION.remove();
        } catch (Exception e) {
            log.warn("remove current connection failed !", e);
        }
    }
}
