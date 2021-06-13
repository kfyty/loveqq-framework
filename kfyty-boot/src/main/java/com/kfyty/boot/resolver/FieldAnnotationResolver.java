package com.kfyty.boot.resolver;

import com.kfyty.boot.beans.BeanResources;
import com.kfyty.boot.configuration.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Lazy;
import com.kfyty.support.autoconfig.annotation.Qualifier;
import com.kfyty.support.autoconfig.beans.AutowiredProcessor;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.MethodBeanDefinition;
import com.kfyty.support.jdbc.ReturnType;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * 功能描述: 属性注解解析器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/27 10:43
 * @since JDK 1.8
 */
@Slf4j
public class FieldAnnotationResolver {
    private final ApplicationContext applicationContext;
    private final AutowiredProcessor autowiredProcessor;

    public FieldAnnotationResolver(AnnotationConfigResolver configResolver) {
        this.applicationContext = configResolver.getApplicationContext();
        this.autowiredProcessor = configResolver.getAutowiredProcessor();
    }

    /**
     * 对容器内的所有的 bean 执行属性注入
     */
    public void doResolver() {
        for (Map.Entry<Class<?>, BeanResources> entry : this.applicationContext.getBeanResources().entrySet()) {
            for (Map.Entry<String, Object> beanEntry : entry.getValue().getBeans().entrySet()) {
                this.doResolver(entry.getKey(), beanEntry.getValue());
            }
        }
    }

    /**
     * 对特定 bean 执行属性注入
     * @param clazz bean 的类型
     * @param bean bean 实例
     */
    public void doResolver(Class<?> clazz, Object bean) {
        this.doResolver(clazz, bean, false);
    }

    /**
     * 对特定 bean 执行属性注入
     * 如果容器正在刷新中且要注入的属性实例由该 bean 中的方法定义，则先执行该方法定义
     * @param clazz bean 的类型
     * @param bean bean 实例
     * @param refreshing 当前容器是否正在刷新中
     */
    public void doResolver(Class<?> clazz, Object bean, boolean refreshing) {
        for (Map.Entry<String, Field> entry : ReflectUtil.getFieldMap(clazz).entrySet()) {
            Field field = entry.getValue();
            if(refreshing && field.isAnnotationPresent(Lazy.class)) {
                continue;
            }
            if(refreshing) {
                this.refreshSelfAutowired(clazz, bean, field);
            }
            if(field.isAnnotationPresent(Autowired.class)) {
                this.autowiredProcessor.doAutowired(bean, field);
            }
        }
    }

    /**
     * 刷新注入自身的属性，并提前注册该属性实例所依赖的 bean
     */
    private void refreshSelfAutowired(Class<?> clazz, Object bean, Field field) {
        if(ReflectUtil.getFieldValue(bean, field) != null) {
            return;
        }
        ReturnType<?, ?, ?> type = ReturnType.getReturnType(field);
        for (Method method : clazz.getMethods()) {
            if(!method.isAnnotationPresent(Bean.class) || !type.getActualType().isAssignableFrom(method.getReturnType())) {
                continue;
            }
            for (Parameter parameter : method.getParameters()) {
                String beanName = BeanUtil.getBeanName(parameter.getType(), parameter.getAnnotation(Qualifier.class));
                ReturnType<?, ?, ?> paramType = ReturnType.getReturnType(parameter);
                BeanDefinition beanDefinition = this.applicationContext.getBeanDefinition(beanName, paramType.getActualType());
                if(beanDefinition instanceof MethodBeanDefinition) {
                    BeanDefinition sourceDefinition = ((MethodBeanDefinition) beanDefinition).getSourceDefinition();
                    if(sourceDefinition.getBeanType().equals(clazz)) {
                        for (Parameter nestedParam : ((MethodBeanDefinition) beanDefinition).getBeanMethod().getParameters()) {
                            if(ReturnType.getReturnType(nestedParam).getActualType().isAssignableFrom(field.getType())) {
                                throw new IllegalStateException("bean circular dependency: " + nestedParam);
                            }
                        }
                        this.applicationContext.registerBean(beanDefinition, false);
                    }
                }
            }
            Bean annotation = method.getAnnotation(Bean.class);
            String beanName = CommonUtil.notEmpty(annotation.value()) ? annotation.value() : BeanUtil.convert2BeanName(method.getReturnType());
            BeanDefinition beanDefinition = this.applicationContext.getBeanDefinition(beanName, method.getReturnType());
            if(beanDefinition != null) {
                Object targetBean = this.applicationContext.registerBean(beanDefinition, false);
                if(targetBean == null) {
                    throw new IllegalStateException("the return value of the bean annotation method can't be null !");
                }
                ReflectUtil.setFieldValue(bean, field, targetBean);
            }
        }
    }
}
