package com.kfyty.aop.aspectj.creator;

import com.kfyty.aop.Advisor;
import com.kfyty.aop.aspectj.AbstractAspectJAdvice;
import com.kfyty.support.utils.BeanUtil;
import javafx.util.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 描述: advisor 创建器
 *
 * @author kfyty725
 * @date 2021/8/2 11:03
 * @email kfyty725@hotmail.com
 */
public interface AdvisorCreator {

    default List<Advisor> createAdvisor(Function<AbstractAspectJAdvice, Object> aspectMapping, Class<?> aspectClass) {
        return this.createAdvisor(aspectMapping, new Class[] {aspectClass});
    }

    default List<Advisor> createAdvisor(Function<AbstractAspectJAdvice, Object> aspectMapping, Class<?> ... aspectClasses) {
        return this.createAdvisor(aspectMapping, Arrays.stream(aspectClasses).map(e -> new Pair<String, Class<?>>(BeanUtil.convert2BeanName(e), e)).collect(Collectors.toList()));
    }

    default List<Advisor> createAdvisor(Function<AbstractAspectJAdvice, Object> aspectMapping, List<Pair<String, Class<?>>> namedAspectClasses) {
        return namedAspectClasses.stream().flatMap(e -> this.createAdvisor(aspectMapping, e).stream()).collect(Collectors.toList());
    }

    List<Advisor> createAdvisor(Function<AbstractAspectJAdvice, Object> aspectMapping, Pair<String, Class<?>> namedAspectClass);
}
