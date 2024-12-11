package com.kfyty.loveqq.framework.boot.tx.spring.autoconfig;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.function.Consumer;

/**
 * 描述: 事务工具
 *
 * @author kfyty725
 * @date 2021/6/21 16:45
 * @email kfyty725@hotmail.com
 */
public abstract class TransactionUtils {
    /**
     * 当前线程是否激活了事务
     *
     * @return true/false
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isTransactionActive() {
        return TransactionSynchronizationManager.isSynchronizationActive() && TransactionSynchronizationManager.isActualTransactionActive();
    }

    /**
     * 注册事务提交前的回调
     *
     * @param runnable 回调
     */
    public static void registerBeforeCommit(Runnable runnable) {
        if (!isTransactionActive()) {
            throw new ResolvableException("No Transaction is active");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void beforeCommit(boolean readOnly) {
                runnable.run();
            }
        });
    }

    /**
     * 注册事务提交后的回调
     *
     * @param runnable 回调
     */
    public static void registerAfterCommit(Runnable runnable) {
        if (!isTransactionActive()) {
            throw new ResolvableException("No Transaction is active");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                runnable.run();
            }
        });
    }

    /**
     * 注册事务回滚后的回调
     *
     * @param runnable 回调
     */
    public static void registerAfterRollback(Runnable runnable) {
        if (!isTransactionActive()) {
            throw new ResolvableException("No Transaction is active");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK || status == TransactionSynchronization.STATUS_UNKNOWN) {
                    runnable.run();
                }
            }
        });
    }

    /**
     * 注册事务完成后的回调，即提交/回滚后的回调
     *
     * @param consumer 回调
     */
    public static void registerAfterCompletion(Consumer<Integer> consumer) {
        if (!isTransactionActive()) {
            throw new ResolvableException("No Transaction is active");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                consumer.accept(status);
            }
        });
    }
}
