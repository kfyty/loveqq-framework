package com.kfyty.database.jdbc.session;

import com.kfyty.database.jdbc.BaseMapper;
import com.kfyty.database.jdbc.intercept.Interceptor;
import com.kfyty.database.jdbc.annotation.Execute;
import com.kfyty.database.jdbc.annotation.ForEach;
import com.kfyty.database.jdbc.annotation.Param;
import com.kfyty.database.jdbc.annotation.Query;
import com.kfyty.database.jdbc.annotation.SubQuery;
import com.kfyty.database.jdbc.exception.ExecuteInterceptorException;
import com.kfyty.database.jdbc.intercept.QueryInterceptor;
import com.kfyty.database.jdbc.sql.ProviderAdapter;
import com.kfyty.support.generic.Generic;
import com.kfyty.support.generic.SimpleGeneric;
import com.kfyty.support.jdbc.JdbcTransaction;
import com.kfyty.support.method.MethodParameter;
import com.kfyty.support.transaction.Transaction;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.JdbcUtil;
import com.kfyty.support.utils.ReflectUtil;
import com.kfyty.support.utils.SerializableUtil;
import com.kfyty.support.wrapper.WeakKey;
import javafx.util.Pair;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * toString 方法
     */
    private static final Method TO_STRING_METHOD = ReflectUtil.getMethod(Object.class, "toString");

    /**
     * #{}、${} 正则匹配
     */
    private static final Pattern PARAMETERS_PATTERN = Pattern.compile("(\\$\\{.*?})|(#\\{.*?})");

    /**
     * 序列化的注解缓存
     */
    private static final Map<WeakKey<Annotation>, byte[]> SERIALIZE_CACHE = Collections.synchronizedMap(new WeakHashMap<>(4));

    /**
     * mapper class
     */
    private final Class<?> mapperClass;

    /**
     * sql 提供适配器
     */
    private final ProviderAdapter providerAdapter;

    /**
     * 数据源
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
        this.providerAdapter = new ProviderAdapter();
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

    private Object invokeInterceptor(Method sourceMethod, String sql, Transaction transaction, SimpleGeneric returnType, MethodParameter... params) {
        if (CommonUtil.empty(this.configuration.getInterceptors())) {
            return null;
        }
        try {
            for (Interceptor interceptor : this.configuration.getInterceptors()) {
                if (AnnotationUtil.hasAnnotation(sourceMethod, Execute.class) && interceptor instanceof QueryInterceptor) {
                    continue;
                }
                Object retVal = interceptor.intercept(sql, transaction, returnType, params);
                if (retVal != null) {
                    return retVal;
                }
            }
            return null;
        } catch (SQLException e) {
            throw new ExecuteInterceptorException(e);
        }
    }

    /**
     * 根据注解类名调用相应的方法
     *
     * @param returnType 返回值类型
     * @param annotation 注解
     * @param params     参数
     * @return 返回值
     */
    private Object requestQuery(Method sourceMethod, Annotation annotation, SimpleGeneric returnType, Map<String, MethodParameter> params) {
        checkMapKey(annotation, returnType);
        final String sql = this.processForEach(sourceMethod, annotation, params);
        final Pair<String, MethodParameter[]> sqlParams = this.parseSQL(sql, params);
        return Optional
                .ofNullable(this.invokeInterceptor(sourceMethod, sqlParams.getKey(), this.getTransaction(), returnType, sqlParams.getValue()))
                .map(e -> this.processSubQuery(sourceMethod, annotation, e))
                .orElseGet(() -> {
                    String methodName = BeanUtil.convert2BeanName(annotation.annotationType());
                    Method method = ReflectUtil.getMethod(JdbcUtil.class, methodName, Transaction.class, SimpleGeneric.class, String.class, MethodParameter[].class);
                    Object obj = ReflectUtil.invokeMethod(null, method, this.getTransaction(), returnType, sqlParams.getKey(), sqlParams.getValue());
                    return this.processSubQuery(sourceMethod, annotation, obj);
                });
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
    private Pair<String, Annotation> getSQL(Method sourceMethod, Annotation annotation, Map<String, MethodParameter> params) {
        Class<?> provider = ReflectUtil.invokeSimpleMethod(annotation, "provider");
        if (!provider.equals(void.class)) {
            annotation = SerializableUtil.clone(annotation, SERIALIZE_CACHE);
            return new Pair<>(this.providerAdapter.doProvide(this.mapperClass, sourceMethod, annotation, params), annotation);
        }
        String sql = ReflectUtil.invokeSimpleMethod(annotation, "value");
        if (CommonUtil.empty(sql)) {
            throw new NullPointerException("SQL statement is empty !");
        }
        return new Pair<>(sql, annotation);
    }

    /**
     * 解析 ForEach 注解，并将对应的参数添加到 Map
     *
     * @param annotation 注解
     * @param params     参数
     * @return 拼接完毕的 sql
     */
    private String processForEach(Method sourceMethod, Annotation annotation, Map<String, MethodParameter> params) {
        Pair<String, Annotation> sqlPair = getSQL(sourceMethod, annotation, params);
        ForEach[] forEachList = ReflectUtil.invokeSimpleMethod(sqlPair.getValue(), "forEach");
        if (CommonUtil.empty(forEachList)) {
            return sqlPair.getKey();
        }
        StringBuilder builder = new StringBuilder();
        for (ForEach each : forEachList) {
            MethodParameter parameter = params.get(each.collection());
            List<Object> list = CommonUtil.toList(parameter.getValue());
            builder.append(each.open());
            for (int i = 0; i < list.size(); i++) {
                String flag = "param_" + i + "_";
                Object value = list.get(i);
                builder.append(each.sqlPart().replace("#{", "#{" + flag).replace("${", "${" + flag));
                params.put(flag + each.item(), new MethodParameter(value == null ? Object.class : value.getClass(), value));
                if (i == list.size() - 1) {
                    break;
                }
                builder.append(each.separator());
            }
            builder.append(each.close());
        }
        return sqlPair.getKey() + builder;
    }

    /**
     * 根据子查询注解的两个属性提取参数
     *
     * @param paramField  父查询 sql 中查询出的字段名
     * @param mapperField 子查询 sql 中的 #{}/${} 参数
     * @param obj         父查询映射的结果对象
     * @return MethodParameter
     * @see this#processMethodParameters(Method, Object[])
     */
    private Map<String, MethodParameter> processMappingParameters(String[] paramField, String[] mapperField, Object obj) {
        if (CommonUtil.empty(paramField) || CommonUtil.empty(mapperField)) {
            return null;
        }
        if (paramField.length != mapperField.length) {
            log.error("parameters number and mapper field number can't match !");
            return null;
        }
        Map<String, MethodParameter> param = new HashMap<>();
        for (int i = 0; i < paramField.length; i++) {
            Field field = ReflectUtil.getField(obj.getClass(), paramField[i]);
            param.put(mapperField[i], new MethodParameter(field.getType(), ReflectUtil.getFieldValue(obj, field)));
        }
        return param;
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
        if (CommonUtil.notEmpty(subQueries)) {
            for (SubQuery subQuery : subQueries) {
                Field returnField = ReflectUtil.getField(obj.getClass(), subQuery.returnField());
                Map<String, MethodParameter> params = this.processMappingParameters(subQuery.paramField(), subQuery.mapperField(), obj);
                SimpleGeneric returnType = SimpleGeneric.from(returnField);
                ReflectUtil.setFieldValue(obj, returnField, this.requestQuery(sourceMethod, subQuery, returnType, params));
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
     * 处理重复注解
     *
     * @param method 存在注解的方法
     * @return 注解数组
     */
    public Annotation[] processRepeatableAnnotation(Method method) {
        List<Annotation> annotations = new ArrayList<>();
        for (Annotation annotation : AnnotationUtil.findAnnotations(method)) {
            Object o = ReflectUtil.invokeSimpleMethod(annotation, "value");
            if (o.getClass().isArray()) {
                annotations.addAll(Arrays.asList((Annotation[]) o));
                continue;
            }
            annotations.add(annotation);
        }
        return annotations.toArray(new Annotation[0]);
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
        if (method.equals(TO_STRING_METHOD)) {
            return this.toString();
        }
        SimpleGeneric returnType = this.processReturnType(method);
        Annotation[] annotations = this.processRepeatableAnnotation(method);
        Map<String, MethodParameter> methodParameter = this.processMethodParameters(method, args);
        if (annotations.length == 1) {
            return this.requestQuery(method, annotations[0], returnType, methodParameter);
        }
        List<Object> os = new ArrayList<>();
        for (Annotation annotation : annotations) {
            os.add(this.requestQuery(method, annotation, returnType, methodParameter));
        }
        return os;
    }

    /**
     * 解析 sql 中的 #{} 为 ? ，并将参数对应保存到集合中；
     * 解析 sql 中的 ${} ，并使用相应的值直接替换掉
     *
     * @param sql        sql 语句
     * @param parameters MethodParameter
     * @return Map 集合，包含 sql/args
     * @see this#processMethodParameters(Method, Object[])
     */
    public Pair<String, MethodParameter[]> parseSQL(String sql, Map<String, MethodParameter> parameters) {
        List<MethodParameter> args = new ArrayList<>();
        Map<String, List<String>> params = this.getFormalParameters(sql);
        for (Map.Entry<String, List<String>> next : params.entrySet()) {
            for (String param : next.getValue()) {
                Object value = null;
                Class<?> paramType = null;
                if (!param.contains(".")) {
                    MethodParameter methodParam = parameters.get(param);
                    value = methodParam.getValue();
                    paramType = methodParam.getParamType();
                } else {
                    String nested = param.substring(param.indexOf(".") + 1);
                    Object root = parameters.get(param.split("\\.")[0]).getValue();
                    value = ReflectUtil.parseValue(nested, root);
                    paramType = ReflectUtil.parseFieldType(nested, root.getClass());
                }
                if (value == null && log.isDebugEnabled()) {
                    log.debug("discovery null parameter: [{}] !", param);
                }
                if ("#".equals(next.getKey())) {
                    args.add(new MethodParameter(paramType, value));
                    continue;
                }
                sql = sql.replace("${" + param + "}", String.valueOf(value));
            }
        }
        return new Pair<>(this.replaceParam(sql, params.get("#")), args.toArray(new MethodParameter[0]));
    }

    /**
     * 替换 sql 中的 #{} 为 ?
     *
     * @param sql    sql 语句
     * @param params 解析出的 #{} 中的字符串集合
     * @return 替换后的 sql
     */
    public String replaceParam(String sql, List<String> params) {
        for (String param : params) {
            sql = sql.replace("#{" + param + "}", "?");
        }
        return sql;
    }

    /**
     * 将方法参数中有 @Param 注解的参数封装为 Map
     * 若 @Param 注解不存在，则直接使用 Parameter#getName()
     *
     * @param method 方法
     * @param args   参数数组
     * @return 参数 Map
     */
    public Map<String, MethodParameter> processMethodParameters(Method method, Object[] args) {
        Parameter[] parameters = method.getParameters();
        Map<String, MethodParameter> params = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Param annotation = AnnotationUtil.findAnnotation(parameter, Param.class);
            String paramName = annotation != null && CommonUtil.notEmpty(annotation.value()) ? annotation.value() : parameter.getName();
            params.put(paramName, new MethodParameter(method, parameter, args[i]));
        }
        return params;
    }

    /**
     * 解析 sql 中的 #{}/${} 中的字符串，并分别保存到 Map
     *
     * @param sql sql 语句
     * @return 解析得到的包含 #{}/${} 的 Map
     */
    public Map<String, List<String>> getFormalParameters(String sql) {
        Map<String, List<String>> params = new HashMap<>();
        params.put("#", new ArrayList<>());
        params.put("$", new ArrayList<>());
        Matcher matcher = PARAMETERS_PATTERN.matcher(sql);
        while (matcher.find()) {
            String group = matcher.group();
            if (group.charAt(0) == '#') {
                params.get("#").add(group.replaceAll("[#{}]", ""));
            } else {
                params.get("$").add(group.replaceAll("[${}]", ""));
            }
        }
        return params;
    }
}
