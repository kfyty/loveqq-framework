package com.kfyty.database.jdbc.intercept;

import com.kfyty.core.generic.SimpleGeneric;
import com.kfyty.core.jdbc.TransactionHolder;
import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.utils.IOUtil;
import com.kfyty.core.utils.JdbcUtil;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.core.utils.ResultSetUtil;
import com.kfyty.core.wrapper.ValueWrapper;
import com.kfyty.database.jdbc.annotation.Execute;
import com.kfyty.database.jdbc.exception.ExecuteInterceptorException;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import static com.kfyty.core.utils.CommonUtil.size;
import static com.kfyty.core.utils.JdbcUtil.commitTransactionIfNecessary;

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
    private final ValueWrapper<String> sql;
    private final SimpleGeneric returnType;
    private final MethodParameter[] methodParameters;
    private final Iterator<Map.Entry<Method, Interceptor>> interceptors;

    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private Object retValue;
    private boolean hasRet;

    public InterceptorChain(MethodParameter method, Annotation annotation, String sql, SimpleGeneric returnType, MethodParameter[] methodParameters, Iterator<Map.Entry<Method, Interceptor>> interceptors) {
        this.mapperMethod = method;
        this.annotation = annotation;
        this.sql = new ValueWrapper<>(sql);
        this.returnType = returnType;
        this.methodParameters = methodParameters;
        this.interceptors = interceptors;
    }

    public MethodParameter getMapperMethod() {
        return this.mapperMethod;
    }

    public PreparedStatement getPreparedStatement() {
        return this.preparedStatement;
    }

    public ResultSet getResultSet() {
        return this.resultSet;
    }

    public void setPreparedStatement(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    public void setRetValue(Object retValue) {
        this.retValue = retValue;
        this.hasRet = true;
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
            IOUtil.close(this.getPreparedStatement());
            IOUtil.close(this.getResultSet());
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
        if (ValueWrapper.class.isAssignableFrom(parameterType)) {
            return this.sql;
        }
        if (SimpleGeneric.class.isAssignableFrom(parameterType)) {
            return this.returnType;
        }
        if (parameterType.isArray() && MethodParameter.class.isAssignableFrom(parameterType.getComponentType())) {
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
        throw new IllegalArgumentException("interceptor parameter bind failed of parameter: " + parameter);
    }

    protected PreparedStatement preparePreparedStatement() {
        if (this.preparedStatement == null) {
            try {
                this.preparedStatement = JdbcUtil.getPreparedStatement(TransactionHolder.currentTransaction().getConnection(), this.sql.get(), this.methodParameters);
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
        if (!this.hasRet) {
            try {
                this.setRetValue(ResultSetUtil.processObject(this.prepareResultSet(), this.returnType));
                if (log.isDebugEnabled()) {
                    log.debug("<==         total: {} [{}]", size(this.retValue), this.retValue == null ? null : this.retValue.getClass());
                }
            } catch (SQLException e) {
                throw new ExecuteInterceptorException(e);
            }
        }
        return this.retValue;
    }

    protected Object processChainResult() {
        if (this.hasRet) {
            return this.retValue;
        }
        try {
            this.preparePreparedStatement().execute();
            this.setRetValue(this.preparePreparedStatement().getUpdateCount());
            if (log.isDebugEnabled()) {
                log.debug("<== affected rows: {}", this.retValue);
            }
            return this.retValue;
        } catch (SQLException e) {
            throw new ExecuteInterceptorException(e);
        }
    }
}
