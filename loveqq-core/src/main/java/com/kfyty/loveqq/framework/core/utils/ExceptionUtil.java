package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/**
 * 描述: 异常工具类
 *
 * @author kfyty725
 * @date 2022/2/25 19:42
 * @email kfyty725@hotmail.com
 */
public abstract class ExceptionUtil {

    public static String buildStackTrace(Throwable throwable) {
        if (throwable == null) {
            return CommonUtil.EMPTY_STRING;
        }
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer, true));
        return writer.toString();
    }

    public static ResolvableException wrap(Throwable throwable) {
        if (throwable instanceof ResolvableException) {
            return (ResolvableException) throwable;
        }
        if (throwable instanceof CompletionException && throwable.getCause() != null) {
            return wrap(throwable.getCause());
        }
        if (throwable instanceof ExecutionException && throwable.getCause() != null) {
            return wrap(throwable.getCause());
        }
        if (throwable instanceof UndeclaredThrowableException) {
            Throwable undeclaredThrowable = ((UndeclaredThrowableException) throwable).getUndeclaredThrowable();
            if (undeclaredThrowable != null) {
                return wrap(undeclaredThrowable);
            }
        }
        if (throwable instanceof InvocationTargetException) {
            Throwable targetException = ((InvocationTargetException) throwable).getTargetException();
            if (targetException != null) {
                return wrap(targetException);
            }
        }
        return new ResolvableException(throwable);
    }

    public static Throwable unwrap(Throwable throwable) {
        return wrap(throwable).getCause();
    }
}
