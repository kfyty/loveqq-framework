package com.kfyty.jdbc;

import com.kfyty.jdbc.annotation.ForEach;
import com.kfyty.jdbc.annotation.Param;
import com.kfyty.jdbc.annotation.Query;
import com.kfyty.jdbc.annotation.SubQuery;
import com.kfyty.support.jdbc.ReturnType;
import com.kfyty.util.CommonUtil;
import com.kfyty.util.JdbcUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
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
@NoArgsConstructor
@AllArgsConstructor
public class SqlSession implements InvocationHandler {
    /**
     * 数据源
     */
    @Setter
    private DataSource dataSource;

    /**
     * 根据注解类名调用相应的方法
     * @param returnType    返回值类型
     * @param annotation    注解
     * @param params        参数
     * @return              返回值
     * @throws Exception
     */
    private Object requestQuery(Annotation annotation, ReturnType returnType, Map<String, Object> params) throws Exception {
        checkMapKey(annotation, returnType);
        String annotationName = annotation.annotationType().getSimpleName();
        String methodName = Character.toLowerCase(annotationName.charAt(0)) + annotationName.substring(1);
        String sql = this.parseForEach(annotation, params);
        Map<String, Object> map = this.parseSQL(sql, params);
        Method method = JdbcUtil.class.getDeclaredMethod(methodName, DataSource.class, ReturnType.class, String.class, Object[].class);
        method.setAccessible(true);
        Object obj = method.invoke(null, dataSource, returnType, map.get("sql"), map.get("args"));
        this.handleSubQuery(annotation, obj);
        return obj;
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
        returnType.setKey((String) annotation.getClass().getDeclaredMethod("key").invoke(annotation));
    }

    /**
     * 解析 ForEach 注解，并将对应的参数添加到 Map
     * @param annotation    注解
     * @param params        参数
     * @return              拼接完毕的 sql
     * @throws Exception
     */
    private String parseForEach(Annotation annotation, Map<String, Object> params) throws Exception {
        String sql = (String) annotation.annotationType().getDeclaredMethod("value").invoke(annotation);
        if(CommonUtil.empty(sql)) {
            throw new NullPointerException("sql statement is null !");
        }

        ForEach[] forEachList = (ForEach[]) annotation.getClass().getDeclaredMethod("forEach").invoke(annotation);
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
        return sql + builder.toString();
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
        Class<?> objClass = obj.getClass();
        for (int i = 0; i < paramField.length; i++) {
            Field field = CommonUtil.getField(objClass, paramField[i]);
            field.setAccessible(true);
            param.put(mapperField[i], field.get(obj));
        }
        return param;
    }

    /**
     * 处理子查询
     * @param annotation    包含子查询的注解
     * @param obj           父查询的结果集
     * @throws Exception
     */
    private void handleSubQuery(Annotation annotation, Object obj) throws Exception {
        if(!(annotation instanceof Query && obj != null)) {
            return ;
        }
        SubQuery[] subQueries = (SubQuery[]) annotation.getClass().getMethod("subQuery").invoke(annotation);
        if(obj instanceof Collection) {
            for (Object o : (Collection) obj) {
                this.handleSubQuery(subQueries, o);
            }
            return ;
        }
        if(obj instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) obj;
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                this.handleSubQuery(subQueries, entry.getValue());
            }
            return ;
        }
        this.handleSubQuery(subQueries, obj);
    }

    /**
     * 处理子查询
     * @param subQueries    子查询注解
     * @param obj           父查询映射的结果对象，若是集合，则为其中的每一个对象
     * @throws Exception
     */
    private void handleSubQuery(SubQuery[] subQueries, Object obj) throws Exception {
        if(CommonUtil.empty(subQueries) || obj == null) {
            return ;
        }
        for (SubQuery subQuery : subQueries) {
            Field returnField = CommonUtil.getField(obj.getClass(), subQuery.returnField());
            Map<String, Object> params = this.getParamFromAnnotation(subQuery.paramField(), subQuery.mapperField(), obj);
            ReturnType returnType = ReturnType.getReturnType(returnField.getGenericType(), returnField.getType());
            returnField.setAccessible(true);
            returnField.set(obj, this.requestQuery(subQuery, returnType, params));
        }
    }

    /**
     * 获取接口代理对象
     * @param interfaces    接口 Class 对象
     * @param <T>           泛型
     * @return              代理对象
     */
    public <T> T getProxyObject(Class<T> interfaces) {
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] {interfaces}, this);
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
            Object o = annotation.annotationType().getDeclaredMethod("value").invoke(annotation);
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
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Annotation[] annotations = this.getAnnotationFromMethod(method);
        ReturnType returnType = ReturnType.getReturnType(method.getGenericReturnType(), method.getReturnType());
        if(annotations.length == 1) {
            return this.requestQuery(annotations[0], returnType, this.getParamFromMethod(method.getParameters(), args));
        }
        List<Object> os = new ArrayList<>();
        for(Annotation annotation : annotations) {
            os.add(this.requestQuery(annotation, returnType, this.getParamFromMethod(method.getParameters(), args)));
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
