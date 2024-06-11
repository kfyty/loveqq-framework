package com.kfyty.loveqq.framework.boot.validator.agent;

import com.kfyty.loveqq.framework.boot.validator.context.ValidatorContext;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * 描述: 增强 {@link org.hibernate.validator.internal.engine.valuecontext.ValueContext}
 *
 * @author kfyty725
 * @date 2023/4/14 15:33
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class ValidatorValueContextInstrumentation implements ClassFileTransformer {
    private static final String VALUE_CONTEXT_CLASS_NAME = "org/hibernate/validator/internal/engine/valuecontext/ValueContext";
    private static final String ENHANCE_METHOD_NAME = "getCurrentBean";

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws IllegalClassFormatException {
        if (!VALUE_CONTEXT_CLASS_NAME.equals(className)) {
            return null;
        }
        return this.enhanceMethod(classFileBuffer);
    }

    protected byte[] enhanceMethod(byte[] classFileBuffer) {
        try {
            ClassPool classPool = ClassPool.getDefault();
            CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classFileBuffer));
            CtMethod ctMethod = ctClass.getDeclaredMethod(ENHANCE_METHOD_NAME);
            ctMethod.insertAfter(ValidatorContext.class.getName() + ".setValueContext(this);");
            return ctClass.toBytecode();
        } catch (Throwable e) {
            log.error("enhanceMethod failed !", e);
            return null;
        }
    }
}
