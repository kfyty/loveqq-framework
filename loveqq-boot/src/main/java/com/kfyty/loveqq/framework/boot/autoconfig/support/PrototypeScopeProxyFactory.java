package com.kfyty.loveqq.framework.boot.autoconfig.support;

import com.kfyty.loveqq.framework.core.autoconfig.DestroyBean;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.scope.ScopeProxyFactory;
import com.kfyty.loveqq.framework.core.proxy.MethodProxy;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 描述: 原型代理工厂
 *
 * @author kfyty725
 * @date 2022/10/22 10:17
 * @email kfyty725@hotmail.com
 */
public class PrototypeScopeProxyFactory implements ScopeProxyFactory {

    @Override
    public Object getObject(BeanDefinition beanDefinition, BeanFactory beanFactory) {
        return beanFactory.registerBean(beanDefinition);
    }

    /**
     * 基于代理的原型作用域为方法级别，执行完毕后就要销毁 bean
     *
     * @param beanDefinition bean 定义
     * @param beanFactory    bean 工厂
     * @param bean           目标 bean
     * @param methodProxy    执行的代理方法
     */
    @Override
    public void onInvoked(BeanDefinition beanDefinition, BeanFactory beanFactory, Object bean, MethodProxy methodProxy) {
        // 获取执行的目标方法
        final Method method = methodProxy.getTargetMethod();

        // 如果已经是销毁方法，则不再执行，避免销毁多次
        if (Objects.equals(beanDefinition.getDestroyMethod(bean), method)) {
            return;
        }

        // 如果已经是销毁方法，则不再执行，避免销毁多次
        if (bean instanceof DestroyBean) {
            Method destroy = ReflectUtil.getMethod(DestroyBean.class, "destroy");
            if (ReflectUtil.isSuperMethod(destroy, method)) {
                return;
            }
        }

        beanFactory.destroyBean(beanDefinition.getBeanName(), bean);
    }
}
