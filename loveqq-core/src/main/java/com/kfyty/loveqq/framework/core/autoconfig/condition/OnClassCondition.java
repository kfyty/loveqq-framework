package com.kfyty.loveqq.framework.core.autoconfig.condition;

import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnClass;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.lang.JarIndexClassLoader;
import com.kfyty.loveqq.framework.core.support.AnnotationMetadata;
import com.kfyty.loveqq.framework.core.thread.ContextRefreshThread;
import com.kfyty.loveqq.framework.core.utils.ClassLoaderUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;

import java.io.IOException;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/4/17 11:43
 * @email kfyty725@hotmail.com
 */
public class OnClassCondition implements Condition {

    @Override
    public boolean isMatch(ConditionContext context, AnnotationMetadata<?> metadata) {
        for (String conditionName : this.conditionNames(metadata)) {
            if (!isPresent(conditionName)) {
                return false;
            }
        }
        Class<?>[] classes = this.conditionClasses(metadata);
        if (classes != null) {
            for (Class<?> clazz : classes) {
                if (!isPresent(clazz.getName())) {                                                                      // 目的是全量刷新时，可能 class loader 已经卸载
                    return false;
                }
            }
        }
        return true;
    }

    protected String[] conditionNames(AnnotationMetadata<?> metadata) {
        return ((ConditionalOnClass) metadata.get()).value();
    }

    protected Class<?>[] conditionClasses(AnnotationMetadata<?> metadata) {
        try {
            return ((ConditionalOnClass) metadata.get()).classes();
        } catch (Throwable e) {
            return null;
        }
    }

    protected boolean isPresent(String className) {
        ClassLoader classLoader = ClassLoaderUtil.classLoader(OnClassCondition.class);

        if (classLoader instanceof JarIndexClassLoader jcl) {

            if (Thread.currentThread() instanceof ContextRefreshThread) {
                try (JarIndexClassLoader cl = new JarIndexClassLoader(jcl.getJarIndex(), jcl.getParent())) {
                    cl.loadClass(className);
                    return true;
                } catch (ClassNotFoundException e) {
                    return false;
                } catch (IOException e) {
                    throw new ResolvableException(e);
                }
            }

            return ReflectUtil.isPresent(className);
        }

        if (Thread.currentThread() instanceof ContextRefreshThread) {
            throw new IllegalStateException("The class loader doesn't support ConditionalOnClass/ConditionalOnMissingClass conditional refresh.");
        }

        return ReflectUtil.isPresent(className);
    }
}
