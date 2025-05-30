package com.kfyty.loveqq.framework.data.korm.session;

import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import com.kfyty.loveqq.framework.core.jdbc.TransactionHolder;
import com.kfyty.loveqq.framework.core.jdbc.transaction.Transaction;
import com.kfyty.loveqq.framework.core.lang.Value;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.JdbcUtil;
import com.kfyty.loveqq.framework.data.korm.BaseMapper;
import com.kfyty.loveqq.framework.data.korm.annotation.Execute;
import com.kfyty.loveqq.framework.data.korm.annotation.Query;
import com.kfyty.loveqq.framework.data.korm.annotation.SubQuery;
import com.kfyty.loveqq.framework.data.korm.exception.ExecuteInterceptorException;
import com.kfyty.loveqq.framework.data.korm.intercept.Interceptor;
import com.kfyty.loveqq.framework.data.korm.intercept.InterceptorChain;
import com.kfyty.loveqq.framework.data.korm.sql.Provider;
import com.kfyty.loveqq.framework.data.korm.sql.ProviderAdapter;
import com.kfyty.loveqq.framework.data.korm.util.AnnotationInstantiateUtil;
import com.kfyty.loveqq.framework.data.korm.util.SQLParametersResolveUtil;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.findAnnotations;
import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.flatRepeatableAnnotation;
import static com.kfyty.loveqq.framework.core.utils.CommonUtil.notEmpty;
import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.invokeMethod;

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
            TransactionHolder.setCurrentTransaction(transaction = this.configuration.getTransactionFactory().get());
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
            return this.requestExecuteSQL(method, new Value<>(annotations[0]), returnType, methodParameter);
        }
        List<Object> os = new ArrayList<>();
        for (Annotation annotation : annotations) {
            os.add(this.requestExecuteSQL(method, new Value<>(annotation), returnType, methodParameter));
        }
        return os;
    }

    /**
     * 根据注解类名调用相应的方法
     *
     * @param returnType 返回值类型
     * @param annotation 注解
     * @param params     参数
     * @return 返回值
     */
    public Object requestExecuteSQL(Method mapperMethod, Value<Annotation> annotation, SimpleGeneric returnType, Map<String, MethodParameter> params) throws SQLException {
        SQLParametersResolveUtil.checkMapKey(annotation.get(), returnType);
        final String sql = this.resolveSQL(mapperMethod, annotation, params);
        final Pair<String, MethodParameter[]> sqlParams = SQLParametersResolveUtil.resolveSQL(sql, params);
        final Transaction before = TransactionHolder.currentTransaction(false);
        try {
            Transaction transaction = this.getTransaction();
            if (notEmpty(this.configuration.getInterceptorMethodChain())) {
                MethodParameter method = new MethodParameter(mapperMethod, params.values().toArray(MethodParameter[]::new));
                return this.invokeInterceptorChain(method, annotation.get(), sqlParams, returnType);
            }
            if (annotation.get().annotationType() == Query.class || annotation.get().annotationType() == SubQuery.class) {
                return JdbcUtil.query(transaction, returnType, sqlParams.getKey(), sqlParams.getValue());
            }
            return JdbcUtil.execute(transaction, sqlParams.getKey(), sqlParams.getValue());
        } finally {
            TransactionHolder.resetCurrentTransaction(before);
        }
    }

    /**
     * 解析方法返回值类型
     *
     * @param method 方法
     * @return 返回值类型包装
     */
    private SimpleGeneric processReturnType(Method method) {
        return SimpleGeneric.from(this.mapperClass, method);
    }

    /**
     * 处理方法上的注解
     *
     * @param method 代理方法
     * @return 注解数组
     */
    private Annotation[] processAnnotation(Method method) {
        Predicate<Annotation> annotationFilter = e -> e.annotationType().equals(Query.class) || e.annotationType().equals(Execute.class);
        Annotation[] annotations = flatRepeatableAnnotation(findAnnotations(method), annotationFilter, Annotation[]::new);
        if (CommonUtil.notEmpty(annotations)) {
            return annotations;
        }

        // 不存在注解，走模板渲染处理，并创建模拟注解返回
        String id = this.configuration.getDynamicProvider().resolveTemplateStatementId(this.mapperClass, method);
        String labelType = this.configuration.getTemplateStatements().get(id).getLabelType();
        return new Annotation[]{AnnotationInstantiateUtil.createDynamicByLabelType(labelType)};
    }

    /**
     * 从注解获取 SQL
     *
     * @param mapperMethod mapper 方法
     * @param annotation   注解
     * @return SQL
     */
    private String resolveSQL(Method mapperMethod, Value<Annotation> annotation, Map<String, MethodParameter> params) {
        Class<?> provider = invokeMethod(annotation.get(), "provider");
        if (!provider.equals(Provider.class)) {
            return this.providerAdapter.doProvide(provider, this.mapperClass, mapperMethod, annotation, params);
        }
        String sql = invokeMethod(annotation.get(), "value");
        if (CommonUtil.empty(sql)) {
            throw new IllegalArgumentException("SQL statement is empty !");
        }
        return sql;
    }

    /**
     * 执行 SQL 拦截器链
     *
     * @param mapperMethod 接口方法
     * @param annotation   mapper 方法注解
     * @param sqlParams    SQL 相关参数
     * @param returnType   返回值泛型
     * @return 执行结果
     */
    private Object invokeInterceptorChain(MethodParameter mapperMethod, Annotation annotation, Pair<String, MethodParameter[]> sqlParams, SimpleGeneric returnType) {
        Transaction transaction = TransactionHolder.currentTransaction();
        Iterator<Map.Entry<Method, Interceptor>> iterator = this.configuration.getInterceptorMethodChain().entrySet().iterator();
        try (InterceptorChain chain = new InterceptorChain(this, mapperMethod, annotation, sqlParams.getKey(), returnType, new ArrayList<>(Arrays.asList(sqlParams.getValue())), iterator)) {
            Object retValue = chain.proceed();
            if (annotation instanceof SubQuery) {
                return retValue;
            }
            JdbcUtil.commitTransactionIfNecessary(transaction);
            return retValue;
        } catch (Exception e) {
            try {
                transaction.rollback();
                throw e;
            } catch (SQLException ex) {
                if (ex == e) {
                    throw new ExecuteInterceptorException(e);
                }
                throw new ExecuteInterceptorException(ex.getMessage() + ", root cause is " + e.getMessage(), e);
            }
        }
    }
}
