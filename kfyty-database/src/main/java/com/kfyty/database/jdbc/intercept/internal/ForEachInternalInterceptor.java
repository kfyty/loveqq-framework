package com.kfyty.database.jdbc.intercept.internal;

import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.generic.SimpleGeneric;
import com.kfyty.core.lang.Value;
import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.support.Pair;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.database.jdbc.annotation.ForEach;
import com.kfyty.database.jdbc.intercept.Interceptor;
import com.kfyty.database.jdbc.intercept.InterceptorChain;
import com.kfyty.database.util.ForEachUtil;
import com.kfyty.database.util.SQLParametersResolveUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.kfyty.core.utils.ReflectUtil.invokeMethod;

/**
 * 描述: {@link com.kfyty.database.jdbc.annotation.ForEach} 注解处理器
 * <p>
 * 解析 ForEach 注解，并将对应的参数添加到 Map
 *
 * @author kfyty725
 * @date 2021/10/5 12:27
 * @email kfyty725@hotmail.com
 */
@Order(Integer.MIN_VALUE)
public class ForEachInternalInterceptor implements Interceptor {

    @Override
    public Object intercept(Value<String> sql, SimpleGeneric returnType, List<MethodParameter> parameters, InterceptorChain chain) {
        ForEach[] forEachList = invokeMethod(chain.getAnnotation(), "forEach");
        if (CommonUtil.empty(forEachList)) {
            return chain.proceed();
        }
        Map<String, MethodParameter> parameterMap = this.getParameterMap(chain);
        String forEach = ForEachUtil.processForEach(parameterMap, forEachList);

        // 更新 SQL 解析
        Pair<String, MethodParameter[]> sqlParams = SQLParametersResolveUtil.resolveSQL(sql + forEach, parameterMap);
        sql.set(sqlParams.getKey());
        parameters.addAll(Arrays.asList(sqlParams.getValue()));
        return chain.proceed();
    }
}
