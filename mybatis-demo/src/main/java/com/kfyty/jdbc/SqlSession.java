package com.kfyty.jdbc;

import com.kfyty.jdbc.annotation.Param;
import com.kfyty.jdbc.annotation.SelectList;
import com.kfyty.jdbc.annotation.SelectOne;
import com.kfyty.jdbc.annotation.SubQuery;
import com.kfyty.util.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 功能描述: SqlSession
 *
 * @author zhangkun@wisdombud.com
 * @date 2019/6/27 16:04
 * @since JDK 1.8
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class SqlSession implements InvocationHandler {
    private static final String[] NUMBER_TYPE = {
            "byte", "short", "int", "long", "float", "double",
            "Byte", "Short", "Integer", "Long", "Float", "Double", "String"
    };

    @Setter
    private DataSource dataSource;

    private Object query(Class<?> returnType, Annotation annotation, Map<String, Object> params) throws Exception {
        String annotationName = annotation.annotationType().getSimpleName();
        String methodName = Character.toLowerCase(annotationName.charAt(0)) + annotationName.substring(1);
        String sql = (String) annotation.annotationType().getDeclaredMethod("value").invoke(annotation);
        Map<String, Object> map = this.parseSQL(sql, params);
        Method method = this.getClass().getDeclaredMethod(methodName, Annotation.class, Class.class, String.class, Object[].class);
        return method.invoke(this, annotation, returnType, map.get("sql"), map.get("args"));
    }

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
            Field field = this.getField(objClass, paramField[i]);
            field.setAccessible(true);
            param.put(mapperField[i], field.get(obj));
        }
        return param;
    }

    private void handleSubQuery(Annotation annotation, Object outerObj) throws Exception {
        if(annotation == null || outerObj == null) {
            return ;
        }
        if(!(annotation instanceof SelectOne || annotation instanceof SelectList)) {
            return ;
        }
        SubQuery[] subQuerys = (SubQuery[]) annotation.getClass().getMethod("subQuery").invoke(annotation);
        if(CommonUtil.empty(subQuerys)) {
            return ;
        }
        for (SubQuery subQuery : subQuerys) {
            Field returnField = this.getField(outerObj.getClass(), subQuery.returnField());
            Map<String, Object> param = this.getParamFromAnnotation(subQuery.paramField(), subQuery.mapperField(), outerObj);
            Map<String, Object> sqlMap = this.parseSQL(subQuery.query(), param);
            String sql = (String) sqlMap.get("sql");
            Object[] args = (Object[]) sqlMap.get("args");
            Object o = subQuery.returnSingle() ? this.selectOne(getReturnType(returnField), sql, args) : this.selectList(getReturnType(returnField), sql, args);
            returnField.setAccessible(true);
            returnField.set(outerObj, o);
        }
    }

    public <T> T getProxyObject(Class<T> interfaces) {
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] {interfaces}, this);
    }

    public Class<?> getReturnType(Field field) {
        Type type = field.getGenericType();
        return type instanceof ParameterizedType ? (Class<?>)((ParameterizedType) type).getActualTypeArguments()[0] : field.getType();
    }

    public Class<?> getReturnType(Method method) {
        Type type = method.getGenericReturnType();
        return type instanceof ParameterizedType ? (Class<?>)((ParameterizedType) type).getActualTypeArguments()[0] : method.getReturnType();
    }

    public Field getField(Class<?> clazz, String fieldName) throws Exception {
        if(clazz.getSimpleName().equals("Object")) {
            log.error(": no field found:[{}] !", fieldName);
            return null;
        }
        try {
            return clazz.getDeclaredField(fieldName);
        } catch(NoSuchFieldException e) {
            return getField(clazz.getSuperclass(), fieldName);
        }
    }

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

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Annotation[] annotations = this.getAnnotationFromMethod(method);
        if(annotations.length == 1) {
            return this.query(this.getReturnType(method), annotations[0], this.getParamFromMethod(method.getParameters(), args));
        }
        List<Object> os = new ArrayList<>();
        for(Annotation annotation : annotations) {
            os.add(this.query(this.getReturnType(method), annotation, this.getParamFromMethod(method.getParameters(), args)));
        }
        return os;
    }

    public Object parseValue(String param, Object value) throws Exception {
        String[] fields = param.split("\\.");
        for(int i = 1; i < fields.length; i++) {
            Field field = value.getClass().getDeclaredField(fields[i]);
            field.setAccessible(true);
            value = field.get(value);
        }
        return value;
    }

    public void parseField(String param, Object obj, Object value) throws Exception {
        Class<?> clazz = obj.getClass();
        String[] fields = param.split("\\.");
        for(int i = 0; i < fields.length; i++) {
            Field field = clazz.getDeclaredField(fields[i]);
            field.setAccessible(true);
            if(i == fields.length - 1) {
                field.set(obj, value);
                break;
            }
            if(field.get(obj) == null) {
                Object fieldValue = field.getType().newInstance();
                field.set(obj, fieldValue);
                obj = fieldValue;
            }
            clazz = field.getType();
        }
    }

    public Map<String, Object> parseSQL(String sql, Map<String, Object> parameters) throws Exception {
        List<Object> args = new ArrayList<>();
        Map<String, List<String>> params = this.getParamFromSQL(sql);
        for(Map.Entry<String, List<String>> next : params.entrySet()) {
            for(String param : next.getValue()) {
                Object o = param.contains(".") ? this.parseValue(param, parameters.get(param.split("\\.")[0])) : parameters.get(param);
                if(o == null) {
                    log.error(": cannot find parameter:[{}] from method @Param annotation !", param);
                    return null;
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

    private  <T> T selectOne(Annotation annotation, Class<T> clazz, String sql, Object ... params) throws Exception {
        return Optional.ofNullable(selectList(annotation, clazz, sql, params)).filter(e -> !e.isEmpty()).map(e -> e.get(0)).orElse(null);
    }

    public <T> T selectOne(Class<T> clazz, String sql, Object ... params) throws Exception {
        return selectOne(null, clazz, sql, params);
    }

    private  <T> List<T> selectList(Annotation annotation, Class<T> clazz, String sql, Object ... params) throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = this.getPreparedStatement(connection, sql, params);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            List<T> list = fillObject(resultSet, clazz);
            if(!CommonUtil.empty(list)) {
                for (T t : list) {
                    this.handleSubQuery(annotation, t);
                }
            }
            if(log.isDebugEnabled()) {
                log.debug(": executed sql statement:[{}] --> parameters:{} --> return type:[{}] --> return number:[{}]", sql, params, clazz, list.size());
            }
            return list;
        } catch(Exception e) {
            log.error(": failed execute sql statement:[{}] --> parameters:{}", sql, params);
            throw e;
        }
    }

    public <T> List<T> selectList(Class<T> clazz, String sql, Object ... params) throws Exception {
        return selectList(null, clazz, sql, params);
    }

    private void execute(Annotation annotation, Class<?> non, String sql, Object ... params) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = this.getPreparedStatement(connection, sql, params)) {
            preparedStatement.execute();
            if(log.isDebugEnabled()) {
                log.debug(": executed sql statement:[{}] --> parameters:{}", sql, params);
            }
        } catch(SQLException e) {
            log.error(": failed execute sql statement:[{}] --> parameters:{}", sql, params);
            throw e;
        }
    }

    public void execute(String sql, Object ... params) throws SQLException {
        this.execute(null, null, sql, params);
    }

    public PreparedStatement getPreparedStatement(Connection connection, String sql, Object ... params) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for(int i = 0; params != null && i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
        if(log.isDebugEnabled()) {
            log.debug(": prepare statement:[{}] --> parameters:{}", sql, params);
        }
        return preparedStatement;
    }

    public Map<String, Field> getFieldMap(Class<?> clazz) {
        if(clazz == null || clazz.getSimpleName().equals("Object")) {
            return new HashMap<>(0);
        }
        Map<String, Field> map = new HashMap<>();
        map.putAll(Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.toMap(Field::getName, e -> e)));
        map.putAll(getFieldMap(clazz.getSuperclass()));
        return map;
    }

    public <T> Class<T> convert2Wrapper(Class<T> clazz) throws Exception {
        String type = clazz.getSimpleName();
        return Character.isUpperCase(type.charAt(0)) ? clazz :
                type.equals("int") ? (Class<T>) Integer.class : (Class<T>) Class.forName("java.lang." + Character.toUpperCase(type.charAt(0)) + type.substring(1));
    }

    public <T> List<T> fillBaseType(ResultSet resultSet, Class<T> clazz) throws Exception {
        if(resultSet == null) {
            log.error(": fill number error: result set:[{}]", resultSet);
        }
        List<T> list = new ArrayList<>();
        Constructor<T> constructor = this.convert2Wrapper(clazz).getConstructor(String.class);
        while(resultSet.next()) {
            list.add(constructor.newInstance(resultSet.getString(1)));
        }
        return list;
    }

    public  <T> List<T> fillObject(ResultSet resultSet, Class<T> clazz) throws Exception {
        if(Arrays.stream(NUMBER_TYPE).filter(e -> e.equals(clazz.getSimpleName())).anyMatch(e -> e.length() > 0)) {
            return this.fillBaseType(resultSet, clazz);
        }
        List<T> list = new ArrayList<>();
        Map<String, Field> fieldMap = this.getFieldMap(clazz);
        while (resultSet.next()) {
            T o = clazz.newInstance();
            ResultSetMetaData metaData = resultSet.getMetaData();
            for(int i = 1; i <= metaData.getColumnCount(); i++) {
                String fieldName = CommonUtil.convert2Hump(metaData.getColumnLabel(i), false);
                Field field = fieldMap.get(fieldName);
                if(field != null) {
                    field.setAccessible(true);
                    field.set(o, resultSet.getObject(metaData.getColumnLabel(i)));
                    continue;
                }
                if(fieldName.contains(".")) {
                    this.parseField(fieldName, o, resultSet.getObject(metaData.getColumnLabel(i)));
                    continue;
                }
                if(log.isDebugEnabled()) {
                    log.debug("Found column: [{}], but class:[{}] not field found!", metaData.getColumnName(i), clazz);
                }
            }
            list.add(o);
        }
        return list;
    }

    public String replaceParam(String sql, List<String> params) {
        for(String param : params) {
            sql = sql.replace("#{" + param + "}", "?");
        }
        return sql;
    }

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
