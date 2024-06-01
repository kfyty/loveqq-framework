package com.kfyty.core.autoconfig.beans.filter;

import com.kfyty.core.autoconfig.ConfigurableApplicationContext;
import com.kfyty.core.autoconfig.annotation.ComponentScan;
import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.autoconfig.aware.ConfigurableApplicationContextAware;
import com.kfyty.core.support.Pair;
import com.kfyty.core.support.PatternMatcher;

import java.util.List;

import static com.kfyty.core.utils.AnnotationUtil.hasAnnotationElement;

/**
 * 描述: 组件匹配器
 *
 * @author kfyty725
 * @date 2022/11/12 16:51
 * @email kfyty725@hotmail.com
 */
@Order
public class DefaultComponentMatcher implements ComponentMatcher, ConfigurableApplicationContextAware {
    private ConfigurableApplicationContext applicationContext;

    @Override
    public void setConfigurableApplicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 组件匹配
     * 先根据排除过滤器进行匹配
     * a. 返回 true 时，说明不符合排除过滤器，此时继续匹配包含过滤器即可
     * b. 返回 false 时，说明符合排除过滤器，此时需继续判断该过滤器所在的声明类是否匹配，若该过滤器的声明类不匹配，则当前目标类匹配
     *
     * @param beanClass      目标类
     * @param includeFilters 包含过滤器元数据
     * @param excludeFilters 排除过滤器元数据
     * @return 是否匹配，true 时将生成 bean 定义
     */
    @Override
    public boolean isMatch(Class<?> beanClass, List<ComponentFilterDescription> includeFilters, List<ComponentFilterDescription> excludeFilters) {
        Pair<Boolean, ComponentFilterDescription> exclude = this.isMatch(beanClass, excludeFilters, false);
        if (!exclude.getKey() && exclude.getValue() != null) {
            return !this.applicationContext.isMatchComponent(exclude.getValue().getDeclare());
        }
        Pair<Boolean, ComponentFilterDescription> include = this.isMatch(beanClass, includeFilters, true);
        return include.getKey();
    }

    /**
     * 根据组件过滤器配置进行匹配
     *
     * @param beanClass 目标 bean class
     * @param filters   组件过滤条件，可能是 {@link ComponentScan#includeFilter()}，也可能是 {@link ComponentScan#excludeFilter()}
     * @param isInclude 用于标识当前过滤器是排除还是包含
     * @return 匹配结果
     * key: 表示匹配结果，无论何种匹配器，false 都表示目标类不匹配，不应生成 bean 定义
     * value: 表示返回结果时，使用的过滤器配置
     */
    protected Pair<Boolean, ComponentFilterDescription> isMatch(Class<?> beanClass, List<ComponentFilterDescription> filters, boolean isInclude) {
        PatternMatcher patternMatcher = this.applicationContext.getPatternMatcher();
        for (ComponentFilterDescription filter : filters) {
            if (filter.getBasePackages().stream().anyMatch(e -> patternMatcher.matches(e, beanClass.getName()))) {
                return new Pair<>(isInclude, filter);
            }
            if (filter.getClasses().contains(beanClass)) {
                return new Pair<>(isInclude, filter);
            }
            if (filter.getAnnotations().stream().anyMatch(e -> hasAnnotationElement(beanClass, e))) {
                return new Pair<>(isInclude, filter);
            }
        }
        return new Pair<>(!isInclude, null);
    }
}
