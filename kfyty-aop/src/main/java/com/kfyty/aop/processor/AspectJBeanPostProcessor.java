package com.kfyty.aop.processor;

import com.kfyty.aop.Advisor;
import com.kfyty.aop.MethodMatcher;
import com.kfyty.aop.PointcutAdvisor;
import com.kfyty.aop.aspectj.creator.AdvisorCreator;
import com.kfyty.aop.proxy.AspectMethodInterceptorProxy;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.proxy.AbstractProxyCreatorProcessor;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 描述: 切面处理器
 *
 * @author kfyty725
 * @date 2021/7/29 13:07
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Component
public class AspectJBeanPostProcessor extends AbstractProxyCreatorProcessor {
    /**
     * 切面
     */
    private List<Advisor> aspectAdvisor;

    /**
     * advisor 创建器
     */
    private AdvisorCreator advisorCreator;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        super.setApplicationContext(applicationContext);
        this.advisorCreator = applicationContext.getBean(AdvisorCreator.class);
    }

    @Override
    public Object postProcessAfterInstantiation(Object bean, String beanName) {
        Class<?> beanClass = this.applicationContext.getBeanDefinition(beanName).getBeanType();
        List<Advisor> advisors = this.findAdvisor(beanClass);
        if (CommonUtil.empty(advisors)) {
            return null;
        }
        return this.createProxy(bean, beanName, new AspectMethodInterceptorProxy(advisors));
    }

    protected List<Advisor> findAdvisor(Class<?> beanClass) {
        this.prepareAspectJAnnotationAdvisor();
        List<Advisor> advisors = new ArrayList<>();
        List<Method> methods = ReflectUtil.getMethods(beanClass);
        for (Advisor advisor : this.aspectAdvisor) {
            if (advisor instanceof PointcutAdvisor) {
                MethodMatcher methodMatcher = ((PointcutAdvisor) advisor).getPointcut().getMethodMatcher();
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

    protected void prepareAspectJAnnotationAdvisor() {
        if (this.aspectAdvisor != null) {
            return;
        }
        this.aspectAdvisor = new ArrayList<>();
        List<BeanDefinition> aspectJAnnotationBeanDefinition = this.applicationContext.getBeanDefinitionWithAnnotation(Aspect.class).values().stream().sorted(Comparator.comparing(BeanUtil::getBeanOrder)).collect(Collectors.toList());
        for (BeanDefinition beanDefinition : aspectJAnnotationBeanDefinition) {
            Pair<String, Class<?>> namesAspectClass = new Pair<>(beanDefinition.getBeanName(), beanDefinition.getBeanType());
            List<Advisor> advisors = this.advisorCreator.createAdvisor(e -> this.applicationContext.getBean(e.getAspectName()), namesAspectClass);
            this.aspectAdvisor.addAll(advisors);
        }
    }
}
