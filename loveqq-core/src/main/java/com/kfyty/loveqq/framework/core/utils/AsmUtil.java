package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import lombok.SneakyThrows;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
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
    public static Collection<String> getAnnotationNames(String className) {
        return getAnnotationNames(className, new LinkedList<>(), null);
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
     * @param className eg: java/lang/Object
     * @param container 结果容器
     * @param breakTest 测试通过时，不再查找嵌套的注解
     * @return annotations of class
     */
    @SneakyThrows(IOException.class)
    public static Collection<String> getAnnotationNames(String className, Collection<String> container, Predicate<String> breakTest) {
        URL url = AsmUtil.class.getResource("/" + className + ".class");
        if (url == null) {
            throw new ResolvableException("Can't obtain class file of: " + className);
        }
        return getAnnotationNames(url.openStream(), container, breakTest);
    }

    /**
     * 基于 asm 获取 class 上的全部注解
     *
     * @param stream    字节码输入流
     * @param container 结果容器
     * @param breakTest 测试通过时，不再查找嵌套的注解
     * @return annotations of class
     */
    @SneakyThrows(IOException.class)
    public static Collection<String> getAnnotationNames(InputStream stream, Collection<String> container, Predicate<String> breakTest) {
        new ClassReader(stream).accept(new ClassVisitor(Opcodes.ASM9) {

            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                String annotationDescriptor = descriptor.substring(1, descriptor.length() - 1);
                String annotationName = annotationDescriptor.replace('/', '.');
                if (!container.contains(annotationName)) {
                    container.add(annotationName);
                    if (breakTest != null && breakTest.test(annotationName)) {
                        return null;
                    }
                    try {
                        getAnnotationNames(annotationDescriptor, container, breakTest);
                    } catch (ResolvableException e) {
                        return null;                                                                                    // 嵌套的注解可能不存在，忽略即可
                    }
                }
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return container;
    }
}
