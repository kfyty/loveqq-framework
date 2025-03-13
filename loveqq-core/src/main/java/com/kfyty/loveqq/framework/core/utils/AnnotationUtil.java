package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.lang.annotation.AliasFor;
import com.kfyty.loveqq.framework.core.lang.annotation.AnnotationInvocationHandler;
import com.kfyty.loveqq.framework.core.lang.annotation.Inherited;
import com.kfyty.loveqq.framework.core.lang.util.concurrent.WeakConcurrentHashMap;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.kfyty.loveqq.framework.core.lang.ConstantConfig.ANNOTATION_RESOLVE_DEPTH;
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
     * 注解缓存
     * key: 注解声明对象
     * value: 对应存在的注解，包括解析别名后的注解，以及注解的注解
     */
    private static final Map<AnnotatedElement, Annotation[]> ANNOTATION_CACHE = new WeakConcurrentHashMap<>(256);

    /* ------------------------------------------ 基础方法 ------------------------------------------ */

    public static boolean isAnnotation(Class<?> clazz) {
        return clazz != null && Annotation.class.isAssignableFrom(clazz);
    }

    public static boolean isMetaAnnotation(Class<?> clazz) {
        String packageName = ofNullable(clazz.getPackage()).map(Package::getName).orElse(null);
        return isAnnotation(clazz) && (Objects.equals(packageName, "java.lang.annotation") || Objects.equals(packageName, "kotlin.annotation") || Objects.equals(clazz.getName(), "kotlin.Metadata"));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T clone(T annotation) {
        return clone(annotation, (Class<? extends T>) annotation.annotationType());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Annotation, R extends Annotation> R clone(T annotation, Class<R> annotationType) {
        Map<String, Object> values = getAnnotationValues(annotation);
        AnnotationInvocationHandler handler = new AnnotationInvocationHandler(annotationType, new HashMap<>(values));
        return (R) Proxy.newProxyInstance(annotation.getClass().getClassLoader(), new Class<?>[]{annotationType}, handler);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getAnnotationValues(Annotation annotation) {
        InvocationHandler handler = Proxy.getInvocationHandler(annotation);
        if (handler instanceof AnnotationInvocationHandler) {
            return ((AnnotationInvocationHandler) handler).getMemberValues();
        }
        return (Map<String, Object>) ReflectUtil.getFieldValue(handler, "memberValues");
    }

    public static void setAnnotationValue(Annotation annotation, String annotationField, Object value) {
        getAnnotationValues(annotation).put(annotationField, value);
    }

    public static <T extends Annotation> T[] flatRepeatableAnnotation(Annotation[] annotations, Predicate<Annotation> test, IntFunction<T[]> supplier) {
        return flatRepeatableAnnotation(annotations).stream().filter(test).toArray(supplier);
    }

    public static List<Annotation> flatRepeatableAnnotation(Annotation[] annotations) {
        List<Annotation> flatAnnotations = new ArrayList<>(annotations.length);
        for (Annotation annotation : annotations) {
            Object value = null;
            Method method = ReflectUtil.getMethod(annotation.annotationType(), "value");
            if (method == null || !((value = ReflectUtil.invokeMethod(annotation, method)) instanceof Annotation[])) {
                flatAnnotations.add(annotation);
                continue;
            }
            Repeatable repeatable = method.getReturnType().getComponentType().getAnnotation(Repeatable.class);
            if (repeatable == null || !repeatable.value().equals(annotation.annotationType())) {
                flatAnnotations.add(annotation);
                continue;
            }
            flatAnnotations.addAll(Arrays.asList((Annotation[]) value));
        }
        return flatAnnotations;
    }

    /* ------------------------------------------ 查询对象注解 ------------------------------------------ */

    public static boolean hasAnnotation(Object source, Class<? extends Annotation> annotation) {
        return findAnnotation(source, annotation) != null;
    }

    @SafeVarargs
    public static boolean hasAnyAnnotation(Object source, Class<? extends Annotation>... annotations) {
        return Arrays.stream(annotations).anyMatch(e -> hasAnnotation(source, e));
    }

    /* ------------------------------------------ 获取注解 ------------------------------------------ */

    public static <T extends Annotation> T findAnnotation(Object source, Class<T> annotationClass) {
        if (source instanceof AnnotatedElement) {
            return findAnnotation((AnnotatedElement) source, annotationClass);
        }
        return findAnnotation(source.getClass(), annotationClass);
    }

    public static Annotation[] findAnnotations(Object source) {
        if (source instanceof AnnotatedElement) {
            return findAnnotations((AnnotatedElement) source);
        }
        return findAnnotations(source.getClass());
    }

    public static Annotation[] findAnnotations(AnnotatedElement annotatedElement, Predicate<Annotation> annotationTest) {
        return Arrays.stream(findAnnotations(annotatedElement)).filter(annotationTest).toArray(Annotation[]::new);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T findAnnotation(AnnotatedElement element, Class<T> annotationClass) {
        if (element == null) {
            return null;
        }
        Annotation[] annotations = ANNOTATION_CACHE.get(element);
        if (annotations == null) {
            annotations = findAnnotations(element);                                                                     // null 表示没有缓存
        }
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == annotationClass) {
                return (T) annotation;
            }
        }
        return null;
    }

    public static Annotation[] findAnnotations(AnnotatedElement element) {
        if (element == null || element instanceof Class<?> && isMetaAnnotation((Class<?>) element)) {
            return CommonUtil.EMPTY_ANNOTATIONS;
        }
        final Annotation[] resolvedAnnotations;
        final Annotation[] directAnnotations = element.getAnnotations();
        if (CommonUtil.notEmpty(directAnnotations)) {
            resolvedAnnotations = resolveAnnotation(new ArrayList<>(Arrays.asList(directAnnotations))).toArray(CommonUtil.EMPTY_ANNOTATIONS);
        } else {
            // class
            if (element instanceof Class<?>) {
                if (AopUtil.isJdkProxy(element)) {
                    resolvedAnnotations = Arrays.stream(((Class<?>) element).getInterfaces()).flatMap(e -> Arrays.stream(findAnnotations(e))).toArray(Annotation[]::new);
                } else if (AopUtil.isClassProxy(element)) {
                    resolvedAnnotations = findAnnotations(((Class<?>) element).getSuperclass());
                } else {
                    resolvedAnnotations = CommonUtil.EMPTY_ANNOTATIONS;
                }
            }
            // 构造器
            else if (element instanceof Constructor<?>) {
                Constructor<?> constructor = (Constructor<?>) element;
                Class<?> declaringClass = constructor.getDeclaringClass();
                if (Inherited.class.isAssignableFrom(declaringClass) || AopUtil.isProxy(declaringClass) && declaringClass != Object.class && declaringClass.getSuperclass() != Object.class) {
                    resolvedAnnotations = findAnnotations(ReflectUtil.getSuperConstructor(constructor));
                } else {
                    resolvedAnnotations = CommonUtil.EMPTY_ANNOTATIONS;
                }
            }
            // 方法
            else if (element instanceof Method) {
                Method method = (Method) element;
                Class<?> declaringClass = method.getDeclaringClass();
                if (Inherited.class.isAssignableFrom(declaringClass) || AopUtil.isProxy(declaringClass) && declaringClass != Object.class && declaringClass.getSuperclass() != Object.class) {
                    resolvedAnnotations = findAnnotations(ReflectUtil.getSuperMethod((Method) element));
                } else {
                    resolvedAnnotations = CommonUtil.EMPTY_ANNOTATIONS;
                }
            }
            // 参数
            else if (element instanceof Parameter) {
                Parameter parameter = (Parameter) element;
                Class<?> declaringClass = parameter.getDeclaringExecutable().getDeclaringClass();
                if (Inherited.class.isAssignableFrom(declaringClass) || AopUtil.isProxy(declaringClass) && declaringClass != Object.class && declaringClass.getSuperclass() != Object.class) {
                    resolvedAnnotations = findAnnotations(ReflectUtil.getSuperParameters((Parameter) element));
                } else {
                    resolvedAnnotations = CommonUtil.EMPTY_ANNOTATIONS;
                }
            } else {
                resolvedAnnotations = CommonUtil.EMPTY_ANNOTATIONS;
            }
        }
        ANNOTATION_CACHE.putIfAbsent(element, resolvedAnnotations);
        return resolvedAnnotations;
    }

    /**
     * 解析注解，获取嵌套的注解，以及注解别名
     *
     * @param annotations 直接注解
     * @return 解析后的注解
     */
    public static List<Annotation> resolveAnnotation(List<Annotation> annotations) {
        return resolveAnnotation(annotations, 1);
    }

    /**
     * 解析注解，获取嵌套的注解，以及注解别名
     *
     * @param annotations 直接注解
     * @param depth       嵌套注解解析深度
     * @return 解析后的注解
     */
    public static List<Annotation> resolveAnnotation(List<Annotation> annotations, int depth) {
        for (int i = annotations.size() - 1; i > -1; i--) {
            Annotation annotation = annotations.get(i);
            // 元注解不处理
            if (isMetaAnnotation(annotation.annotationType())) {
                annotations.remove(i);
                continue;
            }

            // 处理别名注解
            Method[] methods = ReflectUtil.getMethods(annotation.annotationType());
            Map<Annotation, Map<String, Object>> annotationValuesMap = new HashMap<>(8);
            Map<Class<? extends Annotation>, Annotation> nestedAnnotationMap = Arrays.stream(annotation.annotationType().getAnnotations()).collect(Collectors.toMap(Annotation::annotationType, Function.identity()));
            for (Method method : methods) {
                AliasFor aliasFor = method.getAnnotation(AliasFor.class);
                if (aliasFor != null) {
                    String alias = CommonUtil.empty(aliasFor.value()) ? method.getName() : aliasFor.value();
                    Annotation aliasAnnotation = aliasFor.annotation() == Annotation.class ? annotation : nestedAnnotationMap.get(aliasFor.annotation());

                    // 非自身注解需要深克隆，因为非自身注解是公用的元数据
                    if (aliasAnnotation == null) {
                        throw new IllegalArgumentException("The annotation of " + annotation.annotationType() + " must be annotated with " + aliasFor.annotation() + ", when AliasFor exists.");
                    }
                    if (aliasAnnotation != annotation && !(Proxy.getInvocationHandler(aliasAnnotation) instanceof AnnotationInvocationHandler)) {
                        nestedAnnotationMap.put(aliasFor.annotation(), aliasAnnotation = clone(aliasAnnotation));
                    }

                    // 处理别名属性配置
                    Map<String, Object> annotationValues = annotationValuesMap.computeIfAbsent(aliasAnnotation, AnnotationUtil::getAnnotationValues);
                    if (aliasAnnotation == annotation) {
                        annotationValues.put(method.getName(), ReflectUtil.invokeMethod(annotation, alias));
                    } else {
                        annotationValues.put(alias, ReflectUtil.invokeMethod(annotation, method));
                    }
                }
            }

            // 解析嵌套的注解
            if (depth < ANNOTATION_RESOLVE_DEPTH) {
                annotations.addAll(resolveAnnotation(new ArrayList<>(nestedAnnotationMap.values()), depth + 1));
            }
        }
        return annotations;
    }
}
