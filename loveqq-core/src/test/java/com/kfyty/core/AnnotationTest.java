package com.kfyty.core;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.BootApplication;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.lang.annotation.AliasFor;
import com.kfyty.loveqq.framework.core.proxy.factory.DynamicProxyFactory;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/8/6 9:15
 * @email kfyty725@hotmail.com
 */
@BootApplication("test")
public class AnnotationTest {
    @Anno
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface SelfAlias {
        @AliasFor
        String path();

        @AliasFor(value = "anno", annotation = Anno.class)
        String annoAlias() default "";

        @AliasFor("path")
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Anno {
        String anno() default "";
    }

    @Test
    public void annotationTest() {
        @SelfAlias(path = "path", annoAlias = "alias")
        class User {
        }

        SelfAlias selfAlias = AnnotationUtil.findAnnotation(User.class, SelfAlias.class);
        Anno anno = AnnotationUtil.findAnnotation(User.class, Anno.class);
        Assertions.assertEquals(selfAlias.value(), "path");
        Assertions.assertEquals(anno.anno(), "alias");
    }

    interface AnnoInter {
        @Autowired
        void test();
    }

    @Test
    public void annotationTest1() throws Exception {
        AnnoInter ai = DynamicProxyFactory.create().createProxy(AnnoInter.class);
        Autowired autowired = AnnotationUtil.findAnnotation(ai.getClass().getMethod("test"), Autowired.class);
        Assertions.assertNotNull(autowired);
    }

    @Test
    public void annotationTest2() {
        Annotation[] annotations = AnnotationUtil.findAnnotations(AnnotationTest.class);
        Component annotation = (Component) Arrays.stream(annotations).filter(e -> e.annotationType() == Component.class).findAny().orElse(null);
        Assertions.assertEquals(annotations.length, 5);
        Assertions.assertNotNull(annotation);
        Assertions.assertEquals(annotation.value(), "test");
    }

    @Test
    public void annotationTest3() {
        AnnotationTest proxy = DynamicProxyFactory.create(true).createProxy(AnnotationTest.class);
        Annotation[] annotations = AnnotationUtil.findAnnotations(proxy);
        Assertions.assertEquals(annotations.length, 5);
    }

    @Test
    public void cloneTest() {
        BootApplication annotation = AnnotationTest.class.getAnnotation(BootApplication.class);
        BootApplication clone = AnnotationUtil.clone(annotation);
        Assertions.assertEquals(annotation, clone);
        Assertions.assertEquals(clone, annotation);
    }

    @Test
    @A(name = "test")
    public void cycleAnnotationTest() {
        Method method = ReflectUtil.getMethod(AnnotationTest.class, "cycleAnnotationTest");
        Annotation[] annotations = AnnotationUtil.findAnnotations(method);
    }

    @B
    @Retention(RetentionPolicy.RUNTIME)
    @interface A {
        @AliasFor(annotation = B.class)
        String name() default "";
    }

    @A
    @Retention(RetentionPolicy.RUNTIME)
    @interface B {
        @AliasFor(annotation = A.class)
        String name() default "";
    }
}
