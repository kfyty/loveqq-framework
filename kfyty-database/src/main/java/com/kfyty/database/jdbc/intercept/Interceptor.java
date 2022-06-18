package com.kfyty.database.jdbc.intercept;

import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.generic.SimpleGeneric;
import com.kfyty.support.method.MethodParameter;
import com.kfyty.support.wrapper.ValueWrapper;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * 描述: SQL 执行拦截器
 * 执行顺序由 {@link com.kfyty.support.autoconfig.annotation.Order} 控制
 *
 * @author kfyty725
 * @date 2021/8/8 10:40
 * @email kfyty725@hotmail.com
 */
public interface Interceptor {

    @Order(10)
    default Object intercept(ValueWrapper<String> sql, SimpleGeneric returnType, List<MethodParameter> parameters, InterceptorChain chain) {
        return chain.proceed();
    }

    @Order(20)
    default Object intercept(PreparedStatement ps, SimpleGeneric returnType, List<MethodParameter> parameters, InterceptorChain chain) {
        return chain.proceed();
    }
}
