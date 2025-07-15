package com.kfyty.loveqq.framework.boot.tx.spring.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChain;
import com.kfyty.loveqq.framework.core.proxy.MethodProxy;
import com.kfyty.loveqq.framework.core.proxy.aop.AdviceMethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.proxy.aop.MethodAroundAdvice;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ExceptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.NoRollbackRuleAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 描述: 事务拦截代理
 *
 * @author kfyty725
 * @date 2021/7/29 13:07
 * @email kfyty725@hotmail.com
 */
@Slf4j
@RequiredArgsConstructor
public class TransactionalInterceptorProxy implements AdviceMethodInterceptorChainPoint {
    /**
     * bean factory
     */
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
        TransactionAttribute transactionAttribute = this.obtainTransactionAttribute(transactional);
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
            Throwable unwrap = ExceptionUtil.unwrap(e);
            this.completeTransactionAfterThrowing(transaction, transactionAttribute, platformTransactionManager, unwrap);
            throw unwrap;
        }
    }

    protected Transactional obtainTransactional(Method method) {
        Transactional annotation = AnnotationUtil.findAnnotation(method, Transactional.class);
        if (annotation == null) {
            annotation = AnnotationUtil.findAnnotation(method.getDeclaringClass(), Transactional.class);
        }
        return annotation;
    }

    protected TransactionAttribute obtainTransactionAttribute(Transactional transactional) {
        if (transactional == null) {
            return null;
        }

        List<RollbackRuleAttribute> rollbackRuleAttributes = new LinkedList<>();
        for (Class<? extends Throwable> clazz : transactional.rollbackFor()) {
            rollbackRuleAttributes.add(new RollbackRuleAttribute(clazz));
        }
        for (String className : transactional.rollbackForClassName()) {
            rollbackRuleAttributes.add(new RollbackRuleAttribute(className));
        }
        for (Class<? extends Throwable> clazz : transactional.noRollbackFor()) {
            rollbackRuleAttributes.add(new NoRollbackRuleAttribute(clazz));
        }
        for (String className : transactional.noRollbackForClassName()) {
            rollbackRuleAttributes.add(new NoRollbackRuleAttribute(className));
        }

        RuleBasedTransactionAttribute transactionAttribute = new RuleBasedTransactionAttribute();
        transactionAttribute.setIsolationLevel(transactional.isolation().value());
        transactionAttribute.setPropagationBehavior(transactional.propagation().value());
        transactionAttribute.setReadOnly(transactional.readOnly());
        transactionAttribute.setTimeout(transactional.timeout());
        transactionAttribute.setLabels(Arrays.asList(transactional.label()));
        transactionAttribute.setRollbackRules(rollbackRuleAttributes);

        return transactionAttribute;
    }

    protected PlatformTransactionManager obtainPlatformTransactionManager(Transactional transactional) {
        String beanName = transactional.value();
        if (beanName == null || beanName.isEmpty()) {
            beanName = transactional.transactionManager();
        }
        Object bean = CommonUtil.notEmpty(beanName) ? this.beanFactory.getBean(beanName) : this.beanFactory.getBean(PlatformTransactionManager.class);
        if (!(bean instanceof PlatformTransactionManager)) {
            throw new IllegalArgumentException("The PlatformTransactionManager doesn't exists.");
        }
        return (PlatformTransactionManager) bean;
    }

    protected void completeTransactionAfterThrowing(TransactionStatus transaction, TransactionAttribute transactionAttribute, PlatformTransactionManager transactionManager, Throwable ex) {
        if (transactionAttribute.rollbackOn(ex)) {
            try {
                transactionManager.rollback(transaction);
            } catch (TransactionSystemException e) {
                log.error("Application exception overridden by rollback exception", ex);
                e.initApplicationException(ex);
                throw e;
            } catch (RuntimeException | Error throwable) {
                log.error("Application exception overridden by rollback exception", ex);
                throw throwable;
            }
            return;
        }

        // don't roll back on this exception, and roll back if TransactionStatus.isRollbackOnly() is true.
        try {
            transactionManager.commit(transaction);
        } catch (TransactionSystemException e) {
            log.error("Application exception overridden by commit exception", ex);
            e.initApplicationException(ex);
            throw e;
        } catch (RuntimeException | Error throwable) {
            log.error("Application exception overridden by commit exception", ex);
            throw throwable;
        }
    }
}
