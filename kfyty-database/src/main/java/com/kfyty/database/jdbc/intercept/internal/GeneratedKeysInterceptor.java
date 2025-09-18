package com.kfyty.database.jdbc.intercept.internal;

import com.kfyty.database.jdbc.BaseMapper;
import com.kfyty.database.jdbc.annotation.TableId;
import com.kfyty.database.jdbc.exception.ExecuteInterceptorException;
import com.kfyty.database.jdbc.intercept.Interceptor;
import com.kfyty.database.jdbc.intercept.InterceptorChain;
import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.generic.SimpleGeneric;
import com.kfyty.core.jdbc.TransactionHolder;
import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.JdbcUtil;
import com.kfyty.core.utils.ResultSetUtil;
import com.kfyty.core.wrapper.ValueWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static com.kfyty.core.utils.AnnotationUtil.hasAnnotation;
import static com.kfyty.core.utils.ReflectUtil.getFieldMap;
import static com.kfyty.core.utils.ReflectUtil.getMethod;
import static com.kfyty.core.utils.ReflectUtil.setFieldValue;

/**
 * 描述: 主键自增拦截器，针对 {@link BaseMapper} 的插入方法回设主键值
 *
 * @author kfyty725
 * @date 2021/10/5 12:27
 * @email kfyty725@hotmail.com
 */
@Order(0)
public class GeneratedKeysInterceptor implements Interceptor {
    private static final Method INSERT = getMethod(BaseMapper.class, "insert", Object.class);
    private static final Method INSERT_BATCH = getMethod(BaseMapper.class, "insertBatch", List.class);
    private static final Predicate<Method> INSERT_METHOD_PREDICATE = method -> method.equals(INSERT) || method.equals(INSERT_BATCH);

    @Override
    public Object intercept(ValueWrapper<String> sql, SimpleGeneric returnType, MethodParameter[] parameters, InterceptorChain chain) {
        if (!INSERT_METHOD_PREDICATE.test(chain.getMapperMethod().getMethod())) {
            return chain.proceed();
        }
        try {
            Connection connection = TransactionHolder.currentTransaction().getConnection();
            chain.setPreparedStatement(JdbcUtil.getPreparedStatement(connection, sql.get(), (c, s) -> JdbcUtil.preparedStatement(c, s, Statement.RETURN_GENERATED_KEYS), parameters));
            return chain.proceed();
        } catch (SQLException e) {
            throw new ExecuteInterceptorException(e);
        }
    }

    @Override
    public Object intercept(PreparedStatement ps, SimpleGeneric returnType, MethodParameter[] parameters, InterceptorChain chain) {
        if (!INSERT_METHOD_PREDICATE.test(chain.getMapperMethod().getMethod())) {
            return chain.proceed();
        }
        Object retValue = chain.proceed();
        Field primaryKeyField = this.resolvePrimaryKeyField(chain.getMapperMethod().getMethodArgs());
        if (primaryKeyField != null) {
            this.processGeneratedKeys(ps, primaryKeyField, chain.getMapperMethod().getMethodArgs());
        }
        return retValue;
    }

    private Field resolvePrimaryKeyField(Object[] methodArgs) {
        if (CommonUtil.empty(methodArgs)) {
            return null;
        }
        return Arrays.stream(methodArgs)
                .filter(e -> !(e instanceof Collection))
                .findAny()
                .flatMap(e -> getFieldMap(e.getClass()).values().stream().filter(f -> hasAnnotation(f, TableId.class)).findAny())
                .orElse(null);
    }

    private void processGeneratedKeys(PreparedStatement ps, Field pkField, Object[] params) {
        try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
            int index = 0;
            List<?> list = ResultSetUtil.processListBaseType(generatedKeys, pkField.getType());
            for (Object param : params) {
                if (!(param instanceof Collection)) {
                    setFieldValue(param, pkField, list.get(index++));
                }
            }
        } catch (SQLException e) {
            throw new ExecuteInterceptorException(e);
        }
    }
}
