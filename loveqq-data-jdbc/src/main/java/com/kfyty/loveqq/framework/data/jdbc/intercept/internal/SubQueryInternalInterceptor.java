package com.kfyty.loveqq.framework.data.jdbc.intercept.internal;

import com.kfyty.database.jdbc.exception.ExecuteInterceptorException;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import com.kfyty.loveqq.framework.core.lang.Value;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.data.jdbc.annotation.Query;
import com.kfyty.loveqq.framework.data.jdbc.annotation.SubQuery;
import com.kfyty.loveqq.framework.data.jdbc.intercept.InterceptorChain;
import com.kfyty.loveqq.framework.data.jdbc.intercept.QueryInterceptor;
import com.kfyty.loveqq.framework.data.jdbc.session.SqlSession;
import com.kfyty.loveqq.framework.data.jdbc.util.SQLParametersResolveUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.invokeMethod;

/**
 * 描述: {@link SubQuery} 注解处理器
 * <p>
 * 解析 ForEach 注解，并将对应的参数添加到 Map
 *
 * @author kfyty725
 * @date 2021/10/5 12:27
 * @email kfyty725@hotmail.com
 */
@Order(Integer.MAX_VALUE)
public class SubQueryInternalInterceptor implements QueryInterceptor {

    @Override
    public Object intercept(Object retValue, List<MethodParameter> parameters, InterceptorChain chain) {
        Annotation annotation = chain.getAnnotation();
        if (retValue == null || !(annotation instanceof Query)) {
            return chain.proceed();
        }
        SubQuery[] subQueries = invokeMethod(annotation, "subQuery");
        CommonUtil.consumer(retValue, e -> this.processSubQuery(chain.getSqlSession(), chain.getMapperMethod().getMethod(), subQueries, e), Map.Entry::getValue);
        return chain.proceed();
    }

    /**
     * 处理子查询
     *
     * @param subQueries 子查询注解
     * @param retValue        父查询映射的结果对象，若是集合，则为其中的每一个对象
     */
    protected void processSubQuery(SqlSession sqlSession, Method mapperMethod, SubQuery[] subQueries, Object retValue) {
        if (CommonUtil.empty(subQueries)) {
            return;
        }
        try {
            for (SubQuery subQuery : subQueries) {
                Field returnField = ReflectUtil.getField(retValue.getClass(), subQuery.returnField());
                Map<String, MethodParameter> params = SQLParametersResolveUtil.resolveMappingParameters(subQuery.paramField(), subQuery.mapperField(), retValue);
                SimpleGeneric returnType = SimpleGeneric.from(returnField);
                ReflectUtil.setFieldValue(retValue, returnField, sqlSession.requestExecuteSQL(mapperMethod, new Value<>(subQuery), returnType, params));
            }
        } catch (SQLException e) {
            throw new ExecuteInterceptorException("processSubQuery failed", e);
        }
    }
}
