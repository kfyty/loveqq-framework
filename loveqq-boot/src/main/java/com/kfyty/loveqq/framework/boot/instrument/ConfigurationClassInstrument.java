package com.kfyty.loveqq.framework.boot.instrument;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.AsmUtil;
import com.kfyty.loveqq.framework.core.utils.JavassistUtil;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * 描述: {@link com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration} 提供自调用支持
 * 仅当前类加载器是 {@link com.kfyty.loveqq.framework.core.lang.JarIndexClassLoader}
 * 且 {@link com.kfyty.loveqq.framework.core.lang.ConstantConfig#LOAD_TRANSFORMER} == true，并且父类是 {@link Object} 时有效
 *
 * @author kfyty725
 * @date 2024/8/26 13:40
 * @email kfyty725@hotmail.com
 * @see com.kfyty.loveqq.framework.core.lang.JarIndexClassLoader
 * @see com.kfyty.loveqq.framework.core.lang.ConstantConfig#LOAD_TRANSFORMER
 */
public class ConfigurationClassInstrument implements ClassFileTransformer {
    /**
     * {@link com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration} class name
     */
    private static final String CONFIGURATION_CLASS = "com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration";

    /**
     * {@link com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean} class name
     */
    private static final String BEAN_CLASS = "com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean";

    /**
     * 增强字节码，只有 {@link com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration} 标注的类会增强
     * <p>
     * 但是如果该类具有父类，并且父类不是 {@link Object} 则不会进行字节码增强，而是通过动态代理增强
     *
     * @param loader              the defining loader of the class to be transformed,
     *                            may be {@code null} if the bootstrap loader
     * @param className           the name of the class in the internal form of fully
     *                            qualified class and interface names as defined in
     *                            <i>The Java Virtual Machine Specification</i>.
     *                            For example, <code>"java/util/List"</code>.
     * @param classBeingRedefined if this is triggered by a redefine or retransform,
     *                            the class being redefined or retransformed;
     *                            if this is a class load, {@code null}
     * @param protectionDomain    the protection domain of the class being defined or redefined
     * @param classfileBuffer     the input byte buffer in class file format - must not be modified
     */
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        Pair<Boolean, CtClass> isConfigurationClass = this.isConfigurationClass(classfileBuffer);
        if (isConfigurationClass.getKey()) {
            return this.enhanceConfigurationClass(classfileBuffer, isConfigurationClass.getValue());
        }
        return null;
    }

    /**
     * 判断是否是 {@link com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration} 配置类
     *
     * @param classfile 字节码
     * @return 是否配置类，以及对应的 {@link CtClass}
     */
    @SneakyThrows(IOException.class)
    protected Pair<Boolean, CtClass> isConfigurationClass(byte[] classfile) {
        Collection<String> annotationNames = AsmUtil.getClassAnnotationNames(classfile, new HashSet<>(4), e -> e.equals(CONFIGURATION_CLASS), true);
        for (String annotationName : annotationNames) {
            if (annotationName.equals(CONFIGURATION_CLASS)) {
                CtClass ctClass = ClassPool.getDefault().makeClass(new ByteArrayInputStream(classfile), false);
                return new Pair<>(!ctClass.isFrozen(), ctClass);
            }
        }
        return new Pair<>(false, null);
    }

    /**
     * 判断是否有父类的 bean 方法
     *
     * @param ctClass {@link CtClass}
     * @return true/false
     */
    protected boolean hasSuperMethod(CtClass ctClass) {
        try {
            return !ctClass.getSuperclass().getName().equals("java.lang.Object");
        } catch (NotFoundException e) {
            return true;
        }
    }

    /**
     * 增强配置类字节码
     *
     * @param classfileBuffer 字节码
     * @param ctClass         {@link CtClass}
     * @return 增强后的字节码
     */
    protected byte[] enhanceConfigurationClass(byte[] classfileBuffer, CtClass ctClass) {
        if (this.hasSuperMethod(ctClass)) {
            return classfileBuffer;
        }
        Collection<Pair<String, String>> methodNames = AsmUtil.getMethodNames(classfileBuffer, new LinkedList<>(), null, e -> e.equals(BEAN_CLASS));
        List<CtMethod> methods = JavassistUtil.getMethods(ctClass, methodNames);
        if (methods.isEmpty()) {
            return classfileBuffer;
        }
        try {
            for (CtMethod method : methods) {
                if (!method.isEmpty()) {
                    Object[] annotations = method.getAvailableAnnotations();
                    for (Object annotation : annotations) {
                        if (((Annotation) annotation).annotationType().getName().equals(BEAN_CLASS)) {
                            String value = (String) annotation.getClass().getMethod("value").invoke(annotation);
                            method.insertBefore(this.getBeforeEnhanceCode(method.getName(), value));
                            method.insertAfter(this.getAfterEnhanceCode(method.getName(), value));
                            break;
                        }
                    }
                }
            }
            return ctClass.toBytecode();
        } catch (Throwable e) {
            Throwable throwable = JavassistUtil.unwrap(e);
            if (throwable instanceof NotFoundException) {
                return classfileBuffer;
            }
            throw new ResolvableException(e.getMessage(), throwable);
        }
    }

    protected String getBeforeEnhanceCode(String methodName, String annotationValue) {
        return this.getCommonEnhanceCode(methodName, annotationValue) +
                "if (bf.contains(bd.getBeanName())) {" +
                "   return ($r) bf.getBean(bd.getBeanName());" +
                "}";
    }

    protected String getAfterEnhanceCode(String methodName, String annotationValue) {
        return this.getCommonEnhanceCode(methodName, annotationValue) +
                "if (!bf.contains(bd.getBeanName())) {" +
                "   return ($r) bf.registerBean(bd.getBeanName(), $_);" +
                "}";
    }

    /**
     * 这里的逻辑要分三种：
     * <p>
     * 1、require == null
     * <p>
     * 此时一定是方法直接调用，应直接返回 bean name 对应的 bean 即可
     * </p>
     * 2、require != null
     * <p>
     * 此时分两种情况
     * <p>
     * a、BeanFactory 创建 bean 实例，此时 require 即为实际正在创建中的 bean，忽略注解值，直接取 require 作为 bean name 即可
     * </p>
     * <p>
     * b、方法直接调用，此时的 require 可能是其他正在创建的 bean，和当前方法返回可能的不一致，此时应直接返回 bean name 对应的 bean 即可
     * </p>
     * </p>
     * </p>
     *
     * @param methodName      bean 方法名称
     * @param annotationValue {@link com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean} 注解值
     * @return java source code
     */
    protected String getCommonEnhanceCode(String methodName, String annotationValue) {
        String beanName = annotationValue != null && !annotationValue.isEmpty() ? annotationValue : methodName;
        return "String required = com.kfyty.loveqq.framework.boot.context.factory.AbstractBeanFactory.getCreatingBean();" +
                "String beanName = (com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBean.class.isAssignableFrom($type) ? \"&\" : \"\") + \"" + beanName + "\";" +
                "com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory bf = com.kfyty.loveqq.framework.core.utils.IOC.getBeanFactory();" +
                "com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition bd = bf.getBeanDefinition(beanName, $type);" +
                "if (required != null && bf.getBeanDefinition(required).getBeanType() == $type) {" +
                "   bd = bf.getBeanDefinition(required);" +
                "}";
    }
}
