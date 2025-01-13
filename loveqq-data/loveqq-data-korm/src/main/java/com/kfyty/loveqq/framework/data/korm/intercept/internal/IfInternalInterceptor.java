package com.kfyty.loveqq.framework.data.korm.intercept.internal;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import com.kfyty.loveqq.framework.core.lang.Value;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.OgnlUtil;
import com.kfyty.loveqq.framework.data.korm.annotation.ForEach;
import com.kfyty.loveqq.framework.data.korm.annotation.If;
import com.kfyty.loveqq.framework.data.korm.intercept.Interceptor;
import com.kfyty.loveqq.framework.data.korm.intercept.InterceptorChain;
import com.kfyty.loveqq.framework.data.korm.util.ForEachUtil;
import com.kfyty.loveqq.framework.data.korm.util.SQLParametersResolveUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.invokeMethod;

/**
 * 描述: {@link If} 注解处理器
 * <p>
 * 解析 ForEach 注解，并将对应的参数添加到 Map
 *
 * @author kfyty725
 * @date 2021/10/5 12:27
 * @email kfyty725@hotmail.com
 */
@Order(Integer.MIN_VALUE)
public class IfInternalInterceptor implements Interceptor {

    @Override
    public Object intercept(Value<String> sql, SimpleGeneric returnType, List<MethodParameter> parameters, InterceptorChain chain) {
        If[] ifList = invokeMethod(chain.getAnnotation(), "_if");
        String last = invokeMethod(chain.getAnnotation(), "last");
        if (CommonUtil.empty(ifList)) {
            sql.set(sql.get() + ' ' + last);
            return chain.proceed();
        }


        Map<String, MethodParameter> parameterMap = this.getParameterMap(chain);
        String processedSQL = this.processIf(sql, last, ifList, parameterMap);

        Pair<String, MethodParameter[]> sqlParams = SQLParametersResolveUtil.resolveSQL(processedSQL, parameterMap);
        sql.set(sqlParams.getKey());
        parameters.addAll(Arrays.asList(sqlParams.getValue()));

        return chain.proceed();
    }

    public String processIf(Value<String> sql, String last, If[] ifList, Map<String, MethodParameter> parameterMap) {
        Map<String, Object> conditionContext = parameterMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getValue()));
        StringBuilder builder = new StringBuilder(sql.get());
        for (If anIf : ifList) {
            if (OgnlUtil.getBoolean(anIf.test(), conditionContext)) {
                builder.append(' ').append(anIf.value());
                ForEach[] forEachList = anIf.forEach();
                if (CommonUtil.empty(forEachList)) {
                    continue;
                }
                String forEach = ForEachUtil.processForEach(parameterMap, forEachList);
                builder.append(forEach);
            }
        }

        If lastIf = ifList[ifList.length - 1];
        if (CommonUtil.notEmpty(lastIf.trim())) {
            int index = builder.lastIndexOf(lastIf.trim());
            if (builder.length() - index == lastIf.trim().length()) {
                builder.delete(index, builder.length());
            }
        }

        return builder.append(' ').append(last).toString();
    }
}
