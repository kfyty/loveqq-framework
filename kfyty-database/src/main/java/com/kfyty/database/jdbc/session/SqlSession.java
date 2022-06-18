package com.kfyty.database.jdbc.session;

import com.kfyty.database.jdbc.BaseMapper;
import com.kfyty.database.jdbc.annotation.Execute;
import com.kfyty.database.jdbc.annotation.ForEach;
import com.kfyty.database.jdbc.annotation.Query;
import com.kfyty.database.jdbc.annotation.SubQuery;
import com.kfyty.database.jdbc.exception.ExecuteInterceptorException;
import com.kfyty.database.jdbc.intercept.Interceptor;
import com.kfyty.database.jdbc.intercept.InterceptorChain;
import com.kfyty.database.jdbc.sql.ProviderAdapter;
import com.kfyty.database.util.AnnotationInstantiateUtil;
import com.kfyty.database.util.ForEachUtil;
import com.kfyty.database.util.SQLParametersResolveUtil;
import com.kfyty.support.generic.Generic;
import com.kfyty.support.generic.SimpleGeneric;
import com.kfyty.support.jdbc.JdbcTransaction;
import com.kfyty.support.jdbc.TransactionHolder;
import com.kfyty.support.method.MethodParameter;
import com.kfyty.support.jdbc.transaction.Transaction;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.JdbcUtil;
import com.kfyty.support.utils.ReflectUtil;
import javafx.util.Pair;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.kfyty.support.utils.AnnotationUtil.findAnnotations;
import static com.kfyty.support.utils.CommonUtil.notEmpty;

/**
 * 功能描述: SqlSession，仅支持通过接口代理操作数据库
 *
 * @author kfyty725@hotmail.com
 * @date 2019/6/27 16:04
 * @see BaseMapper
 * @since JDK 1.8
 */
@Slf4j
@ToString
public class SqlSession implements InvocationHandler {
    /**
     * mapper class
     */
    private final Class<?> mapperClass;

    /**
     * sql 提供适配器
     */
    private final ProviderAdapter providerAdapter;

    /**
     * 配置
     */
    @ToString.Exclude
    private final Configuration configuration;

    public SqlSession(Class<?> mapperClass, Configuration configuration) {
        this.mapperClass = mapperClass;
        this.configuration = configuration;
        this.providerAdapter = new ProviderAdapter(configuration);
    }

    /**
     * 获取事务，如果没有则开启一个新的
     *
     * @return Transaction
     */
    public Transaction getTransaction() {
        Transaction transaction = TransactionHolder.currentTransaction(false);
        if (transaction == null) {
            TransactionHolder.setCurrentTransaction(transaction = new JdbcTransaction(this.configuration.getDataSource()));
        }
        return transaction;
    }

    /**
     * 代理方法
     *
     * @param proxy  代理对象
     * @param method 被代理的方法
     * @param args   被代理的方法的方法参数
     * @return 被代理的方法的返回值
     * @throws Throwable Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }
        SimpleGeneric returnType = this.processReturnType(method);
        Annotation[] annotations = this.processAnnotation(method);
        Map<String, MethodParameter> methodParameter = SQLParametersResolveUtil.processMethodParameters(method, args);
        if (annotations.length == 1) {
            return this.requestExecuteSQL(method, annotations[0], returnType, methodParameter);
        }
        List<Object> os = new ArrayList<>();
        for (Annotation annotation : annotations) {
            os.add(this.requestExecuteSQL(method, annotation, returnType, methodParameter));
        }
        return os;
    }

    /**
     * 检查注解中的 key 属性
     *
     * @param annotation 注解
     * @param returnType 返回值类型
     */
    private void checkMapKey(Annotation annotation, SimpleGeneric returnType) {
        if (!(annotation instanceof Query || annotation instanceof SubQuery)) {
            return;
        }
        returnType.setMapKey(ReflectUtil.invokeMethod(annotation, "key"));
    }

    /**
     * 从注解上获取 SQL
     *
     * @param mapperMethod mapper 方法
     * @param annotation   注解
     * @return SQL
     */
    private String resolveSQLByAnnotation(Method mapperMethod, Annotation annotation, Map<String, MethodParameter> params) {
        Class<?> provider = ReflectUtil.invokeMethod(annotation, "provider");
        if (!provider.equals(void.class)) {
            return this.providerAdapter.doProvide(provider, this.mapperClass, mapperMethod, annotation, params);
        }
        String sql = ReflectUtil.invokeMethod(annotation, "value");
        if (CommonUtil.empty(sql)) {
            throw new IllegalArgumentException("SQL statement is empty !");
        }
        return sql;
    }

    /**
     * 解析 ForEach 注解，并将对应的参数添加到 Map
     *
     * @param annotation 注解
     * @param params     参数
     * @return 拼接完毕的 sql
     */
    private String processForEach(Method mapperMethod, Annotation annotation, Map<String, MethodParameter> params) {
        String sql = resolveSQLByAnnotation(mapperMethod, annotation, params);
        ForEach[] forEachList = ReflectUtil.invokeMethod(annotation, "forEach");
        return sql + ForEachUtil.processForEach(params, forEachList);
    }

    /**
     * 处理子查询
     *
     * @param annotation 包含子查询的注解
     * @param obj        父查询的结果集
     */
    private Object processSubQuery(Method mapperMethod, Annotation annotation, Object obj) {
        if (annotation instanceof Query && obj != null) {
            SubQuery[] subQueries = ReflectUtil.invokeMethod(annotation, "subQuery");
            CommonUtil.consumer(obj, e -> this.processSubQuery(mapperMethod, subQueries, e), Map.Entry::getValue);
        }
        return obj;
    }

    /**
     * 处理子查询
     *
     * @param subQueries 子查询注解
     * @param obj        父查询映射的结果对象，若是集合，则为其中的每一个对象
     */
    private void processSubQuery(Method mapperMethod, SubQuery[] subQueries, Object obj) {
        if (notEmpty(subQueries)) {
            for (SubQuery subQuery : subQueries) {
                Field returnField = ReflectUtil.getField(obj.getClass(), subQuery.returnField());
                Map<String, MethodParameter> params = SQLParametersResolveUtil.resolveMappingParameters(subQuery.paramField(), subQuery.mapperField(), obj);
                SimpleGeneric returnType = SimpleGeneric.from(returnField);
                ReflectUtil.setFieldValue(obj, returnField, this.requestExecuteSQL(mapperMethod, subQuery, returnType, params));
            }
        }
    }

    /**
     * 解析方法返回值类型
     *
     * @param method 方法
     * @return 返回值类型包装
     */
    private SimpleGeneric processReturnType(Method method) {
        if (!method.getDeclaringClass().equals(BaseMapper.class) || method.getReturnType().equals(int.class)) {
            return SimpleGeneric.from(method);
        }
        Class<?> entityClass = ReflectUtil.getSuperGeneric(this.mapperClass, 1);
        if (method.getReturnType().equals(Object.class)) {
            return new SimpleGeneric(entityClass);
        }
        if (Collection.class.isAssignableFrom(method.getReturnType())) {
            SimpleGeneric simpleGeneric = new SimpleGeneric(method.getReturnType(), method.getGenericReturnType());
            simpleGeneric.getGenericInfo().put(new Generic(entityClass), null);
            return simpleGeneric;
        }
        throw new IllegalArgumentException("parse return type failed !");
    }

    /**
     * 处理方法上的注解
     *
     * @param method 代理方法
     * @return 注解数组
     */
    private Annotation[] processAnnotation(Method method) {
        Predicate<Annotation> annotationFilter = e -> e.annotationType().equals(Query.class) || e.annotationType().equals(Execute.class);
        Annotation[] annotations = Arrays.stream(AnnotationUtil.flatRepeatableAnnotation(findAnnotations(method))).filter(annotationFilter).toArray(Annotation[]::new);
        if (CommonUtil.notEmpty(annotations)) {
            return annotations;
        }
        String id = this.configuration.getDynamicProvider().resolveTemplateStatementId(this.mapperClass, method);
        String labelType = this.configuration.getTemplateStatements().get(id).getLabelType();
        return new Annotation[]{AnnotationInstantiateUtil.createDynamicByLabelType(labelType)};
    }

    /**
     * 根据注解类名调用相应的方法
     *
     * @param returnType 返回值类型
     * @param annotation 注解
     * @param params     参数
     * @return 返回值
     */
    private Object requestExecuteSQL(Method mapperMethod, Annotation annotation, SimpleGeneric returnType, Map<String, MethodParameter> params) {
        checkMapKey(annotation, returnType);
        final String sql = this.processForEach(mapperMethod, annotation, params);
        final Pair<String, MethodParameter[]> sqlParams = SQLParametersResolveUtil.resolveSQL(sql, params);
        try {
            Transaction transaction = this.getTransaction();
            if (notEmpty(this.configuration.getInterceptorMethodChain())) {
                MethodParameter method = new MethodParameter(mapperMethod, params.values().stream().map(MethodParameter::getValue).toArray());
                return this.processSubQuery(mapperMethod, annotation, this.invokeInterceptorChain(method, annotation, sqlParams, returnType));
            }
            String methodName = BeanUtil.getBeanName(annotation.annotationType());
            Method method = ReflectUtil.getMethod(JdbcUtil.class, methodName, Transaction.class, SimpleGeneric.class, String.class, MethodParameter[].class);
            Object retValue = ReflectUtil.invokeMethod(null, method, transaction, returnType, sqlParams.getKey(), sqlParams.getValue());
            return this.processSubQuery(mapperMethod, annotation, retValue);
        } finally {
            TransactionHolder.removeCurrentTransaction();
        }
    }

    /**
     * 执行 SQL 拦截器链
     *
     * @param mapperMethod 接口方法
     * @param sqlParams    SQL 相关参数
     * @param returnType   返回值泛型
     * @return 执行结果
     */
    private Object invokeInterceptorChain(MethodParameter mapperMethod, Annotation annotation, Pair<String, MethodParameter[]> sqlParams, SimpleGeneric returnType) {
        Iterator<Map.Entry<Method, Interceptor>> iterator = this.configuration.getInterceptorMethodChain().entrySet().iterator();
        try (InterceptorChain chain = new InterceptorChain(mapperMethod, annotation, sqlParams.getKey(), returnType, Arrays.asList(sqlParams.getValue()), iterator)) {
            return chain.proceed();
        } catch (Exception e) {
            try {
                TransactionHolder.currentTransaction().rollback();
                throw e;
            } catch (SQLException ex) {
                throw new ExecuteInterceptorException(ex);
            }
        }
    }
}
