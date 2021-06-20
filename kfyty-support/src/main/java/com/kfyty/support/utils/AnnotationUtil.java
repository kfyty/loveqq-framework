package com.kfyty.support.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 描述: 注解工具
 *
 * @author kfyty725
 * @date 2021/6/17 10:30
 * @email kfyty725@hotmail.com
 */
public abstract class AnnotationUtil {
    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];

    public static boolean hasAnnotation(Object source, Class<? extends Annotation> annotation) {
        return hasAnnotation(source.getClass(), annotation);
    }

    public static boolean hasAnnotationElement(Object source, Class<? extends Annotation> annotation) {
        return hasAnnotationElement(source.getClass(), annotation);
    }

    @SafeVarargs
    public static boolean hasAnyAnnotation(Object source, Class<? extends Annotation>... annotations) {
        return hasAnyAnnotation(source.getClass(), annotations);
    }

    @SafeVarargs
    public static boolean hasAnyAnnotationElement(Object source, Class<? extends Annotation>... annotations) {
        return hasAnyAnnotationElement(source.getClass(), annotations);
    }

    @SafeVarargs
    public static boolean hasAnyAnnotation(Class<?> source, Class<? extends Annotation>... annotations) {
        for (Class<? extends Annotation> annotation : annotations) {
            if (hasAnnotation(source, annotation)) {
                return true;
            }
        }
        return false;
    }

    @SafeVarargs
    public static boolean hasAnyAnnotationElement(Class<?> source, Class<? extends Annotation>... annotations) {
        for (Class<? extends Annotation> annotation : annotations) {
            if (hasAnnotationElement(source, annotation)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAnnotation(Class<?> source, Class<? extends Annotation> annotationClass) {
        return findAnnotation(source, annotationClass) != null;
    }

    public static boolean hasAnnotationElement(Class<?> source, Class<? extends Annotation> annotationClass) {
        if (hasAnnotation(source, annotationClass)) {
            return true;
        }
        for (Annotation annotation : findAnnotations(source)) {
            if (hasAnnotation(annotation.annotationType(), annotationClass)) {
                return true;
            }
        }
        return false;
    }

    @SafeVarargs
    public static boolean hasAnyAnnotation(Method method, Class<? extends Annotation>... annotations) {
        for (Class<? extends Annotation> annotation : annotations) {
            if (hasAnnotation(method, annotation)) {
                return true;
            }
        }
        return false;
    }

    @SafeVarargs
    public static boolean hasAnyAnnotationElement(Method method, Class<? extends Annotation>... annotations) {
        for (Class<? extends Annotation> annotation : annotations) {
            if (hasAnnotationElement(method, annotation)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAnnotation(Method method, Class<? extends Annotation> annotationClass) {
        return findAnnotation(method, annotationClass) != null;
    }

    public static boolean hasAnnotationElement(Method method, Class<? extends Annotation> annotationClass) {
        if (hasAnnotation(method, annotationClass)) {
            return true;
        }
        for (Annotation annotation : findAnnotations(method)) {
            if (hasAnnotation(annotation.annotationType(), annotationClass)) {
                return true;
            }
        }
        return false;
    }

    @SafeVarargs
    public static boolean hasAnyAnnotation(Field field, Class<? extends Annotation>... annotations) {
        for (Class<? extends Annotation> annotation : annotations) {
            if (hasAnnotation(field, annotation)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAnnotation(Field field, Class<? extends Annotation> annotationClass) {
        return findAnnotation(field, annotationClass) != null;
    }

    @SafeVarargs
    public static boolean hasAnyAnnotation(Parameter parameter, Class<? extends Annotation>... annotations) {
        for (Class<? extends Annotation> annotation : annotations) {
            if (hasAnnotation(parameter, annotation)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAnnotation(Parameter parameter, Class<? extends Annotation> annotationClass) {
        return findAnnotation(parameter, annotationClass) != null;
    }

    public static Annotation[] findAnnotations(Object source) {
        return findAnnotations(source.getClass());
    }

    public static Annotation[] findAnnotations(Class<?> source) {
        if (source == null) {
            return EMPTY_ANNOTATIONS;
        }
        Annotation[] annotations = source.getAnnotations();
        if (CommonUtil.notEmpty(annotations)) {
            return annotations;
        }
        annotations = findAnnotations(source.getSuperclass());
        if (CommonUtil.notEmpty(annotations)) {
            return annotations;
        }
        List<Annotation> interfaceAnnotations = new ArrayList<>();
        for (Class<?> sourceInterface : source.getInterfaces()) {
            interfaceAnnotations.addAll(Arrays.asList(findAnnotations(sourceInterface)));
        }
        return interfaceAnnotations.toArray(new Annotation[0]);
    }

    public static Annotation[] findAnnotations(Field field) {
        return field.getAnnotations();
    }

    public static Annotation[] findAnnotations(Method method) {
        if (method == null) {
            return EMPTY_ANNOTATIONS;
        }
        Annotation[] annotations = method.getAnnotations();
        if (CommonUtil.notEmpty(annotations)) {
            return annotations;
        }
        return findAnnotations(ReflectUtil.getSuperMethod(method));
    }

    public static Annotation[] findAnnotations(Parameter parameter) {
        if (parameter == null) {
            return EMPTY_ANNOTATIONS;
        }
        Annotation[] annotations = parameter.getAnnotations();
        if (CommonUtil.notEmpty(annotations)) {
            return annotations;
        }
        return findAnnotations(ReflectUtil.getSuperParameters(parameter));
    }

    public static <T extends Annotation> T findAnnotation(Object source, Class<T> annotationClass) {
        return findAnnotation(source.getClass(), annotationClass);
    }

    public static <T extends Annotation> T findAnnotation(Class<?> source, Class<T> annotationClass) {
        if (source == null) {
            return null;
        }
        T annotation = source.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }
        annotation = findAnnotation(source.getSuperclass(), annotationClass);
        if (annotation != null) {
            return annotation;
        }
        for (Class<?> sourceInterface : source.getInterfaces()) {
            annotation = findAnnotation(sourceInterface, annotationClass);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    public static <T extends Annotation> T findAnnotation(Field field, Class<T> annotationClass) {
        return field == null ? null : field.getAnnotation(annotationClass);
    }

    public static <T extends Annotation> T findAnnotation(Method method, Class<T> annotationClass) {
        if (method == null) {
            return null;
        }
        T annotation = method.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }
        return findAnnotation(ReflectUtil.getSuperMethod(method), annotationClass);
    }

    public static <T extends Annotation> T findAnnotation(Parameter parameter, Class<T> annotationClass) {
        if (parameter == null) {
            return null;
        }
        T annotation = parameter.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }
        return findAnnotation(ReflectUtil.getSuperParameters(parameter), annotationClass);
    }
}
