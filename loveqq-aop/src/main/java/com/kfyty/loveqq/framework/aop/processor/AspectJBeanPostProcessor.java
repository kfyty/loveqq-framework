package com.kfyty.loveqq.framework.aop.processor;

import com.kfyty.loveqq.framework.aop.Advisor;
import com.kfyty.loveqq.framework.aop.MethodMatcher;
import com.kfyty.loveqq.framework.aop.PointcutAdvisor;
import com.kfyty.loveqq.framework.aop.aspectj.AbstractAspectJAdvice;
import com.kfyty.loveqq.framework.aop.aspectj.AspectClass;
import com.kfyty.loveqq.framework.aop.aspectj.AspectJFactory;
import com.kfyty.loveqq.framework.aop.aspectj.adapter.AdviceInterceptorPointAdapter;
import com.kfyty.loveqq.framework.aop.aspectj.creator.AdvisorCreator;
import com.kfyty.loveqq.framework.aop.proxy.AspectMethodInterceptorProxy;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.AspectResolve;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.proxy.AbstractProxyCreatorProcessor;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 描述: 切面处理器
 * <p>
 * 只要被注解了 {@link AspectResolve} 的组件，才会被解析，目的是避免解析过多的无用组件
 *
 * @author kfyty725
 * @date 2021/7/29 13:07
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Component
public class AspectJBeanPostProcessor extends AbstractProxyCreatorProcessor {
    /**
     * 是否已解析切面配置
     */
    private boolean hasResolveAspect;

    /**
     * advisor 创建器
     */
    @Autowired
    private AdvisorCreator advisorCreator;

    /**
     * 切面
     */
    @Autowired(required = false)
    private List<Advisor> aspectAdvisor;

    /**
     * 通知适配器
     */
    @Autowired(required = false)
    private List<AdviceInterceptorPointAdapter> adviceInterceptorPointAdapters;

    @Override
    public Object postProcessAfterInstantiation(Object bean, String beanName, BeanDefinition beanDefinition) {
        Class<?> beanClass = beanDefinition.getBeanType();
        AspectResolve aspectResolve = AnnotationUtil.findAnnotation(beanClass, AspectResolve.class);
        if (!beanDefinition.isAutowireCandidate() || aspectResolve == null || !aspectResolve.value()) {
            return null;
        }
        List<Advisor> advisors = this.findAvailableAdvisor(beanClass);
        if (advisors.isEmpty()) {
            return null;
        }
        return this.createProxy(bean, beanDefinition, new AspectMethodInterceptorProxy(advisors, this.adviceInterceptorPointAdapters));
    }

    @Override
    public MethodInterceptorChainPoint createProxyPoint() {
        throw new UnsupportedOperationException("AspectJBeanPostProcessor.createProxyPoint");
    }

    /**
     * 查找该 bean class 中是否有方法适合 Advisor
     * 查找到存在一个方法适合即可返回，在代理中再搜索全部
     *
     * @param beanClass bean class
     * @return Advisor
     */
    protected List<Advisor> findAvailableAdvisor(Class<?> beanClass) {
        this.prepareAspectJAnnotationAdvisor();
        List<Advisor> advisors = new ArrayList<>();
        Method[] methods = ReflectUtil.getMethods(beanClass);
        for (Advisor advisor : this.aspectAdvisor) {
            if (advisor instanceof PointcutAdvisor pointcutAdvisor) {
                MethodMatcher methodMatcher = pointcutAdvisor.getPointcut().getMethodMatcher();
                for (Method targetMethod : methods) {
                    if (methodMatcher.matches(targetMethod, beanClass)) {
                        advisors.add(advisor);
                        break;
                    }
                }
            }
        }
        return advisors;
    }

    /**
     * 解析 @Aspect 注解的 Bean 为 Advisor
     */
    protected void prepareAspectJAnnotationAdvisor() {
        if (this.hasResolveAspect) {
            return;
        }
        this.hasResolveAspect = true;
        AspectJFactory aspectJFactory = advice -> this.applicationContext.getBean(((AbstractAspectJAdvice) advice).getAspectName());
        Collection<BeanDefinition> aspectJAnnotationBeanDefinition = this.applicationContext.getBeanDefinitionWithAnnotation(Aspect.class, true).values();
        for (BeanDefinition beanDefinition : aspectJAnnotationBeanDefinition) {
            AspectClass aspectClass = new AspectClass(beanDefinition.getBeanName(), beanDefinition.getBeanType());
            this.aspectAdvisor.addAll(this.advisorCreator.createAdvisor(aspectJFactory, aspectClass));
        }
    }
}
