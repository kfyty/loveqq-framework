package com.kfyty.core.autoconfig.beans.autowired;

import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.Lazy;
import com.kfyty.core.utils.AnnotationUtil;
import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;

import static com.kfyty.core.utils.AnnotationUtil.hasAnnotation;
import static com.kfyty.core.utils.CommonUtil.empty;

/**
 * 描述: 默认自动注入描述符解析器实现
 *
 * @author kfyty725
 * @date 2022/7/24 14:05
 * @email kfyty725@hotmail.com
 */
@Component
public class DefaultAutowiredDescriptionResolver implements AutowiredDescriptionResolver {

    @Override
    public AutowiredDescription resolve(AnnotatedElement element) {
        return doResolve(element);
    }

    public static AutowiredDescription doResolve(AnnotatedElement element) {
        AutowiredDescription description = doResolveAutowired(element);

        if (description != null) {
            return description;
        }

        description = doResolveInject(element);

        if (description != null) {
            return description;
        }

        description = doResolveResource(element);

        return description;
    }

    protected static AutowiredDescription doResolveAutowired(AnnotatedElement element) {
        Autowired autowired = AnnotationUtil.findAnnotation(element, Autowired.class);
        return autowired == null ? null : new AutowiredDescription(autowired.value(), autowired.required()).markLazied(hasAnnotation(element, Lazy.class));
    }

    protected static AutowiredDescription doResolveInject(AnnotatedElement element) {
        if (!AutowiredDescription.INJECT_AVAILABLE) {
            return null;
        }

        Inject inject = AnnotationUtil.findAnnotation(element, Inject.class);

        if (inject == null) {
            return null;
        }

        Named named = AnnotationUtil.findAnnotation(element, Named.class);
        String accessObjectName = element instanceof Field ? ((Field) element).getName() : ((Executable) element).getName();
        String beanName = named == null || empty(named.value()) ? accessObjectName : named.value();
        return new AutowiredDescription(beanName, true).markLazied(hasAnnotation(element, Lazy.class));
    }

    protected static AutowiredDescription doResolveResource(AnnotatedElement element) {
        if (!AutowiredDescription.JAKARTA_AVAILABLE) {
            return null;
        }

        Resource resource = AnnotationUtil.findAnnotation(element, Resource.class);

        if (resource == null) {
            return null;
        }

        String accessObjectName = element instanceof Field ? ((Field) element).getName() : ((Executable) element).getName();
        String beanName = empty(resource.name()) ? accessObjectName : resource.name();
        return new AutowiredDescription(beanName, true).markLazied(hasAnnotation(element, Lazy.class));
    }
}
