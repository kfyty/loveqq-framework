package com.kfyty.loveqq.framework.javafx.core.processor;

import com.kfyty.loveqq.framework.core.proxy.AbstractProxyCreatorProcessor;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.javafx.core.annotation.FController;
import com.kfyty.loveqq.framework.javafx.core.proxy.CopyControllerComponentProxy;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.hasAnnotation;

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
        return AopUtil.isProxy(bean) && hasAnnotation(beanType, FController.class);
    }

    @Override
    public MethodInterceptorChainPoint createProxyPoint() {
        return new CopyControllerComponentProxy();
    }
}
