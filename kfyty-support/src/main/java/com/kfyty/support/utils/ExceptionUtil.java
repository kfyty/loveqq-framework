package com.kfyty.support.utils;

import com.kfyty.support.exception.SupportException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

import static com.kfyty.support.utils.CommonUtil.EMPTY_STRING;

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
            return EMPTY_STRING;
        }
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter, true));
        return stringWriter.toString();
    }

    public static SupportException wrap(Throwable throwable) {
        if (throwable instanceof SupportException) {
            return (SupportException) throwable;
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
        return new SupportException(throwable);
    }

    public static Throwable unwrap(Throwable throwable) {
        return wrap(throwable).getCause();
    }
}
