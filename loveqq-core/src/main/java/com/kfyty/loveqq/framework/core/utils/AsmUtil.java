package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.support.Pair;
import lombok.SneakyThrows;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Predicate;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/8/25 14:04
 * @email kfyty725@hotmail.com
 */
public abstract class AsmUtil {
    /**
     * 基于 asm 获取 class 上的全部注解
     *
     * @param className eg: java/lang/Object
     * @return annotations of class
     */
    public static Collection<String> getClassAnnotationNames(String className, boolean ignoredMetaAnnotation) {
        return getClassAnnotationNames(className, new LinkedList<>(), null, ignoredMetaAnnotation);
    }

    /**
     * 返回是否元注解
     *
     * @param className eg: java/lang/Object
     * @return true if meta annotation
     */
    public static boolean isMetaAnnotation(String className) {
        return className.startsWith("java/lang/annotation") || className.startsWith("kotlin/annotation") || className.equals("kotlin/Metadata");
    }

    /**
     * 基于 asm 获取 class 上的全部注解
     *
     * @param className             eg: java/lang/Object
     * @param container             结果容器
     * @param breakTest             测试通过时，不再查找嵌套的注解
     * @param ignoredMetaAnnotation 是否忽略元注解
     * @return annotations of class
     */
    @SneakyThrows(IOException.class)
    public static Collection<String> getClassAnnotationNames(String className, Collection<String> container, Predicate<String> breakTest, boolean ignoredMetaAnnotation) {
        URL url = AsmUtil.class.getResource("/" + className + ".class");
        if (url == null) {
            throw new ResolvableException("Can't obtain class file of: " + className);
        }
        return getClassAnnotationNames(url.openStream(), container, breakTest, ignoredMetaAnnotation);
    }

    /**
     * 基于 asm 获取 class 上的全部注解，会搜索注解的注解
     *
     * @param stream                字节码 输入流
     * @param container             结果容器
     * @param breakTest             测试通过时，不再查找嵌套的注解
     * @param ignoredMetaAnnotation 是否忽略元注解
     * @return annotations of class
     */
    @SneakyThrows(IOException.class)
    public static Collection<String> getClassAnnotationNames(InputStream stream, Collection<String> container, Predicate<String> breakTest, boolean ignoredMetaAnnotation) {
        new ClassReader(stream).accept(new ClassAnnotationNamesClassVisitor(Opcodes.ASM9, container, breakTest, ignoredMetaAnnotation), ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return container;
    }

    /**
     * 基于 asm 获取 class 上的全部注解，会搜索注解的注解
     *
     * @param classBytes            字节码
     * @param container             结果容器
     * @param breakTest             测试通过时，不再查找嵌套的注解
     * @param ignoredMetaAnnotation 是否忽略元注解
     * @return annotations of class
     */
    public static Collection<String> getClassAnnotationNames(byte[] classBytes, Collection<String> container, Predicate<String> breakTest, boolean ignoredMetaAnnotation) {
        new ClassReader(classBytes).accept(new ClassAnnotationNamesClassVisitor(Opcodes.ASM9, container, breakTest, ignoredMetaAnnotation), ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return container;
    }

    /**
     * 基于 asm 获取符合的方法名称
     *
     * @param classBytes         字节码
     * @param container          结果容器
     * @param methodNameTest     测试方法名称
     * @param annotationNameTest 测试方法上的注解
     * @return annotations of method
     */
    public static Collection<Pair<String, String>> getMethodNames(byte[] classBytes, Collection<Pair<String, String>> container, Predicate<String> methodNameTest, Predicate<String> annotationNameTest) {
        new ClassReader(classBytes).accept(new MethodNamesClassVisitor(Opcodes.ASM9, container, methodNameTest, annotationNameTest), ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return container;
    }

    private static class ClassAnnotationNamesClassVisitor extends ClassVisitor {
        private final Collection<String> container;
        private final Predicate<String> breakTest;
        private final boolean ignoredMetaAnnotation;

        private ClassAnnotationNamesClassVisitor(int api, Collection<String> container, Predicate<String> breakTest, boolean ignoredMetaAnnotation) {
            super(api);
            this.container = container;
            this.breakTest = breakTest;
            this.ignoredMetaAnnotation = ignoredMetaAnnotation;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            String annotationDescriptor = descriptor.substring(1, descriptor.length() - 1);
            String annotationName = annotationDescriptor.replace('/', '.');
            if (container.contains(annotationName)) {
                return null;
            }
            container.add(annotationName);
            if (ignoredMetaAnnotation && isMetaAnnotation(annotationDescriptor)) {
                return null;
            }
            if (breakTest != null) {
                for (String name : container) {
                    if (breakTest.test(name)) {
                        return null;
                    }
                }
            }
            try {
                getClassAnnotationNames(annotationDescriptor, container, breakTest, ignoredMetaAnnotation);
            } catch (ResolvableException e) {
                // 嵌套的注解可能不存在，忽略即可
            }
            return null;
        }
    }

    private static class MethodNamesClassVisitor extends ClassVisitor {
        private final Collection<Pair<String, String>> container;
        private final Predicate<String> methodNameTest;
        private final Predicate<String> annotationNameTest;

        protected MethodNamesClassVisitor(int api, Collection<Pair<String, String>> container, Predicate<String> methodNameTest, Predicate<String> annotationNameTest) {
            super(api);
            this.container = container;
            this.methodNameTest = methodNameTest;
            this.annotationNameTest = annotationNameTest;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            final String methodName = name;
            final String methodDesc = descriptor;
            if (methodNameTest != null && methodNameTest.test(name)) {
                container.add(new Pair<>(methodName, methodDesc));
            }
            return new MethodVisitor(Opcodes.ASM9) {

                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    String annotationName = descriptor.substring(1, descriptor.length() - 1).replace('/', '.');
                    if (annotationNameTest != null && annotationNameTest.test(annotationName)) {
                        container.add(new Pair<>(methodName, methodDesc));
                    }
                    return null;
                }
            };
        }
    }
}
