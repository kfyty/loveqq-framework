package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.lang.util.concurrent.WeakConcurrentHashMap;
import com.kfyty.loveqq.framework.core.support.Pair;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Optional.ofNullable;

/**
 * 描述: 注解工具
 *
 * @author kfyty725
 * @date 2021/6/17 10:30
 * @email kfyty725@hotmail.com
 */
public abstract class AnnotationUtil {
    /**
     * Class 注解元素缓存
     */
    private static final Map<Pair<Object, Class<? extends Annotation>>, Annotation> ANNOTATION_ELEMENT_CACHE = new WeakConcurrentHashMap<>();

    /* ------------------------------------------ 基础方法 ------------------------------------------ */

    public static boolean isAnnotation(Class<?> clazz) {
        return clazz != null && Annotation.class.isAssignableFrom(clazz);
    }

    public static boolean isMetaAnnotation(Class<?> clazz) {
        String packageName = ofNullable(clazz.getPackage()).map(Package::getName).orElse(null);
        return isAnnotation(clazz) && (Objects.equals(packageName, "java.lang.annotation") || Objects.equals(packageName, "kotlin.annotation") || Objects.equals(clazz.getName(), "kotlin.Metadata"));
    }

    public static Annotation[] flatRepeatableAnnotation(Annotation[] annotations) {
        return flatRepeatableAnnotation(annotations, CommonUtil.EMPTY_ANNOTATIONS);
    }

    public static Annotation[] flatRepeatableAnnotation(Annotation[] annotations, Annotation[] targetAnnotation) {
        List<Annotation> flatAnnotations = new ArrayList<>(annotations.length);
        for (Annotation annotation : annotations) {
            Object value = null;
            Method method = ReflectUtil.getMethod(annotation.annotationType(), "value");
            if (method == null || !((value = ReflectUtil.invokeMethod(annotation, method)) instanceof Annotation[])) {
                flatAnnotations.add(annotation);
                continue;
            }
            Repeatable repeatable = findAnnotation(method.getReturnType().getComponentType(), Repeatable.class);
            if (repeatable == null || !repeatable.value().equals(annotation.annotationType())) {
                flatAnnotations.add(annotation);
                continue;
            }
            flatAnnotations.addAll(Arrays.asList((Annotation[]) value));
        }
        return flatAnnotations.toArray(targetAnnotation);
    }

    /* ------------------------------------------ 处理对象注解 ------------------------------------------ */

    public static boolean hasAnnotation(Object source, Class<? extends Annotation> annotation) {
        return findAnnotation(source, annotation) != null;
    }

    public static boolean hasAnnotationElement(Object source, Class<? extends Annotation> annotation) {
        return findAnnotationElement(source, annotation) != null;
    }

    @SafeVarargs
    public static boolean hasAnyAnnotation(Object source, Class<? extends Annotation>... annotations) {
        return Arrays.stream(annotations).anyMatch(e -> hasAnnotation(source, e));
    }

    @SafeVarargs
    public static boolean hasAnyAnnotationElement(Object source, Class<? extends Annotation>... annotations) {
        return Arrays.stream(annotations).anyMatch(e -> hasAnnotationElement(source, e));
    }

    public static <T extends Annotation> T findAnnotation(Object source, Class<T> annotationClass) {
        if (source instanceof Class) {
            return findAnnotation((Class<?>) source, annotationClass);
        }
        if (source instanceof Constructor) {
            return findAnnotation((Constructor<?>) source, annotationClass);
        }
        if (source instanceof Field) {
            return findAnnotation((Field) source, annotationClass);
        }
        if (source instanceof Method) {
            return findAnnotation((Method) source, annotationClass);
        }
        return source instanceof Parameter ? findAnnotation((Parameter) source, annotationClass) : findAnnotation(source.getClass(), annotationClass);
    }

    public static Annotation[] findAnnotations(Object source) {
        if (source instanceof Class) {
            return findAnnotations((Class<?>) source);
        }
        if (source instanceof Constructor) {
            return findAnnotations((Constructor<?>) source);
        }
        if (source instanceof Field) {
            return findAnnotations((Field) source);
        }
        if (source instanceof Method) {
            return findAnnotations((Method) source);
        }
        return source instanceof Parameter ? findAnnotations((Parameter) source) : findAnnotations(source.getClass());
    }

    /* ------------------------------------------ Class<?> 注解 ------------------------------------------ */

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

    public static Annotation[] findAnnotations(Class<?> source) {
        if (source == null) {
            return CommonUtil.EMPTY_ANNOTATIONS;
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
        return interfaceAnnotations.toArray(CommonUtil.EMPTY_ANNOTATIONS);
    }

    /* ------------------------------------------ Constructor<?> 注解 ------------------------------------------ */

    public static <T extends Annotation> T findAnnotation(Constructor<?> constructor, Class<T> annotationClass) {
        if (constructor == null) {
            return null;
        }
        T annotation = constructor.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }
        return findAnnotation(ReflectUtil.getSuperConstructor(constructor), annotationClass);
    }

    public static Annotation[] findAnnotations(Constructor<?> constructor) {
        if (constructor == null) {
            return CommonUtil.EMPTY_ANNOTATIONS;
        }
        Annotation[] annotations = constructor.getAnnotations();
        if (CommonUtil.notEmpty(annotations)) {
            return annotations;
        }
        return findAnnotations(ReflectUtil.getSuperConstructor(constructor));
    }

    /* ------------------------------------------ Field 注解 ------------------------------------------ */

    public static <T extends Annotation> T findAnnotation(Field field, Class<T> annotationClass) {
        return field == null ? null : field.getAnnotation(annotationClass);
    }

    public static Annotation[] findAnnotations(Field field) {
        return field.getAnnotations();
    }

    /* ------------------------------------------ Method 注解 ------------------------------------------ */

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

    public static Annotation[] findAnnotations(Method method) {
        if (method == null) {
            return CommonUtil.EMPTY_ANNOTATIONS;
        }
        Annotation[] annotations = method.getAnnotations();
        if (CommonUtil.notEmpty(annotations)) {
            return annotations;
        }
        return findAnnotations(ReflectUtil.getSuperMethod(method));
    }

    /* ------------------------------------------ Parameter 注解 ------------------------------------------ */

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

    public static Annotation[] findAnnotations(Parameter parameter) {
        if (parameter == null) {
            return CommonUtil.EMPTY_ANNOTATIONS;
        }
        Annotation[] annotations = parameter.getAnnotations();
        if (CommonUtil.notEmpty(annotations)) {
            return annotations;
        }
        return findAnnotations(ReflectUtil.getSuperParameters(parameter));
    }

    /* ------------------------------------------ 获取注解元素 ------------------------------------------ */

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T findAnnotationElement(Object source, Class<T> annotationClass) {
        return (T) ANNOTATION_ELEMENT_CACHE.computeIfAbsent(new Pair<>(source, annotationClass), k -> {
            if (source instanceof AnnotatedElement) {
                return findAnnotationElement((AnnotatedElement) source, annotationClass);
            }
            return findAnnotationElement(source.getClass(), annotationClass);
        });
    }

    public static <T extends Annotation> T findAnnotationElement(AnnotatedElement annotatedElement, Class<T> annotationClass) {
        if (annotatedElement == null) {
            return null;
        }
        T annotation = findAnnotation(annotatedElement, annotationClass);
        if (annotation != null || annotatedElement instanceof Class && isMetaAnnotation((Class<?>) annotatedElement)) {
            return annotation;
        }
        Annotation[] nestedAnnotations = findAnnotations(annotatedElement);
        for (Annotation nestedAnnotation : nestedAnnotations) {
            annotation = findAnnotationElement(nestedAnnotation.annotationType(), annotationClass);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    public static Annotation[] findAnnotationElements(AnnotatedElement annotatedElement, Predicate<Annotation> annotationTest) {
        Annotation[] declareAnnotations = findAnnotations(annotatedElement);
        if (CommonUtil.empty(declareAnnotations)) {
            return CommonUtil.EMPTY_ANNOTATIONS;
        }
        Set<Annotation> annotations = new LinkedHashSet<>();
        for (Annotation annotation : declareAnnotations) {
            if (isMetaAnnotation(annotation.annotationType())) {
                continue;
            }
            if (annotationTest.test(annotation)) {
                annotations.add(annotation);
            }
            annotations.addAll(Arrays.asList(findAnnotationElements(annotation.annotationType(), annotationTest)));
        }
        return annotations.toArray(CommonUtil.EMPTY_ANNOTATIONS);
    }
}
