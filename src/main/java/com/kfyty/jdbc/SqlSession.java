package com.kfyty.jdbc;

import com.kfyty.annotation.Param;
import com.sun.istack.internal.Nullable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
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
    @Setter
    private DataSource dataSource;

    private Object query(Class<?> returnType, Annotation annotation, Map<String, Object> params) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        String annotationName = annotation.annotationType().getSimpleName();
        String methodName = Character.toLowerCase(annotationName.charAt(0)) + annotationName.substring(1);
        String sql = (String) annotation.annotationType().getDeclaredMethod("value").invoke(annotation);
        Map<String, Object> map = this.parseSQL(sql, params);
        Method method = this.getClass().getMethod(methodName, Class.class, String.class, Object[].class);
        return method.invoke(this, returnType, map.get("sql"), map.get("args"));
    }

    public Object getProxyObject(Class<?> interfaces) {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] {interfaces}, this);
    }

    public Class<?> getReturnType(Method method) {
        Type type = method.getGenericReturnType();
        return type instanceof ParameterizedType ? (Class<?>)((ParameterizedType) type).getActualTypeArguments()[0] : method.getReturnType();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Annotation[] annotations = method.getAnnotations();
        if(annotations.length == 1) {
            return this.query(this.getReturnType(method), annotations[0], this.getParamFromMethod(method.getParameters(), args));
        }
        List<Object> os = new ArrayList<>();
        for(Annotation annotation : annotations) {
            os.add(this.query(this.getReturnType(method), annotation, this.getParamFromMethod(method.getParameters(), args)));
        }
        return os;
    }

    public Object parseValue(String param, Object value) throws NoSuchFieldException, IllegalAccessException {
        String[] fields = param.split("\\.");
        for(int i = 1; i < fields.length; i++) {
            Field field = value.getClass().getDeclaredField(fields[i]);
            field.setAccessible(true);
            value = field.get(value);
        }
        return value;
    }

    public Map<String, Object> parseSQL(String sql, Map<String, Object> parameters) throws NoSuchFieldException, IllegalAccessException {
        List<String> params = this.getParamFromSQL(sql);
        List<Object> args = new ArrayList<>();
        for(String param : params) {
            Object o = param.contains(".") ? this.parseValue(param, parameters.get(param.split("\\.")[0])) : parameters.get(param);
            if(o == null) {
                log.error("cannot find parameter:[{}] from method @Param annotation !", param);
                return null;
            }
            args.add(o);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("sql", this.replaceParam(sql, params));
        map.put("args", args.toArray());
        return map;
    }

    public <T> T selectOne(Class<T> clazz, String sql, Object ... params) throws Exception {
        List<T> list = selectList(clazz, sql, params);
        return list == null || list.size() == 0 ? null : list.get(0);
    }

    public <T> List<T> selectList(Class<T> clazz, String sql, Object ... params) throws SQLException, InstantiationException, IllegalAccessException {
        PreparedStatement preparedStatement = this.getPreparedStatement(sql, params);
        ResultSet resultSet = preparedStatement.executeQuery();
        List<T> list =  fillObject(resultSet, clazz);
        preparedStatement.close();
        return list;
    }

    public void execute(@Nullable Class<?> non, String sql, Object ... params) throws Exception {
        PreparedStatement preparedStatement = this.getPreparedStatement(sql, params);
        preparedStatement.execute();
        preparedStatement.close();
    }

    public PreparedStatement getPreparedStatement(String sql, Object ... params) throws SQLException {
        PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(sql);
        for(int i = 0; params != null && i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
        if(log.isDebugEnabled()) {
            log.debug("execute sql statement:[{}], parameters:{}", sql, params);
        }
        return preparedStatement;
    }

    public  <T> List<T> fillObject(ResultSet resultSet, Class<T> clazz) throws SQLException, IllegalAccessException, InstantiationException {
        List<T> list = new ArrayList<>();
        Map<String, Field> fieldMap = Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.toMap(e -> e.getName(), e -> e));
        while (resultSet.next()) {
            T o = clazz.newInstance();
            ResultSetMetaData metaData = resultSet.getMetaData();
            for(int i = 1; i <= metaData.getColumnCount(); i++) {
                String fieldName = getFieldName(metaData.getColumnLabel(i));
                Field field = fieldMap.get(fieldName);
                if(field != null) {
                    field.setAccessible(true);
                    field.set(o, resultSet.getObject(metaData.getColumnLabel(i)));
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

    public String getFieldName(String column) {
        column = Optional.ofNullable(column).map(e -> e.toLowerCase()).orElseThrow(() -> new NullPointerException("column is null"));
        while(column.contains("_")) {
            int index = column.indexOf('_');
            if(index < column.length() - 1) {
                char ch = column.charAt(index + 1);
                column = column.replace("_" + ch, "" + Character.toUpperCase(ch));
            }
        }
        return column;
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

    public List<String> getParamFromSQL(String sql) {
        int begin = 0;
        int end = begin;
        List<String> params = new ArrayList<>();
        while(begin != -1 && end != -1) {
            begin = sql.indexOf("#{", end);
            end = sql.indexOf("}", begin);
            if(begin == -1 || end == -1) {
                break;
            }
            params.add(sql.substring(begin + 2, end));
        }
        return params;
    }

    public void close() throws SQLException {
        synchronized (this) {
            if(!dataSource.getConnection().isClosed()) {
                dataSource.getConnection().close();
            }
            dataSource = null;
        }
    }
}
