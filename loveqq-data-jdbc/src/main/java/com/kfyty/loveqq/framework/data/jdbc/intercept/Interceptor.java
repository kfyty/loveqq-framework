package com.kfyty.loveqq.framework.data.jdbc.intercept;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import com.kfyty.loveqq.framework.core.lang.Value;
import com.kfyty.loveqq.framework.core.lang.annotation.Inherited;
import com.kfyty.loveqq.framework.core.method.MethodParameter;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 描述: SQL 执行拦截器
 * 执行顺序由 {@link Order} 控制
 *
 * @author kfyty725
 * @date 2021/8/8 10:40
 * @email kfyty725@hotmail.com
 */
public interface Interceptor extends Inherited {

    @Order(10)
    default Object intercept(Value<String> sql, SimpleGeneric returnType, List<MethodParameter> parameters, InterceptorChain chain) {
        return chain.proceed();
    }

    @Order(20)
    default Object intercept(PreparedStatement ps, SimpleGeneric returnType, List<MethodParameter> parameters, InterceptorChain chain) {
        return chain.proceed();
    }

    default Map<String, MethodParameter> getParameterMap(InterceptorChain chain) {
        return Arrays.stream(chain.getMapperMethod().getMethodParameters()).collect(Collectors.toMap(MethodParameter::getParamName, Function.identity()));
    }
}
