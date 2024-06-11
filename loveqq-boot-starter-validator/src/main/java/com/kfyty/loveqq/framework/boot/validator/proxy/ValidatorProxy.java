package com.kfyty.loveqq.framework.boot.validator.proxy;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 描述: {@link jakarta.validation.Validator} 代理类
 *
 * @author kfyty725
 * @date 2023/4/20 16:43
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class ValidatorProxy implements InvocationHandler {
    /**
     * 实际校验器
     */
    private final ValidatorAccess validatorAccess;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return ValidatorProxyEnum.forName(method.getName()).doOp(this.validatorAccess, method, args);
    }
}
