package com.kfyty.loveqq.framework.core.autoconfig.beans.autowired;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.io.FactoriesLoader;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;

import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 描述: 默认自动注入描述符解析器实现
 *
 * @author kfyty725
 * @date 2022/7/24 14:05
 * @email kfyty725@hotmail.com
 */
@Component
public class DelegatedAutowiredDescriptionResolver implements AutowiredDescriptionResolver {
    /**
     * 解析器
     */
    private final List<AutowiredDescriptionResolver> resolvers;

    public DelegatedAutowiredDescriptionResolver() {
        this.resolvers = FactoriesLoader.loadFactories(AutowiredDescriptionResolver.class)
                .stream()
                .map(ReflectUtil::load)
                .filter(AutowiredDescriptionResolver.class::isAssignableFrom)
                .map(ReflectUtil::newInstance)
                .map(e -> (AutowiredDescriptionResolver) e)
                .collect(Collectors.toList());
    }

    @Override
    public AutowiredDescription resolve(AnnotatedElement element) {
        for (AutowiredDescriptionResolver resolver : this.resolvers) {
            AutowiredDescription resolved = resolver.resolve(element);
            if (resolved != null) {
                return resolved;
            }
        }
        return null;
    }
}
