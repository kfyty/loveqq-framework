package com.kfyty.support.autoconfig;

import com.kfyty.support.autoconfig.beans.BeanDefinition;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.kfyty.support.autoconfig.beans.builder.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * 描述: 导入自定义的 bean 类型，可以从以扫描的 bean 中过滤得到
 * 实现此接口的 bean 的依赖，如果注入了自己导入的依赖，那么其必须设置为 Lazy
 *
 * @author kfyty725
 * @date 2021/5/21 16:43
 * @email kfyty725@hotmail.com
 */
public interface ImportBeanDefinition {
    /**
     * 导入自定义的 bean 类型参与后续实例化
     *
     * @param applicationContext 应用上下文
     * @param scannedClasses     根据配置扫描到的 Class
     */
    default Set<BeanDefinition> doImport(ApplicationContext applicationContext, Set<Class<?>> scannedClasses) {
        return scannedClasses.stream().filter(classesFilter(applicationContext)).map(e -> this.buildBeanDefinition(applicationContext, e)).collect(Collectors.toSet());
    }

    /**
     * class 根据配置扫描到的 Class
     *
     * @param applicationContext 应用上下文
     * @return 过滤器
     */
    default Predicate<Class<?>> classesFilter(ApplicationContext applicationContext) {
        return clazz -> false;
    }

    /**
     * 生成 BeanDefinition
     *
     * @param applicationContext 应用上下文
     * @param clazz              符合条件的 class
     * @return BeanDefinition
     */
    default BeanDefinition buildBeanDefinition(ApplicationContext applicationContext, Class<?> clazz) {
        return genericBeanDefinition(clazz).getBeanDefinition();
    }
}
