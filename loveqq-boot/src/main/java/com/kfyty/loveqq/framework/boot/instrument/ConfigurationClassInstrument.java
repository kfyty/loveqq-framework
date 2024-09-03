package com.kfyty.loveqq.framework.boot.instrument;

import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.ExceptionUtil;
import com.kfyty.loveqq.framework.core.utils.JavassistUtil;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.annotation.NoSuchClassError;

import java.io.ByteArrayInputStream;
import java.lang.annotation.Annotation;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

/**
 * 描述: {@link com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration} 提供自调用支持
 * 仅当前类加载器是 {@link com.kfyty.loveqq.framework.core.lang.JarIndexClassLoader}
 * 且 {@link com.kfyty.loveqq.framework.core.lang.ConstantConfig#LOAD_TRANSFORMER} == true 时有效
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

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        Pair<Boolean, CtClass> isConfigurationClass = this.isConfigurationClass(classfileBuffer);
        if (isConfigurationClass.getKey()) {
            return this.enhanceConfigurationClass(classfileBuffer, isConfigurationClass.getValue());
        }
        return null;
    }

    protected Pair<Boolean, CtClass> isConfigurationClass(byte[] classfile) {
        try {
            ClassPool classPool = ClassPool.getDefault();
            CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfile), false);
            if (ctClass.isFrozen()) {
                return new Pair<>(false, null);
            }
            for (Object annotation : ctClass.getAnnotations()) {
                Annotation anno = (Annotation) annotation;
                if (CONFIGURATION_CLASS.equals(anno.annotationType().getName())) {
                    return new Pair<>(true, ctClass);
                }
                for (Annotation nested : AnnotationUtil.findAnnotations(anno.annotationType())) {
                    if (CONFIGURATION_CLASS.equals(nested.annotationType().getName())) {
                        return new Pair<>(true, ctClass);
                    }
                }
            }
            return new Pair<>(false, null);
        } catch (NoSuchClassError | ClassNotFoundException error) {
            return new Pair<>(false, null);                         // 某些非运行时注解会出现，忽略
        } catch (Throwable e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    protected byte[] enhanceConfigurationClass(byte[] classfileBuffer, CtClass ctClass) {
        try {
            boolean hasBean = false;
            List<CtMethod> methods = JavassistUtil.getMethods(ctClass);
            for (CtMethod method : methods) {
                if (method.isEmpty() || method.getDeclaringClass().getName().equals("java.lang.Object")) {
                    continue;
                }
                Object[] annotations = method.getAnnotations();
                for (Object annotation : annotations) {
                    if (((Annotation) annotation).annotationType().getName().equals(BEAN_CLASS)) {
                        String value = (String) annotation.getClass().getMethod("value").invoke(annotation);
                        method.insertBefore(this.getBeforeEnhanceCode(method.getName(), value));
                        method.insertAfter(this.getAfterEnhanceCode(method.getName(), value));
                        hasBean = true;
                        break;
                    }
                }
            }
            return hasBean ? ctClass.toBytecode() : classfileBuffer;
        } catch (Exception e) {
            if (e.getCause() instanceof NotFoundException) {
                return classfileBuffer;
            }
            throw ExceptionUtil.wrap(e);
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
