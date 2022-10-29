package com.kfyty.database.jdbc.intercept;

import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.generic.SimpleGeneric;
import com.kfyty.core.method.MethodParameter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * 描述: 查询 SQL 拦截器
 *
 * @author kfyty725
 * @date 2021/8/8 13:30
 * @email kfyty725@hotmail.com
 */
public interface QueryInterceptor extends Interceptor {

    @Order(30)
    default Object intercept(PreparedStatement ps, ResultSet rs, SimpleGeneric returnType, List<MethodParameter> parameters, InterceptorChain chain) {
        return chain.proceed();
    }

    @Order(40)
    default Object intercept(PreparedStatement ps, ResultSet rs, Object retValue, List<MethodParameter> parameters, InterceptorChain chain) {
        return chain.proceed();
    }

    @Order(50)
    default Object intercept(ResultSet rs, Object retValue, List<MethodParameter> parameters, InterceptorChain chain) {
        return chain.proceed();
    }

    @Order(60)
    default Object intercept(Object retValue, List<MethodParameter> parameters, InterceptorChain chain) {
        return chain.proceed();
    }
}
