package com.kfyty.loveqq.framework.core.utils;

import javassist.CtClass;
import javassist.CtMethod;

import java.util.ArrayList;
import java.util.Arrays;
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

    public static List<CtMethod> getMethods(CtClass ctClass) {
        try {
            if (ctClass == null) {
                return Collections.emptyList();
            }
            CtClass superClass = ctClass.getSuperclass();
            CtClass[] interfaces = ctClass.getInterfaces();
            List<CtMethod> ctMethods = new ArrayList<>(Arrays.asList(ctClass.getDeclaredMethods()));
            if (superClass != null) {
                ctMethods.addAll(getMethods(superClass));
            }
            for (CtClass anInterface : interfaces) {
                ctMethods.addAll(getMethods(anInterface));
            }
            return ctMethods;
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }
}
