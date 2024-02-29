package com.kfyty.javafx.core.proxy;

import com.kfyty.core.proxy.MethodInterceptorChain;
import com.kfyty.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.core.proxy.MethodProxy;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.BeanUtil;
import javafx.fxml.FXML;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 描述: 复制 fxml 组件代理
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
public class CopyControllerComponentProxy implements MethodInterceptorChainPoint {
    /**
     * 是否已复制过
     */
    private final AtomicBoolean copied = new AtomicBoolean(false);

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        if (!this.copied.get()) {
            this.copied.set(true);
            BeanUtil.copyProperties(methodProxy.getProxy(), methodProxy.getTarget(), (f, v) -> AnnotationUtil.hasAnnotation(f, FXML.class));
        }
        return chain.proceed(methodProxy);
    }
}
