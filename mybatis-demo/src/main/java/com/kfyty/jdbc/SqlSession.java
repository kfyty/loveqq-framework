package com.kfyty.jdbc;

import com.kfyty.annotation.Param;
import com.sun.istack.internal.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
public class SqlSession implements InvocationHandler {
    private static final String[] NUMBER_TYPE = {
            "byte", "short", "int", "long", "float", "double",
            "Byte", "Short", "Integer", "Long", "Float", "Double", "String"
    };

    @Getter
    private Integer count;

    @Setter
    private DataSource dataSource;

    public SqlSession(DataSource dataSource) {
        this.count = 0;
        this.dataSource = dataSource;
    }

    private Object query(Class<?> returnType, Annotation annotation, Map<String, Object> params) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        String annotationName = annotation.annotationType().getSimpleName();
        String methodName = Character.toLowerCase(annotationName.charAt(0)) + annotationName.substring(1);
        String sql = (String) annotation.annotationType().getDeclaredMethod("value").invoke(annotation);
        Map<String, Object> map = this.parseSQL(sql, params);
        Method method = this.getClass().getMethod(methodName, Class.class, String.class, Object[].class);
        return method.invoke(this, returnType, map.get("sql"), map.get("args"));
    }

    public <T> T getProxyObject(Class<T> interfaces) {
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] {interfaces}, this);
    }

    public Class<?> getReturnType(Method method) {
        Type type = method.getGenericReturnType();
        return type instanceof ParameterizedType ? (Class<?>)((ParameterizedType) type).getActualTypeArguments()[0] : method.getReturnType();
    }

    public Annotation[] getAnnotationFromMethod(Method method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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

    public <T> T selectOne(Class<T> clazz, String sql, Object ... params) throws IllegalAccessException, SQLException, InstantiationException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        if(Arrays.stream(NUMBER_TYPE).filter(e -> e.equals(clazz.getSimpleName())).anyMatch(e -> e.length() > 0)) {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement = this.getPreparedStatement(connection, sql, params);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                if(log.isDebugEnabled()) {
                    log.debug(": {} --> execute sql statement:[{}] --> parameters:{} --> return type:[{}]", ++count, sql, params, clazz);
                }
                return Optional.ofNullable(this.fillBaseType(resultSet, clazz)).orElse(null);
            }
        }
        return Optional.ofNullable(selectList(clazz, sql, params)).filter(e -> !e.isEmpty()).map(e -> e.get(0)).orElse(null);
    }

    public <T> List<T> selectList(Class<T> clazz, String sql, Object ... params) throws SQLException, InstantiationException, IllegalAccessException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = this.getPreparedStatement(connection, sql, params);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if(log.isDebugEnabled()) {
                log.debug(": {} --> execute sql statement:[{}] --> parameters:{} --> return type:[{}]", ++count, sql, params, clazz);
            }
            return fillObject(resultSet, clazz);
        }
    }

    public void execute(@Nullable Class<?> non, String sql, Object ... params) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = this.getPreparedStatement(connection, sql, params)) {
            if(log.isDebugEnabled()) {
                log.debug(": {} --> execute sql statement:[{}] --> parameters:{}", ++count, sql, params);
            }
            preparedStatement.execute();
        }
    }

    public PreparedStatement getPreparedStatement(Connection connection, String sql, Object ... params) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for(int i = 0; params != null && i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
        return preparedStatement;
    }

    public Map<String, Field> getSuperClassField(Class<?> clazz, boolean containPrivate) {
        return Arrays.stream(clazz.getSuperclass().getDeclaredFields()).filter(e -> containPrivate || !Modifier.isPrivate(e.getModifiers())).collect(Collectors.toMap(Field::getName, e -> e));
    }

    public <T> Class<T> convert2Wrapper(Class<T> clazz) throws ClassNotFoundException {
        String type = clazz.getSimpleName();
        return Character.isUpperCase(type.charAt(0)) ? clazz :
                type.equals("int") ? (Class<T>) Integer.class : (Class<T>) Class.forName("java.lang." + Character.toUpperCase(type.charAt(0)) + type.substring(1));
    }

    public <T> T fillBaseType(ResultSet resultSet, Class<T> clazz) throws SQLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException {
        if(resultSet == null || !resultSet.next()) {
            log.error("fill number error: result set:[{}]", resultSet);
        }
        return this.convert2Wrapper(clazz).getConstructor(String.class).newInstance(resultSet.getString(1));
    }

    public  <T> List<T> fillObject(ResultSet resultSet, Class<T> clazz) throws SQLException, IllegalAccessException, InstantiationException {
        List<T> list = new ArrayList<>();
        Map<String, Field> fieldMap = Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.toMap(Field::getName, e -> e));
        fieldMap.putAll(this.getSuperClassField(clazz, false));
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
        column = Optional.ofNullable(column).map(String::toLowerCase).orElseThrow(() -> new NullPointerException("column is null"));
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
