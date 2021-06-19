package com.kfyty.support.proxy.factory;

import com.kfyty.support.proxy.InterceptorChain;
import com.kfyty.support.utils.CommonUtil;
import lombok.NoArgsConstructor;
import net.sf.cglib.proxy.Enhancer;

/**
 * 描述: cglib 动态代理工厂
 *
 * @author kfyty725
 * @date 2021/6/19 11:50
 * @email kfyty725@hotmail.com
 */
@NoArgsConstructor
public class CglibDynamicProxyFactory extends DynamicProxyFactory {

    @Override
    public Object createProxy(Object source) {
        return createProxy(source, CommonUtil.EMPTY_CLASS_ARRAY, CommonUtil.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public Object createProxy(Object source, Class<?>[] argTypes, Object[] arsValues) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(source.getClass());
        enhancer.setCallback(new InterceptorChain(source));
        return enhancer.create(argTypes, arsValues);
    }
}
