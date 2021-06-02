package com.kfyty.database.jdbc;

import com.kfyty.database.jdbc.annotation.ForEach;
import com.kfyty.database.jdbc.annotation.Param;
import com.kfyty.database.jdbc.annotation.Query;
import com.kfyty.database.jdbc.annotation.SubQuery;
import com.kfyty.database.jdbc.sql.Provider;
import com.kfyty.support.jdbc.JdbcTransaction;
import com.kfyty.support.jdbc.ReturnType;
import com.kfyty.support.transaction.Transaction;
import com.kfyty.util.CommonUtil;
import com.kfyty.util.JdbcUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能描述: SqlSession
 *
 * @author kfyty725@hotmail.com
 * @date 2019/6/27 16:04
 * @since JDK 1.8
 */
@Slf4j
public class SqlSession implements InvocationHandler {
    /**
     * mapper class
     */
    private final Class<?> mapperClass;

    /**
     * 数据源
     */
    @Setter @Getter
    private DataSource dataSource;

    /**
     * 事务
     */
    private Transaction transaction;

    public SqlSession(Class<?> mapperClass, DataSource dataSource) {
        this.mapperClass = mapperClass;
        this.dataSource = dataSource;
    }

    public SqlSession(Class<?> mapperClass, DataSource dataSource, Transaction transaction) {
        this.mapperClass = mapperClass;
        this.dataSource = dataSource;
        this.transaction = transaction;
    }

    /**
     * 根据注解类名调用相应的方法
     * @param returnType    返回值类型
     * @param annotation    注解
     * @param params        参数
     * @return              返回值
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    private Object requestQuery(Method sourceMethod, Annotation annotation, ReturnType returnType, Map<String, Object> params) throws Exception {
        checkMapKey(annotation, returnType);
        String annotationName = annotation.annotationType().getSimpleName();
        String methodName = Character.toLowerCase(annotationName.charAt(0)) + annotationName.substring(1);
        String sql = this.parseForEach(sourceMethod, annotation, params);
        Map<String, Object> map = this.parseSQL(sql, params);
        Method method = JdbcUtil.class.getDeclaredMethod(methodName, Transaction.class, ReturnType.class, String.class, Object[].class);
        Object obj = CommonUtil.invokeMethod(null, method, this.getTransaction(), returnType, map.get("sql"), map.get("args"));
        this.handleSubQuery(sourceMethod, annotation, obj);
        return obj;
    }

    /**
     * 获取事务，如果没有则开启一个新的
     * @return Transaction
     */
    public Transaction getTransaction() {
        if(transaction == null) {
            this.transaction = new JdbcTransaction(this.dataSource);
        }
        return this.transaction;
    }

    /**
     * 检查注解中的 key 属性
     * @param annotation    注解
     * @param returnType    返回值类型
     * @throws Exception
     */
    private void checkMapKey(Annotation annotation, ReturnType returnType) throws Exception {
        if(!(annotation instanceof Query || annotation instanceof SubQuery)) {
            return;
        }
        returnType.setKey((String) CommonUtil.invokeSimpleMethod(annotation, "key"));
    }

    /**
     * 从注解上获取 SQL
     * @param sourceMethod mapper 方法
     * @param annotation 注解
     * @return SQL
     */
    @SuppressWarnings("unchecked")
    private String getSQL(Method sourceMethod, Annotation annotation) throws Exception {
        Class<? extends Provider> provider = (Class<? extends Provider>) CommonUtil.invokeSimpleMethod(annotation, "provider");
        if(!provider.equals(Provider.class)) {
            return CommonUtil.newInstance(provider).doProvide(this.mapperClass, sourceMethod, annotation);
        }
        String sql = (String) CommonUtil.invokeSimpleMethod(annotation, "value");
        if(CommonUtil.empty(sql)) {
            throw new NullPointerException("sql statement is null !");
        }
        return sql;
    }

    /**
     * 解析 ForEach 注解，并将对应的参数添加到 Map
     * @param annotation    注解
     * @param params        参数
     * @return              拼接完毕的 sql
     * @throws Exception
     */
    private String parseForEach(Method sourceMethod, Annotation annotation, Map<String, Object> params) throws Exception {
        String sql = getSQL(sourceMethod, annotation);
        ForEach[] forEachList = (ForEach[]) CommonUtil.invokeSimpleMethod(annotation, "forEach");
        if(CommonUtil.empty(forEachList)) {
            return sql;
        }
        StringBuilder builder = new StringBuilder();
        for (ForEach each : forEachList) {
            List<Object> list = CommonUtil.convert2List(params.get(each.collection()));
            builder.append(each.open());
            for(int i = 0; i < list.size(); i++) {
                String flag = "_" + i;
                builder.append(each.sqlPart().replace("#{", "#{" + flag).replace("${", "${" + flag));
                params.put(flag + each.item(), list.get(i));
                if(i == list.size() - 1) {
                    break;
                }
                builder.append(each.separator());
            }
            builder.append(each.close());
        }
        return sql + builder;
    }

    /**
     * 根据子查询注解的两个属性提取参数
     * @param paramField    父查询 sql 中查询出的字段名
     * @param mapperField   子查询 sql 中的 #{}/${} 参数
     * @param obj           父查询映射的结果对象
     * @return              @see getParamFromMethod()
     * @throws Exception
     */
    private Map<String, Object> getParamFromAnnotation(String[] paramField, String[] mapperField, Object obj) throws Exception {
        if(CommonUtil.empty(paramField) || CommonUtil.empty(mapperField)) {
            return null;
        }
        if(paramField.length != mapperField.length) {
            log.error(": parameters number and mapper field number can't match !");
            return null;
        }
        Map<String, Object> param = new HashMap<>();
        for (int i = 0; i < paramField.length; i++) {
            param.put(mapperField[i], CommonUtil.getFieldValue(obj, paramField[i]));
        }
        return param;
    }

    /**
     * 处理子查询
     * @param annotation    包含子查询的注解
     * @param obj           父查询的结果集
     * @throws Exception
     */
    private void handleSubQuery(Method sourceMethod, Annotation annotation, Object obj) throws Exception {
        if(!(annotation instanceof Query && obj != null)) {
            return ;
        }
        SubQuery[] subQueries = (SubQuery[]) CommonUtil.invokeSimpleMethod(annotation, "subQuery");
        if(obj instanceof Collection) {
            for (Object o : (Collection<?>) obj) {
                this.handleSubQuery(sourceMethod, subQueries, o);
            }
            return ;
        }
        if(obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                this.handleSubQuery(sourceMethod, subQueries, entry.getValue());
            }
            return ;
        }
        this.handleSubQuery(sourceMethod, subQueries, obj);
    }

    /**
     * 处理子查询
     * @param subQueries    子查询注解
     * @param obj           父查询映射的结果对象，若是集合，则为其中的每一个对象
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    private void handleSubQuery(Method sourceMethod, SubQuery[] subQueries, Object obj) throws Exception {
        if(CommonUtil.empty(subQueries) || obj == null) {
            return ;
        }
        for (SubQuery subQuery : subQueries) {
            Field returnField = CommonUtil.getField(obj.getClass(), subQuery.returnField());
            Map<String, Object> params = this.getParamFromAnnotation(subQuery.paramField(), subQuery.mapperField(), obj);
            ReturnType returnType = ReturnType.getReturnType(returnField.getGenericType(), returnField.getType());
            CommonUtil.setFieldValue(obj, returnField, this.requestQuery(sourceMethod, subQuery, returnType, params));
        }
    }

    /**
     * 处理重复注解
     * @param method    存在注解的方法
     * @return          注解数组
     * @throws Exception
     */
    public Annotation[] getAnnotationFromMethod(Method method) throws Exception {
        List<Annotation> annotations = new ArrayList<>();
        for(Annotation annotation : method.getAnnotations()) {
            Object o = CommonUtil.invokeSimpleMethod(annotation, "value");
            if(o.getClass().isArray()) {
                annotations.addAll(Arrays.asList((Annotation[]) o));
                continue;
            }
            annotations.add(annotation);
        }
        return annotations.toArray(new Annotation[0]);
    }

    /**
     * 代理方法
     * @param proxy     代理对象
     * @param method    被代理的方法
     * @param args      被代理的方法的方法参数
     * @return          被代理的方法的返回值
     * @throws Throwable
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Annotation[] annotations = this.getAnnotationFromMethod(method);
        ReturnType returnType = ReturnType.getReturnType(method.getGenericReturnType(), method.getReturnType());
        if(method.getDeclaringClass().equals(BaseMapper.class) && method.getReturnType().equals(Object.class)) {
            returnType.setReturnType(CommonUtil.getSuperGeneric(this.mapperClass, 1));
        }
        if(annotations.length == 1) {
            return this.requestQuery(method, annotations[0], returnType, this.getParamFromMethod(method.getParameters(), args));
        }
        List<Object> os = new ArrayList<>();
        for(Annotation annotation : annotations) {
            os.add(this.requestQuery(method, annotation, returnType, this.getParamFromMethod(method.getParameters(), args)));
        }
        return os;
    }

    /**
     * 解析 sql 中的 #{} 为 ? ，并将参数对应保存到集合中；
     * 解析 sql 中的 ${} ，并使用相应的值直接替换掉
     * @param sql           sql 语句
     * @param parameters    @see getParamFromMethod() 返回值
     * @return              Map 集合，包含 sql/args
     * @throws Exception
     */
    public Map<String, Object> parseSQL(String sql, Map<String, Object> parameters) throws Exception {
        List<Object> args = new ArrayList<>();
        Map<String, List<String>> params = this.getParamFromSQL(sql);
        for(Map.Entry<String, List<String>> next : params.entrySet()) {
            for(String param : next.getValue()) {
                Object o = !param.contains(".") ? parameters.get(param) : CommonUtil.parseValue(param.substring(param.indexOf(".") + 1), parameters.get(param.split("\\.")[0]));
                if(o == null) {
                    log.warn(": null parameter found:[{}] --> parameter value:[{}] !", param, o);
                }
                if(next.getKey().equals("#")) {
                    args.add(o);
                    continue;
                }
                sql = sql.replace("${" + param + "}", o.toString());
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("sql", this.replaceParam(sql, params.get("#")));
        map.put("args", args.toArray());
        return map;
    }

    /**
     * 替换 sql 中的 #{} 为 ?
     * @param sql       sql 语句
     * @param params    解析出的 #{} 中的字符串集合
     * @return          替换后的 sql
     */
    public String replaceParam(String sql, List<String> params) {
        for(String param : params) {
            sql = sql.replace("#{" + param + "}", "?");
        }
        return sql;
    }

    /**
     * 将方法参数中有 @Param 注解的参数封装为 Map
     * @param parameters    方法参数数组
     * @param args          参数数组
     * @return              参数 Map
     */
    public Map<String, Object> getParamFromMethod(Parameter[] parameters, Object[] args) {
        Map<String, Object> params = new HashMap<>();
        for(int i = 0; i < parameters.length; i++) {
            if(!parameters[i].isAnnotationPresent(Param.class)) {
                continue;
            }
            params.put(parameters[i].getAnnotation(Param.class).value(), args[i]);
        }
        return params;
    }

    /**
     * 解析 sql 中的 #{}/${} 中的字符串，并分别保存到 Map
     * @param sql   sql 语句
     * @return      解析得到的包含 #{}/${} 的Map
     */
    public Map<String, List<String>> getParamFromSQL(String sql) {
        int begin = 0;
        int end = begin;
        Map<String, List<String>> params = new HashMap<>();
        params.put("#", new ArrayList<>());
        params.put("$", new ArrayList<>());
        while(begin != -1 && end != -1) {
            begin = Math.min(sql.indexOf("#{", end), sql.indexOf("${", end));
            begin = begin != -1 ? begin : Math.max(sql.indexOf("#{", end), sql.indexOf("${", end));
            end = sql.indexOf("}", begin);
            if(begin == -1 || end == -1) {
                break;
            }
            Object o = sql.charAt(begin) == '#' ? params.get("#").add(sql.substring(begin + 2, end)) : params.get("$").add(sql.substring(begin + 2, end));
        }
        return params;
    }
}
