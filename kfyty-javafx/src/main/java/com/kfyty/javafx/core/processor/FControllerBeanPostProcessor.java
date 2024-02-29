package com.kfyty.javafx.core.processor;

import com.kfyty.core.proxy.AbstractProxyCreatorProcessor;
import com.kfyty.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.core.utils.AopUtil;
import com.kfyty.javafx.core.annotation.FController;
import com.kfyty.javafx.core.proxy.CopyControllerComponentProxy;

import static com.kfyty.core.utils.AnnotationUtil.hasAnnotation;

/**
 * 描述: 由于 {@link FController} 注解的控制器初始化只能在 {@link javafx.fxml.Initializable} 中实现
 * 因此如果存在代理的话，fxml 组件会被解析到代理对象上，真实对象为null，所以如果存在代理的话，需要复制 fxml 组件
 *
 * @author kfyty725
 * @date 2021/6/13 17:27
 * @email kfyty725@hotmail.com
 */
public class FControllerBeanPostProcessor extends AbstractProxyCreatorProcessor {

    @Override
    public boolean canCreateProxy(String beanName, Class<?> beanType, Object bean) {
        return hasAnnotation(beanType, FController.class) && AopUtil.isProxy(bean);
    }

    @Override
    public MethodInterceptorChainPoint createProxyPoint() {
        return new CopyControllerComponentProxy();
    }
}
