package com.kfyty.core.support;

import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.CommonUtil;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Objects;

/**
 * 描述: 注解实例包装，包含了该注解所注解的实例
 *
 * @author kfyty725
 * @date 2021/7/10 18:37
 * @email kfyty725@hotmail.com
 */
@ToString
@EqualsAndHashCode
public class AnnotationMetadata<T extends Annotation> {
    /**
     * 注解声明对象，支持 Class，Constructor，Field，Method，Parameter，Annotation
     */
    private final Object declaring;

    /**
     * 注解实例
     */
    private final T annotation;

    public AnnotationMetadata(Object declaring, T annotation) {
        this.declaring = Objects.requireNonNull(declaring);
        this.annotation = Objects.requireNonNull(annotation);
    }

    public boolean isDeclaringClass() {
        return this.declaring instanceof Class;
    }

    public boolean isDeclaringConstructor() {
        return this.declaring instanceof Constructor;
    }

    public boolean isDeclaringField() {
        return this.declaring instanceof Field;
    }

    public boolean isDeclaringMethod() {
        return this.declaring instanceof Method;
    }

    public boolean isDeclaringParameter() {
        return this.declaring instanceof Parameter;
    }

    public boolean isDeclaringAnnotation() {
        return AnnotationUtil.isAnnotation(this.declaring.getClass());
    }

    public T get() {
        return this.annotation;
    }

    @SuppressWarnings("unchecked")
    public <D> D getDeclaring() {
        return (D) this.declaring;
    }

    public static <T extends Annotation> boolean contains(Collection<AnnotationMetadata<T>> metadata, Annotation other) {
        if (CommonUtil.empty(metadata)) {
            return false;
        }
        for (AnnotationMetadata<T> element : metadata) {
            if (element.get().equals(other)) {
                return true;
            }
        }
        return false;
    }
}
