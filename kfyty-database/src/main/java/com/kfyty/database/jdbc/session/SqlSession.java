package com.kfyty.database.jdbc.session;

import com.kfyty.database.jdbc.BaseMapper;
import com.kfyty.database.jdbc.annotation.Execute;
import com.kfyty.database.jdbc.annotation.ForEach;
import com.kfyty.database.jdbc.annotation.Query;
import com.kfyty.database.jdbc.annotation.SubQuery;
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
import com.kfyty.support.transaction.Transaction;
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
import java.util.function.Supplier;

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

    /**
     * 事务
     */
    @ToString.Exclude
    private Transaction transaction;

    public SqlSession(Class<?> mapperClass, Configuration configuration) {
        this(mapperClass, configuration, null);
    }

    public SqlSession(Class<?> mapperClass, Configuration configuration, Transaction transaction) {
        this.mapperClass = mapperClass;
        this.configuration = configuration;
        this.transaction = transaction;
        this.providerAdapter = new ProviderAdapter(configuration);
    }

    /**
     * 获取事务，如果没有则开启一个新的
     *
     * @return Transaction
     */
    public Transaction getTransaction() {
        if (transaction == null) {
            this.transaction = new JdbcTransaction(this.configuration.getDataSource());
        }
        return this.transaction;
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
        returnType.setMapKey(ReflectUtil.invokeSimpleMethod(annotation, "key"));
    }

    /**
     * 从注解上获取 SQL
     *
     * @param sourceMethod mapper 方法
     * @param annotation   注解
     * @return SQL
     */
    private String resolveSQLByAnnotation(Method sourceMethod, Annotation annotation, Map<String, MethodParameter> params) {
        Class<?> provider = ReflectUtil.invokeSimpleMethod(annotation, "provider");
        if (!provider.equals(void.class)) {
            return this.providerAdapter.doProvide(provider, this.mapperClass, sourceMethod, annotation, params);
        }
        String sql = ReflectUtil.invokeSimpleMethod(annotation, "value");
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
    private String processForEach(Method sourceMethod, Annotation annotation, Map<String, MethodParameter> params) {
        String sql = resolveSQLByAnnotation(sourceMethod, annotation, params);
        ForEach[] forEachList = ReflectUtil.invokeSimpleMethod(annotation, "forEach");
        return sql + ForEachUtil.processForEach(params, forEachList);
    }

    /**
     * 处理子查询
     *
     * @param annotation 包含子查询的注解
     * @param obj        父查询的结果集
     */
    private Object processSubQuery(Method sourceMethod, Annotation annotation, Object obj) {
        if (annotation instanceof Query && obj != null) {
            SubQuery[] subQueries = ReflectUtil.invokeSimpleMethod(annotation, "subQuery");
            CommonUtil.consumer(obj, e -> this.processSubQuery(sourceMethod, subQueries, e), Map.Entry::getValue);
        }
        return obj;
    }

    /**
     * 处理子查询
     *
     * @param subQueries 子查询注解
     * @param obj        父查询映射的结果对象，若是集合，则为其中的每一个对象
     */
    private void processSubQuery(Method sourceMethod, SubQuery[] subQueries, Object obj) {
        if (notEmpty(subQueries)) {
            for (SubQuery subQuery : subQueries) {
                Field returnField = ReflectUtil.getField(obj.getClass(), subQuery.returnField());
                Map<String, MethodParameter> params = SQLParametersResolveUtil.resolveMappingParameters(subQuery.paramField(), subQuery.mapperField(), obj);
                SimpleGeneric returnType = SimpleGeneric.from(returnField);
                ReflectUtil.setFieldValue(obj, returnField, this.requestExecuteSQL(sourceMethod, subQuery, returnType, params));
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
    private Object requestExecuteSQL(Method sourceMethod, Annotation annotation, SimpleGeneric returnType, Map<String, MethodParameter> params) {
        checkMapKey(annotation, returnType);
        final String sql = this.processForEach(sourceMethod, annotation, params);
        final Pair<String, MethodParameter[]> sqlParams = SQLParametersResolveUtil.resolveSQL(sql, params);
        final Transaction transaction = this.getTransaction();
        final Supplier<Object> retValueSupplier = () -> {
            String methodName = BeanUtil.convert2BeanName(annotation.annotationType());
            Method method = ReflectUtil.getMethod(JdbcUtil.class, methodName, Transaction.class, SimpleGeneric.class, String.class, MethodParameter[].class);
            return ReflectUtil.invokeMethod(null, method, transaction, returnType, sqlParams.getKey(), sqlParams.getValue());
        };
        try {
            TransactionHolder.setCurrentTransaction(transaction);
            if (notEmpty(this.configuration.getInterceptorMethodChain())) {
                return this.processSubQuery(sourceMethod, annotation, this.invokeInterceptorChain(sourceMethod, sqlParams, returnType, retValueSupplier));
            }
            return this.processSubQuery(sourceMethod, annotation, retValueSupplier.get());
        } finally {
            TransactionHolder.removeCurrentTransaction();
        }
    }

    /**
     * 执行 SQL 拦截器链
     *
     * @param sourceMethod 接口方法
     * @param sqlParams    SQL 相关参数
     * @param returnType   返回值泛型
     * @return 执行结果
     */
    private Object invokeInterceptorChain(Method sourceMethod, Pair<String, MethodParameter[]> sqlParams, SimpleGeneric returnType, Supplier<Object> retValue) {
        Iterator<Map.Entry<Method, Interceptor>> iterator = this.configuration.getInterceptorMethodChain().entrySet().iterator();
        InterceptorChain chain = new InterceptorChain(sourceMethod, sqlParams.getKey(), returnType, Arrays.asList(sqlParams.getValue()), iterator, retValue);
        try {
            return chain.proceed();
        } finally {
            try {
                CommonUtil.close(chain.getPreparedStatement());
                CommonUtil.close(chain.getResultSet());
            } finally {
                try {
                    JdbcUtil.commitTransactionIfNecessary(TransactionHolder.currentTransaction());
                } catch (SQLException e) {
                    log.error("try commit transaction error !", e);
                }
            }
        }
    }
}