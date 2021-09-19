package com.kfyty.database.jdbc.intercept;

import com.kfyty.database.jdbc.annotation.Execute;
import com.kfyty.database.jdbc.exception.ExecuteInterceptorException;
import com.kfyty.support.generic.SimpleGeneric;
import com.kfyty.support.jdbc.TransactionHolder;
import com.kfyty.support.method.MethodParameter;
import com.kfyty.support.utils.JdbcUtil;
import com.kfyty.support.utils.ReflectUtil;
import com.kfyty.support.utils.ResultSetUtil;
import com.kfyty.support.wrapper.WrapperValue;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.kfyty.support.utils.AnnotationUtil.hasAnnotationElement;
import static java.util.Optional.ofNullable;

/**
 * 描述: SQL 执行拦截器链
 *
 * @author kfyty725
 * @date 2021/9/19 11:12
 * @email kfyty725@hotmail.com
 */
public class InterceptorChain {
    private final Method mapperMethod;
    private final WrapperValue<String> sql;
    private final SimpleGeneric returnType;
    private final List<MethodParameter> methodParameters;
    private final Iterator<Map.Entry<Method, Interceptor>> interceptors;
    private final Supplier<Object> sourceReturnValueSupplier;

    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private Object retValue;

    public InterceptorChain(Method method, String sql, SimpleGeneric returnType, List<MethodParameter> methodParameters, Iterator<Map.Entry<Method, Interceptor>> interceptors, Supplier<Object> sourceReturnValueSupplier) {
        this.mapperMethod = method;
        this.sql = new WrapperValue<>(sql);
        this.returnType = returnType;
        this.methodParameters = new ArrayList<>(methodParameters);
        this.interceptors = interceptors;
        this.sourceReturnValueSupplier = sourceReturnValueSupplier;
    }

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public Object proceed() {
        if (!this.interceptors.hasNext()) {
            return ofNullable(this.retValue).orElseGet(this.sourceReturnValueSupplier);
        }
        Map.Entry<Method, Interceptor> interceptor = this.interceptors.next();
        if (interceptor.getValue() instanceof QueryInterceptor && hasAnnotationElement(this.mapperMethod, Execute.class)) {
            return this.proceed();
        }
        return this.retValue = ReflectUtil.invokeMethod(interceptor.getValue(), interceptor.getKey(), this.bindInterceptorParameters(interceptor.getKey()));
    }

    protected Object[] bindInterceptorParameters(Method method) {
        Parameter[] parameters = method.getParameters();
        Object[] paramValues = new Object[method.getParameterCount()];
        for (byte i = 0, size = (byte) parameters.length; i < size; i++) {
            Class<?> parameterType = parameters[i].getType();
            if (WrapperValue.class.isAssignableFrom(parameterType)) {
                paramValues[i] = this.sql;
                continue;
            }
            if (SimpleGeneric.class.isAssignableFrom(parameterType)) {
                paramValues[i] = this.returnType;
                continue;
            }
            if (List.class.isAssignableFrom(parameterType)) {
                paramValues[i] = this.methodParameters;
                continue;
            }
            if (PreparedStatement.class.isAssignableFrom(parameterType)) {
                paramValues[i] = this.preparePreparedStatement();
                continue;
            }
            if (ResultSet.class.isAssignableFrom(parameterType)) {
                paramValues[i] = this.prepareResultSet();
                continue;
            }
            if (InterceptorChain.class.isAssignableFrom(parameterType)) {
                paramValues[i] = this;
                continue;
            }
            if (Object.class.equals(parameterType)) {
                paramValues[i] = this.prepareReturnValue();
                continue;
            }
            throw new IllegalArgumentException("interceptor parameter bind failed of parameter: " + parameters[i]);
        }
        return paramValues;
    }

    private PreparedStatement preparePreparedStatement() {
        if (this.preparedStatement == null) {
            try {
                this.preparedStatement = JdbcUtil.getPreparedStatement(TransactionHolder.currentTransaction().getConnection(), this.sql.get(), this.methodParameters.toArray(new MethodParameter[0]));
            } catch (SQLException e) {
                throw new ExecuteInterceptorException(e);
            }
        }
        return this.preparedStatement;
    }

    private ResultSet prepareResultSet() {
        if (this.resultSet == null) {
            try {
                this.resultSet = this.preparePreparedStatement().executeQuery();
            } catch (SQLException e) {
                throw new ExecuteInterceptorException(e);
            }
        }
        return this.resultSet;
    }

    private Object prepareReturnValue() {
        if (this.retValue == null) {
            try {
                this.retValue = ResultSetUtil.processObject(this.prepareResultSet(), this.returnType);
            } catch (SQLException e) {
                throw new ExecuteInterceptorException(e);
            }
        }
        return this.retValue;
    }
}
