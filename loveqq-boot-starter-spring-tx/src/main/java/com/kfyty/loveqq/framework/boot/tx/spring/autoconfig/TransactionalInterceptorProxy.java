package com.kfyty.loveqq.framework.boot.tx.spring.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChain;
import com.kfyty.loveqq.framework.core.proxy.MethodProxy;
import com.kfyty.loveqq.framework.core.proxy.aop.AdviceMethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.proxy.aop.MethodAroundAdvice;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 描述: 事务拦截代理
 *
 * @author kfyty725
 * @date 2021/7/29 13:07
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class TransactionalInterceptorProxy implements AdviceMethodInterceptorChainPoint {
    private final BeanFactory beanFactory;

    @Override
    public Class<? extends MethodInterceptor> getAdviceType() {
        return MethodAroundAdvice.class;
    }

    @Override
    public int getOrder() {
        return Order.LOWEST_PRECEDENCE;
    }

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        Transactional transactional = this.obtainTransactional(methodProxy.getTargetMethod());
        TransactionAttribute transactionAttribute = this.resolveTransactionAttribute(transactional);
        if (transactionAttribute == null) {
            return chain.proceed(methodProxy);
        }
        PlatformTransactionManager platformTransactionManager = this.obtainPlatformTransactionManager(transactional);
        TransactionStatus transaction = platformTransactionManager.getTransaction(transactionAttribute);
        try {
            Object retValue = chain.proceed(methodProxy);
            platformTransactionManager.commit(transaction);
            return retValue;
        } catch (Throwable e) {
            this.completeTransactionAfterThrowing(transaction, transactionAttribute, platformTransactionManager, e, transactional);
            throw e;
        }
    }

    protected Transactional obtainTransactional(Method method) {
        Transactional annotation = AnnotationUtil.findAnnotation(method, Transactional.class);
        if (annotation == null) {
            annotation = AnnotationUtil.findAnnotation(method.getDeclaringClass(), Transactional.class);
        }
        return annotation;
    }

    protected TransactionAttribute resolveTransactionAttribute(Transactional transactional) {
        if (transactional == null) {
            return null;
        }
        DefaultTransactionAttribute transactionAttribute = new DefaultTransactionAttribute();
        transactionAttribute.setIsolationLevel(transactional.isolation().value());
        transactionAttribute.setPropagationBehavior(transactional.propagation().value());
        transactionAttribute.setReadOnly(transactional.readOnly());
        transactionAttribute.setTimeout(transactional.timeout());
        return transactionAttribute;
    }

    protected PlatformTransactionManager obtainPlatformTransactionManager(Transactional transactional) {
        String beanName = transactional.value();
        if (CommonUtil.empty(beanName)) {
            beanName = transactional.transactionManager();
        }
        Object bean = CommonUtil.notEmpty(beanName) ? this.beanFactory.getBean(beanName) : this.beanFactory.getBean(PlatformTransactionManager.class);
        if (bean == null) {
            throw new IllegalArgumentException("The PlatformTransactionManager does not exists !");
        }
        return (PlatformTransactionManager) bean;
    }

    protected void completeTransactionAfterThrowing(TransactionStatus transaction, TransactionAttribute ta, PlatformTransactionManager tm, Throwable ex, Transactional transactional) {
        Boolean rollback = this.rollbackFor(ex, transactional);
        if (rollback != null && rollback || rollback == null && ta.rollbackOn(ex)) {
            tm.rollback(transaction);
        }
    }

    protected Boolean rollbackFor(Throwable ex, Transactional transactional) {
        Class<?> exClass = ex.getClass();
        String exName = exClass.getName();
        if (CommonUtil.notEmpty(transactional.rollbackFor())) {
            return Arrays.stream(transactional.rollbackFor()).anyMatch(e -> e.isAssignableFrom(exClass));
        }
        if (CommonUtil.notEmpty(transactional.rollbackForClassName())) {
            return Arrays.stream(transactional.rollbackForClassName()).anyMatch(e -> e.equals(exName));
        }
        if (CommonUtil.notEmpty(transactional.noRollbackFor())) {
            return Arrays.stream(transactional.noRollbackFor()).noneMatch(e -> e.isAssignableFrom(exClass));
        }
        if (CommonUtil.notEmpty(transactional.noRollbackForClassName())) {
            return Arrays.stream(transactional.noRollbackForClassName()).noneMatch(e -> e.equals(exName));
        }
        return null;
    }
}
