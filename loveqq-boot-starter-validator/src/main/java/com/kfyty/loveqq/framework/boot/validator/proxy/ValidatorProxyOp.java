package com.kfyty.loveqq.framework.boot.validator.proxy;

import com.kfyty.loveqq.framework.boot.validator.context.ValidatorContext;
import org.hibernate.validator.internal.engine.valuecontext.BeanValueContext;
import org.hibernate.validator.internal.engine.valuecontext.ValueContext;

import java.lang.reflect.Method;

/**
 * 描述: {@link jakarta.validation.Validator} 代理操作接口
 *
 * @author kfyty725
 * @date 2023/4/20 16:44
 * @email kfyty725@hotmail.com
 */
public interface ValidatorProxyOp {
    /**
     * 执行的操作
     */
    Object doOp(ValidatorAccess validatorAccess, Method method, Object[] args) throws Throwable;

    /**
     * 执行的操作
     * 默认设置 {@link ValueContext}，然后继续执行
     */
    default Object doOp(ValidatorAccess validatorAccess, Method method, Object[] args, BeanValueContext<?, ?> valueContext) throws Throwable {
        ValueContext<?, ?> prev = ValidatorContext.getValueContext();
        try {
            ValidatorContext.setValueContext(valueContext);
            return method.invoke(validatorAccess.getValidator(), args);
        } finally {
            ValidatorContext.setValueContext(prev);
        }
    }
}
