package com.kfyty.loveqq.framework.aop.aspectj.creator;

import com.kfyty.loveqq.framework.aop.Advisor;
import com.kfyty.loveqq.framework.aop.aspectj.AspectClass;
import com.kfyty.loveqq.framework.aop.aspectj.AspectJFactory;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 描述: advisor 创建器
 *
 * @author kfyty725
 * @date 2021/8/2 11:03
 * @email kfyty725@hotmail.com
 */
public interface AdvisorCreator {

    default List<Advisor> createAdvisor(AspectJFactory aspectJFactory, Class<?> aspectClass) {
        return this.createAdvisor(aspectJFactory, new Class[]{aspectClass});
    }

    default List<Advisor> createAdvisor(AspectJFactory aspectJFactory, Class<?>... aspectClasses) {
        return this.createAdvisor(aspectJFactory, Arrays.stream(aspectClasses).map(e -> new AspectClass(BeanUtil.getBeanName(e), e)).collect(Collectors.toList()));
    }

    default List<Advisor> createAdvisor(AspectJFactory aspectJFactory, List<AspectClass> aspectClasses) {
        return aspectClasses.stream().flatMap(e -> this.createAdvisor(aspectJFactory, e).stream()).collect(Collectors.toList());
    }

    List<Advisor> createAdvisor(AspectJFactory aspectJFactory, AspectClass aspectClass);
}
