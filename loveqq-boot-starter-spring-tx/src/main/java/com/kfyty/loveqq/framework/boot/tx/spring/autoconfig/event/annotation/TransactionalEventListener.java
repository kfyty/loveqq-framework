package com.kfyty.loveqq.framework.boot.tx.spring.autoconfig.event.annotation;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.EventListener;
import com.kfyty.loveqq.framework.core.event.ApplicationEvent;
import com.kfyty.loveqq.framework.core.lang.annotation.AliasFor;
import org.springframework.transaction.event.TransactionPhase;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 事务事件监听器，用于在事务中发布的事件，期望事务到达某个阶段后执行
 *
 * @author kfyty725
 * @date 2021/6/21 16:45
 * @email kfyty725@hotmail.com
 */
@Documented
@EventListener
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface TransactionalEventListener {
    /**
     * 要监听的事件
     */
    @AliasFor(annotation = EventListener.class)
    Class<? extends ApplicationEvent<?>>[] value() default {};

    /**
     * 事件执行的事务阶段，默认是提交之后
     *
     * @see TransactionPhase
     */
    TransactionPhase phase() default TransactionPhase.AFTER_COMMIT;

    /**
     * 如果不在事务中，事务要继续执行事件监听
     *
     * @return true/false
     */
    boolean continueIfNoTransaction() default false;
}
