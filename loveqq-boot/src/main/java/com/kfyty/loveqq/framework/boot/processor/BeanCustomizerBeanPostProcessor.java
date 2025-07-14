package com.kfyty.loveqq.framework.boot.processor;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.BeanCustomizer;
import com.kfyty.loveqq.framework.core.autoconfig.BeanPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.AopUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 描述:  bean 初始化之后进行自定义配置
 *
 * @author kfyty725
 * @date 2021/6/7 17:14
 * @email kfyty725@hotmail.com
 */
@Component
@Order(Order.HIGHEST_PRECEDENCE)
public class BeanCustomizerBeanPostProcessor implements ApplicationContextAware, BeanPostProcessor {
    /**
     * 应用上下文
     */
    protected ApplicationContext applicationContext;

    /**
     * bean 自定义
     * 使用数组是为了减少内存占用
     */
    protected volatile Pair<Class<?>, BeanCustomizer<Object>[]>[] beanCustomizers;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        this.prepareBeanCustomizers();
        Class<?> targetClass = AopUtil.getTargetClass(bean);
        for (Pair<Class<?>, BeanCustomizer<Object>[]> entry : this.beanCustomizers) {
            if (entry.getKey().isAssignableFrom(targetClass)) {
                for (BeanCustomizer<Object> customizer : entry.getValue()) {
                    customizer.customize(beanName, bean);
                }
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void prepareBeanCustomizers() {
        if (this.beanCustomizers == null) {
            synchronized (this) {
                if (this.beanCustomizers == null) {
                    // 先标记非空
                    this.beanCustomizers = new Pair[0];

                    // 解析
                    Map<Class<?>, List<BeanCustomizer<Object>>> beanCustomizerMap = new HashMap<>();
                    for (Map.Entry<String, BeanCustomizer> entry : this.applicationContext.getBeanOfType(BeanCustomizer.class).entrySet()) {
                        BeanDefinition beanDefinition = this.applicationContext.getBeanDefinition(entry.getKey());
                        if (!beanDefinition.isMethodBean()) {
                            beanCustomizerMap.computeIfAbsent(SimpleGeneric.from(beanDefinition.getBeanType()).getFirst().get(), k -> new LinkedList<>()).add(entry.getValue());
                            continue;
                        }
                        Method beanMethod = beanDefinition.getBeanMethod();
                        SimpleGeneric generic = BeanCustomizer.class == beanMethod.getReturnType() ? SimpleGeneric.from(beanMethod) : SimpleGeneric.from(beanMethod.getReturnType());
                        beanCustomizerMap.computeIfAbsent(generic.getFirst().get(), k -> new LinkedList<>()).add(entry.getValue());
                    }

                    // 赋值并清空 Map
                    this.beanCustomizers = beanCustomizerMap.entrySet().stream().map(e -> new Pair<>(e.getKey(), e.getValue().toArray(new BeanCustomizer[0]))).toArray(Pair[]::new);
                    beanCustomizerMap.clear();
                    beanCustomizerMap = null;
                }
            }
        }
    }
}
