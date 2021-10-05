package com.kfyty.database.jdbc.intercept;

import com.kfyty.database.jdbc.annotation.Execute;
import com.kfyty.database.jdbc.exception.ExecuteInterceptorException;
import com.kfyty.support.generic.SimpleGeneric;
import com.kfyty.support.jdbc.TransactionHolder;
import com.kfyty.support.method.MethodParameter;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.JdbcUtil;
import com.kfyty.support.utils.ReflectUtil;
import com.kfyty.support.utils.ResultSetUtil;
import com.kfyty.support.wrapper.WrapperValue;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.kfyty.support.utils.JdbcUtil.commitTransactionIfNecessary;

/**
 * 描述: SQL 执行拦截器链
 *
 * @author kfyty725
 * @date 2021/9/19 11:12
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class InterceptorChain implements AutoCloseable {
    private final MethodParameter mapperMethod;
    private final Annotation annotation;
    private final WrapperValue<String> sql;
    private final SimpleGeneric returnType;
    private final List<MethodParameter> methodParameters;
    private final Iterator<Map.Entry<Method, Interceptor>> interceptors;

    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private Object retValue;

    public InterceptorChain(MethodParameter method, Annotation annotation, String sql, SimpleGeneric returnType, List<MethodParameter> methodParameters, Iterator<Map.Entry<Method, Interceptor>> interceptors) {
        this.mapperMethod = method;
        this.annotation = annotation;
        this.sql = new WrapperValue<>(sql);
        this.returnType = returnType;
        this.methodParameters = new ArrayList<>(methodParameters);
        this.interceptors = interceptors;
    }

    public MethodParameter getMapperMethod() {
        return mapperMethod;
    }

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public void setPreparedStatement(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    public void setRetValue(Object retValue) {
        this.retValue = retValue;
    }

    public Object proceed() {
        if (!this.interceptors.hasNext()) {
            return this.processChainResult();
        }
        Map.Entry<Method, Interceptor> interceptor = this.interceptors.next();
        if (interceptor.getValue() instanceof QueryInterceptor && this.annotation.annotationType().equals(Execute.class)) {
            return this.proceed();
        }
        return this.retValue = ReflectUtil.invokeMethod(interceptor.getValue(), interceptor.getKey(), this.bindInterceptorParameters(interceptor.getKey()));
    }

    @Override
    public void close() {
        try {
            CommonUtil.close(this.getPreparedStatement());
            CommonUtil.close(this.getResultSet());
        } finally {
            try {
                commitTransactionIfNecessary(TransactionHolder.currentTransaction());
            } catch (SQLException e) {
                log.error("try commit transaction error !", e);
            }
        }
    }

    protected Object[] bindInterceptorParameters(Method method) {
        Parameter[] parameters = method.getParameters();
        Object[] paramValues = new Object[method.getParameterCount()];
        for (byte i = 0, size = (byte) parameters.length; i < size; i++) {
            paramValues[i] = this.doBindParameter(parameters[i]);
            if (paramValues[i] == null) {
                throw new IllegalArgumentException("interceptor parameter bind failed of parameter: " + parameters[i]);
            }
        }
        return paramValues;
    }

    protected Object doBindParameter(Parameter parameter) {
        Class<?> parameterType = parameter.getType();
        if (MethodParameter.class.equals(parameterType)) {
            return this.mapperMethod;
        }
        if (Annotation.class.isAssignableFrom(parameterType)) {
            return this.annotation;
        }
        if (WrapperValue.class.isAssignableFrom(parameterType)) {
            return this.sql;
        }
        if (SimpleGeneric.class.isAssignableFrom(parameterType)) {
            return this.returnType;
        }
        if (List.class.isAssignableFrom(parameterType)) {
            return this.methodParameters;
        }
        if (PreparedStatement.class.isAssignableFrom(parameterType)) {
            return this.preparePreparedStatement();
        }
        if (ResultSet.class.isAssignableFrom(parameterType)) {
            return this.prepareResultSet();
        }
        if (Object.class.equals(parameterType)) {
            return this.prepareReturnValue();
        }
        if (InterceptorChain.class.isAssignableFrom(parameterType)) {
            return this;
        }
        return null;
    }

    protected PreparedStatement preparePreparedStatement() {
        if (this.preparedStatement == null) {
            try {
                this.preparedStatement = JdbcUtil.getPreparedStatement(TransactionHolder.currentTransaction().getConnection(), this.sql.get(), this.methodParameters.toArray(new MethodParameter[0]));
            } catch (SQLException e) {
                throw new ExecuteInterceptorException(e);
            }
        }
        return this.preparedStatement;
    }

    protected ResultSet prepareResultSet() {
        if (this.resultSet == null) {
            try {
                this.resultSet = this.preparePreparedStatement().executeQuery();
            } catch (SQLException e) {
                throw new ExecuteInterceptorException(e);
            }
        }
        return this.resultSet;
    }

    protected Object prepareReturnValue() {
        if (this.retValue == null) {
            try {
                this.retValue = ResultSetUtil.processObject(this.prepareResultSet(), this.returnType);
            } catch (SQLException e) {
                throw new ExecuteInterceptorException(e);
            }
        }
        return this.retValue;
    }

    protected Object processChainResult() {
        if (this.retValue != null) {
            return this.retValue;
        }
        try {
            this.preparePreparedStatement().execute();
            this.retValue = this.preparePreparedStatement().getUpdateCount();
            if (log.isDebugEnabled()) {
                log.debug("<== affected rows: {}", this.retValue);
            }
            return this.retValue;
        } catch (SQLException e) {
            throw new ExecuteInterceptorException(e);
        }
    }
}
