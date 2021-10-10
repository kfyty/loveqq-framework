package com.kfyty.support.utils;

import javafx.util.Pair;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

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
     * 空注解数组
     */
    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];

    /**
     * Class 注解元素缓存
     */
    private static final Map<Pair<Class<?>, Class<? extends Annotation>>, Boolean> CLASS_HAS_ANNOTATION_ELEMENT_CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    /* ------------------------------------------ 基础方法 ------------------------------------------ */

    public static boolean isAnnotation(Class<?> clazz) {
        return clazz != null && Annotation.class.isAssignableFrom(clazz);
    }

    public static boolean isMetaAnnotation(Class<?> clazz) {
        return isAnnotation(clazz) && "java.lang.annotation".equals(ofNullable(clazz.getPackage()).map(Package::getName).orElse(null));
    }

    public static Annotation[] flatRepeatableAnnotation(Annotation[] annotations) {
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
        return flatAnnotations.toArray(EMPTY_ANNOTATIONS);
    }

    /* ------------------------------------------ 处理对象注解 ------------------------------------------ */

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

    public static <T extends Annotation> T findAnnotation(Object source, Class<T> annotationClass) {
        return findAnnotation(source.getClass(), annotationClass);
    }

    public static Annotation[] findAnnotations(Object source) {
        return findAnnotations(source.getClass());
    }

    /* ------------------------------------------ Class<?> 注解 ------------------------------------------ */

    public static boolean hasAnnotation(Class<?> source, Class<? extends Annotation> annotationClass) {
        return findAnnotation(source, annotationClass) != null;
    }

    public static boolean hasAnnotationElement(Class<?> source, Class<? extends Annotation> annotationClass) {
        return CLASS_HAS_ANNOTATION_ELEMENT_CACHE.computeIfAbsent(new Pair<>(source, annotationClass), k -> hasAnnotation(source, annotationClass) || !isMetaAnnotation(source) && hasAnnotationElementHelper(annotationClass, () -> findAnnotations(source)));
    }

    @SafeVarargs
    public static boolean hasAnyAnnotation(Class<?> source, Class<? extends Annotation>... annotations) {
        return Arrays.stream(annotations).anyMatch(e -> hasAnnotation(source, e));
    }

    @SafeVarargs
    public static boolean hasAnyAnnotationElement(Class<?> source, Class<? extends Annotation>... annotations) {
        return Arrays.stream(annotations).anyMatch(e -> hasAnnotationElement(source, e));
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
        return interfaceAnnotations.toArray(EMPTY_ANNOTATIONS);
    }

    /* ------------------------------------------ Constructor<?> 注解 ------------------------------------------ */

    public static boolean hasAnnotation(Constructor<?> constructor, Class<? extends Annotation> annotationClass) {
        return findAnnotation(constructor, annotationClass) != null;
    }

    public static boolean hasAnnotationElement(Constructor<?> constructor, Class<? extends Annotation> annotationClass) {
        return hasAnnotation(constructor, annotationClass) || hasAnnotationElementHelper(annotationClass, () -> findAnnotations(constructor));
    }

    @SafeVarargs
    public static boolean hasAnyAnnotation(Constructor<?> constructor, Class<? extends Annotation>... annotations) {
        return Arrays.stream(annotations).anyMatch(e -> hasAnnotation(constructor, e));
    }

    @SafeVarargs
    public static boolean hasAnyAnnotationElement(Constructor<?> constructor, Class<? extends Annotation>... annotations) {
        return Arrays.stream(annotations).anyMatch(e -> hasAnnotationElement(constructor, e));
    }

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
            return EMPTY_ANNOTATIONS;
        }
        Annotation[] annotations = constructor.getAnnotations();
        if (CommonUtil.notEmpty(annotations)) {
            return annotations;
        }
        return findAnnotations(ReflectUtil.getSuperConstructor(constructor));
    }

    /* ------------------------------------------ Field 注解 ------------------------------------------ */

    public static boolean hasAnnotation(Field field, Class<? extends Annotation> annotationClass) {
        return findAnnotation(field, annotationClass) != null;
    }

    public static boolean hasAnnotationElement(Field field, Class<? extends Annotation> annotationClass) {
        return hasAnnotation(field, annotationClass) || hasAnnotationElementHelper(annotationClass, () -> findAnnotations(field));
    }

    @SafeVarargs
    public static boolean hasAnyAnnotation(Field field, Class<? extends Annotation>... annotations) {
        return Arrays.stream(annotations).anyMatch(e -> hasAnnotation(field, e));
    }

    @SafeVarargs
    public static boolean hasAnyAnnotationElement(Field field, Class<? extends Annotation>... annotations) {
        return Arrays.stream(annotations).anyMatch(e -> hasAnnotationElement(field, e));
    }

    public static <T extends Annotation> T findAnnotation(Field field, Class<T> annotationClass) {
        return field == null ? null : field.getAnnotation(annotationClass);
    }

    public static Annotation[] findAnnotations(Field field) {
        return field.getAnnotations();
    }

    /* ------------------------------------------ Method 注解 ------------------------------------------ */

    public static boolean hasAnnotation(Method method, Class<? extends Annotation> annotationClass) {
        return findAnnotation(method, annotationClass) != null;
    }

    public static boolean hasAnnotationElement(Method method, Class<? extends Annotation> annotationClass) {
        return hasAnnotation(method, annotationClass) || hasAnnotationElementHelper(annotationClass, () -> findAnnotations(method));
    }

    @SafeVarargs
    public static boolean hasAnyAnnotation(Method method, Class<? extends Annotation>... annotations) {
        return Arrays.stream(annotations).anyMatch(e -> hasAnnotation(method, e));
    }

    @SafeVarargs
    public static boolean hasAnyAnnotationElement(Method method, Class<? extends Annotation>... annotations) {
        return Arrays.stream(annotations).anyMatch(e -> hasAnnotationElement(method, e));
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

    /* ------------------------------------------ Parameter 注解 ------------------------------------------ */

    public static boolean hasAnnotation(Parameter parameter, Class<? extends Annotation> annotationClass) {
        return findAnnotation(parameter, annotationClass) != null;
    }

    public static boolean hasAnnotationElement(Parameter parameter, Class<? extends Annotation> annotationClass) {
        return hasAnnotation(parameter, annotationClass) || hasAnnotationElementHelper(annotationClass, () -> findAnnotations(parameter));
    }

    @SafeVarargs
    public static boolean hasAnyAnnotation(Parameter parameter, Class<? extends Annotation>... annotations) {
        return Arrays.stream(annotations).anyMatch(e -> hasAnnotation(parameter, e));
    }

    @SafeVarargs
    public static boolean hasAnyAnnotationElement(Parameter parameter, Class<? extends Annotation>... annotations) {
        return Arrays.stream(annotations).anyMatch(e -> hasAnnotationElement(parameter, e));
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

    /* ------------------------------------------ 获取注解元素 ------------------------------------------ */

    public static <T extends Annotation> T findAnnotationElement(Object source, Class<T> annotationClass) {
        return findAnnotationElement(source.getClass(), annotationClass);
    }

    public static <T extends Annotation> T findAnnotationElement(Class<?> source, Class<T> annotationClass) {
        if (source == null) {
            return null;
        }
        T annotation = findAnnotation(source, annotationClass);
        if (annotation != null || isMetaAnnotation(source)) {
            return annotation;
        }
        for (Annotation nestedAnnotation : findAnnotations(source)) {
            annotation = findAnnotationElement(nestedAnnotation.annotationType(), annotationClass);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    public static <T extends Annotation> T findAnnotationElement(Executable executable, Class<T> annotationClass) {
        if (executable == null) {
            return null;
        }
        T annotation = executable instanceof Method ? findAnnotation((Method) executable, annotationClass) : findAnnotation((Constructor<?>) executable, annotationClass);
        if (annotation != null) {
            return annotation;
        }
        Annotation[] nestedAnnotations = executable instanceof Method ? findAnnotations((Method) executable) : findAnnotations((Constructor<?>) executable);
        for (Annotation nestedAnnotation : nestedAnnotations) {
            annotation = findAnnotationElement(nestedAnnotation.annotationType(), annotationClass);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    /* ------------------------------------------ 私有方法 ------------------------------------------ */

    private static boolean hasAnnotationElementHelper(Class<? extends Annotation> annotationClass, Supplier<Annotation[]> annotations) {
        return hasAnnotationElementHelper(annotationClass, annotations, AnnotationUtil::hasAnnotationElement);
    }

    private static boolean hasAnnotationElementHelper(Class<? extends Annotation> annotationClass, Supplier<Annotation[]> annotations, BiFunction<Class<?>, Class<? extends Annotation>, Boolean> hasAnnotation) {
        return Arrays.stream(annotations.get()).anyMatch(e -> hasAnnotation.apply(e.annotationType(), annotationClass));
    }
}
