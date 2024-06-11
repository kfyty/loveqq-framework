package com.kfyty.loveqq.framework.core.autoconfig.beans.filter;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.ComponentFilter;
import com.kfyty.loveqq.framework.core.exception.BeansException;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import lombok.Data;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 描述: 组件过滤器描述
 *
 * @author kfyty725
 * @date 2022/11/12 16:51
 * @email kfyty725@hotmail.com
 */
@Data
public class ComponentFilterDescription {
    /**
     * 过滤器声明所在类
     */
    private Class<?> declare;

    /**
     * 基础包名
     */
    private Set<String> basePackages;

    /**
     * 具体的某些类
     */
    private Set<Class<?>> classes;

    /**
     * 某些注解存在
     */
    private Set<Class<? extends Annotation>> annotations;

    public ComponentFilterDescription() {
        this.basePackages = new HashSet<>(4);
        this.classes = new HashSet<>(4);
        this.annotations = new HashSet<>(4);
    }

    public static ComponentFilterDescription from(Class<?> declare, ComponentFilter componentFilter) {
        ComponentFilterDescription description = new ComponentFilterDescription();
        description.setDeclare(declare);
        description.getBasePackages().addAll(Arrays.asList(componentFilter.value()));
        description.getClasses().addAll(Arrays.asList(componentFilter.classes()));
        description.getAnnotations().addAll(Arrays.asList(componentFilter.annotations()));
        return description;
    }

    public static boolean contains(Collection<ComponentFilterDescription> filters, Annotation other) {
        if (!(other instanceof ComponentFilter)) {
            throw new BeansException("The annotation must be ComponentFilter: " + other);
        }
        if (CommonUtil.empty(filters)) {
            return false;
        }
        ComponentFilter componentFilter = (ComponentFilter) other;
        for (ComponentFilterDescription filter : filters) {
            if (Arrays.equals(filter.getBasePackages().toArray(), componentFilter.value()) &&
                    Arrays.equals(filter.getClasses().toArray(), componentFilter.classes()) &&
                    Arrays.equals(filter.getAnnotations().toArray(), componentFilter.annotations())) {
                return true;
            }
        }
        return false;
    }
}
