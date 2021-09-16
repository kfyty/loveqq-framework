package com.kfyty.support.jdbc;

import com.kfyty.support.transaction.Transaction;
import lombok.extern.slf4j.Slf4j;

/**
 * 描述: jdbc transaction holder
 *
 * @author kfyty725
 * @date 2021/8/8 12:05
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class TransactionHolder {
    private static final ThreadLocal<Transaction> CURRENT_TRANSACTION = new ThreadLocal<>();

    public static void setCurrentTransaction(Transaction transaction) {
        CURRENT_TRANSACTION.set(transaction);
    }

    public static Transaction currentTransaction() {
        Transaction transaction = CURRENT_TRANSACTION.get();
        if (transaction == null) {
            throw new IllegalStateException("the current thread is not bound to the transaction !");
        }
        return transaction;
    }

    public static void removeCurrentTransaction() {
        try {
            CURRENT_TRANSACTION.remove();
        } catch (Exception e) {
            log.warn("remove current transaction failed !", e);
        }
    }
}
