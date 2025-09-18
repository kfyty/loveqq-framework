package com.kfyty.loveqq.framework.web.core;

import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.proxy.factory.DynamicProxyFactory;
import com.kfyty.loveqq.framework.web.core.annotation.ControllerAdvice;
import com.kfyty.loveqq.framework.web.core.annotation.ExceptionHandler;
import com.kfyty.loveqq.framework.web.core.handler.AnnotatedExceptionHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/9/22 20:12
 * @email kfyty725@hotmail.com
 */
public class ExceptionHandlerTest {

    interface Ex {
        String onEx(Throwable e);
    }

    @ControllerAdvice
    static class ExImpl implements Ex {

        @Override
        @ExceptionHandler
        public String onEx(Throwable e) {
            return null;
        }
    }

    @Test
    public void test() {
        Ex proxy = DynamicProxyFactory.create(ExImpl.class).createProxy(new ExImpl());
        AnnotatedExceptionHandler handler = new AnnotatedExceptionHandler(null, null, null, new Lazy<>(() -> proxy));
        handler.afterPropertiesSet();
        MethodParameter exceptionAdvice = handler.findControllerExceptionAdvice(null, null, null, new RuntimeException());
        Assertions.assertNotNull(exceptionAdvice);
        Assertions.assertEquals(exceptionAdvice.getMethod().getDeclaringClass(), Ex.class);
    }
}
