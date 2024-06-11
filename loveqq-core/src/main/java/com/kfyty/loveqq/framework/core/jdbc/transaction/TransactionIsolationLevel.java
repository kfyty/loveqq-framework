package com.kfyty.loveqq.framework.core.jdbc.transaction;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Connection;

/**
 * 描述: 事务隔离级别
 *
 * @author kfyty725
 * @date 2021/9/16 12:15
 * @email kfyty725@hotmail.com
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TransactionIsolationLevel {
    NONE(Connection.TRANSACTION_NONE),
    READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
    READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
    REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
    SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE),
    ;

    private final int level;
}
