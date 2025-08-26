package com.kfyty.loveqq.framework.boot.tx.spring.autoconfig.event;

import com.kfyty.loveqq.framework.boot.tx.spring.autoconfig.TransactionUtils;
import com.kfyty.loveqq.framework.boot.tx.spring.autoconfig.event.annotation.TransactionalEventListener;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.event.ApplicationEvent;
import com.kfyty.loveqq.framework.core.event.ApplicationListener;
import com.kfyty.loveqq.framework.core.event.EventListenerAdapter;
import com.kfyty.loveqq.framework.core.event.EventListenerAnnotationListener;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import lombok.RequiredArgsConstructor;

/**
 * 描述: 事务事件监听器适配器
 *
 * @author kfyty725
 * @date 2021/6/21 16:45
 * @email kfyty725@hotmail.com
 */
@Component
public class TransactionEventListenerAdapter implements EventListenerAdapter {

    @Override
    public ApplicationListener<?> adapt(ApplicationListener<ApplicationEvent<?>> source, EventListenerAnnotationListener listener) {
        if (listener.getListenerMethod() != null) {
            TransactionalEventListener annotation = AnnotationUtil.findAnnotation(listener.getListenerMethod(), TransactionalEventListener.class);
            if (annotation != null) {
                return new TransactionEventListener(annotation, listener);
            }
        }
        return listener;
    }

    @RequiredArgsConstructor
    public static class TransactionEventListener implements ApplicationListener<ApplicationEvent<Object>> {
        /**
         * 事务监听器注解
         */
        protected final TransactionalEventListener annotation;

        /**
         * 实际监听器
         */
        protected final ApplicationListener<ApplicationEvent<?>> listener;

        @Override
        public void onApplicationEvent(ApplicationEvent<Object> event) {
            if (!TransactionUtils.isTransactionActive()) {
                if (this.annotation.continueIfNoTransaction()) {
                    this.listener.onApplicationEvent(event);
                } else {
                    throw new ResolvableException("No Transaction is active");
                }
            }
            switch (this.annotation.phase()) {
                case BEFORE_COMMIT:
                    TransactionUtils.registerBeforeCommit(() -> this.listener.onApplicationEvent(event));
                    break;
                case AFTER_COMMIT:
                    TransactionUtils.registerAfterCommit(() -> this.listener.onApplicationEvent(event));
                    break;
                case AFTER_ROLLBACK:
                    TransactionUtils.registerAfterRollback(() -> this.listener.onApplicationEvent(event));
                    break;
                case AFTER_COMPLETION:
                    TransactionUtils.registerAfterCompletion(status -> this.listener.onApplicationEvent(event));
                    break;
            }
        }
    }
}
