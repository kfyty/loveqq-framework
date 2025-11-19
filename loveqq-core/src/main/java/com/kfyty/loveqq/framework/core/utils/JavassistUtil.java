package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.support.Pair;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/8/25 14:04
 * @email kfyty725@hotmail.com
 */
public abstract class JavassistUtil {
    /**
     * 获取原始异常
     *
     * @param throwable 异常
     */
    public static Throwable unwrap(Throwable throwable) {
        if (throwable == throwable.getCause()) {
            return throwable;
        }
        if (throwable instanceof NotFoundException) {
            return throwable;
        }
        if (throwable instanceof CannotCompileException) {
            return unwrap(throwable.getCause());
        }
        if (throwable instanceof BadBytecode) {
            return unwrap(throwable.getCause());
        }
        return throwable;
    }

    /**
     * 基于 javassist 获取 {@link CtClass} 的指定 {@link CtMethod}
     *
     * @param ctClass {@link CtClass}
     * @return {@link CtMethod}
     */
    public static List<CtMethod> getMethods(CtClass ctClass, Collection<Pair<String, String>> methodNames) {
        try {
            if (ctClass == null || methodNames == null || methodNames.isEmpty()) {
                return Collections.emptyList();
            }
            List<CtMethod> ctMethods = new ArrayList<>(methodNames.size());
            for (Pair<String, String> method : methodNames) {
                ctMethods.add(ctClass.getMethod(method.getKey(), method.getValue()));
            }
            return ctMethods;
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }
}
