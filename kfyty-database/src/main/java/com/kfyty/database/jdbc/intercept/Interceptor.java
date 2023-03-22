package com.kfyty.database.jdbc.intercept;

import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.generic.SimpleGeneric;
import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.lang.Value;

import java.sql.PreparedStatement;

/**
 * 描述: SQL 执行拦截器
 * 执行顺序由 {@link Order} 控制
 *
 * @author kfyty725
 * @date 2021/8/8 10:40
 * @email kfyty725@hotmail.com
 */
public interface Interceptor {

    @Order(10)
    default Object intercept(Value<String> sql, SimpleGeneric returnType, MethodParameter[] parameters, InterceptorChain chain) {
        return chain.proceed();
    }

    @Order(20)
    default Object intercept(PreparedStatement ps, SimpleGeneric returnType, MethodParameter[] parameters, InterceptorChain chain) {
        return chain.proceed();
    }
}
