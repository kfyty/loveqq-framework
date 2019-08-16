package com.kfyty.jdbc;

import com.kfyty.jdbc.annotation.Execute;
import com.kfyty.jdbc.annotation.ForEach;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * 基本数据类型
     */
    private static final String[] NUMBER_TYPE = {
            "byte", "short", "int", "long", "float", "double",
            "Byte", "Short", "Integer", "Long", "Float", "Double", "String"
    };

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
    private Object query(Class<?> returnType, Annotation annotation, Map<String, Object> params) throws Exception {
        String annotationName = annotation.annotationType().getSimpleName();
        String methodName = Character.toLowerCase(annotationName.charAt(0)) + annotationName.substring(1);
        String sql = this.parseForEach(annotation, params);
        Map<String, Object> map = this.parseSQL(sql, params);
        Method method = this.getClass().getDeclaredMethod(methodName, Annotation.class, Class.class, String.class, Object[].class);
        return method.invoke(this, annotation, returnType, map.get("sql"), map.get("args"));
    }

    /**
     * 转换数据为集合
     * @param forEach   注解
     * @param value     可能的值，或集合，或数组
     * @return
     */
    private List<Object> convert2List(ForEach forEach, Object value) {
        List<Object> list = new ArrayList<>();
        if(value instanceof Collection) {
            list.addAll((Collection) value);
        } else if(value.getClass().isArray()) {
            list.addAll(Arrays.asList((Object[]) value));
        } else {
            log.error(": parse foreach error, parameter is not collection or array !");
            return null;
        }
        return list;
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
        if(!(annotation instanceof SelectList || annotation instanceof Execute)) {
            return sql;
        }
        ForEach[] forEaches = (ForEach[]) annotation.getClass().getDeclaredMethod("forEach").invoke(annotation);
        if(CommonUtil.empty(forEaches)) {
            return sql;
        }
        StringBuilder builder = new StringBuilder();
        for (ForEach each : forEaches) {
            List<Object> list = this.convert2List(each, params.get(each.collection()));
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
            Field field = this.getField(objClass, paramField[i]);
            field.setAccessible(true);
            param.put(mapperField[i], field.get(obj));
        }
        return param;
    }

    /**
     * 处理子查询
     * @param annotation    子查询注解
     * @param outerObj      父查询映射的结果对象，若是集合，则为其中的每一个对象
     * @throws Exception
     */
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
     * 获取属性的类型，若存在泛型则返回泛型类型
     * @param field     属性
     * @return          类型
     */
    public Class<?> getReturnType(Field field) {
        Type type = field.getGenericType();
        return type instanceof ParameterizedType ? (Class<?>)((ParameterizedType) type).getActualTypeArguments()[0] : field.getType();
    }

    /**
     * 获取方法的返回值类型，若存在泛型则返回泛型类型
     * @param method    方法
     * @return          类型
     */
    public Class<?> getReturnType(Method method) {
        Type type = method.getGenericReturnType();
        return type instanceof ParameterizedType ? (Class<?>)((ParameterizedType) type).getActualTypeArguments()[0] : method.getReturnType();
    }

    /**
     * 获取对象的属性，包括其父类
     * @param clazz         属性所属对象的 Class 类型
     * @param fieldName     属性名称
     * @return              属性
     * @throws Exception
     */
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
        if(annotations.length == 1) {
            return this.query(this.getReturnType(method), annotations[0], this.getParamFromMethod(method.getParameters(), args));
        }
        List<Object> os = new ArrayList<>();
        for(Annotation annotation : annotations) {
            os.add(this.query(this.getReturnType(method), annotation, this.getParamFromMethod(method.getParameters(), args)));
        }
        return os;
    }

    /**
     * 根据属性参数，解析出 value 中对应的属性值
     * @param param     属性参数，eg: obj.value
     * @param value     对象，eg：obj
     * @return          属性值，eg：value
     * @throws Exception
     */
    public Object parseValue(String param, Object value) throws Exception {
        String[] fields = param.split("\\.");
        for(int i = 1; i < fields.length; i++) {
            Field field = value.getClass().getDeclaredField(fields[i]);
            field.setAccessible(true);
            value = field.get(value);
        }
        return value;
    }

    /**
     * 根据属性参数，将 value 设置到 obj 中，与 @see parseValue() 过程相反
     * @param param     属性参数
     * @param obj       对象
     * @param value     属性值
     * @throws Exception
     */
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
                Object o = param.contains(".") ? this.parseValue(param, parameters.get(param.split("\\.")[0])) : parameters.get(param);
                if(o == null) {
                    log.error(": null parameter found:[{}] --> parameter value:[{}] !", param, o);
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

    /**
     * 返回单个值，@see selectList()
     */
    private  <T> T selectOne(Annotation annotation, Class<T> clazz, String sql, Object ... params) throws Exception {
        return Optional.ofNullable(selectList(annotation, clazz, sql, params)).filter(e -> !e.isEmpty()).map(e -> e.get(0)).orElse(null);
    }

    /**
     * 对外接口
     */
    public <T> T selectOne(Class<T> clazz, String sql, Object ... params) throws Exception {
        return selectOne(null, clazz, sql, params);
    }

    /**
     * 返回集合
     * @param annotation    注解
     * @param clazz         返回值类型
     * @param sql           sql
     * @param params        参数
     * @param <T>           泛型
     * @return              结果集
     * @throws Exception
     */
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

    /**
     * 对外接口
     */
    public <T> List<T> selectList(Class<T> clazz, String sql, Object ... params) throws Exception {
        return selectList(null, clazz, sql, params);
    }

    /**
     * 执行无返回值的 sql 语句
     * @param annotation    注解
     * @param non           无效，为反射而存在
     * @param sql           sql 语句
     * @param params        参数
     * @throws SQLException
     */
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

    /**
     * 对外接口
     */
    public void execute(String sql, Object ... params) throws SQLException {
        this.execute(null, null, sql, params);
    }

    /**
     * 获取 PreparedStatement 对象
     * @param connection    数据库连接
     * @param sql           sql 语句
     * @param params        参数
     * @return              PreparedStatement 对象
     * @throws SQLException
     */
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

    /**
     * 获取对象的属性，并放入 Map
     * @param clazz     对象的 Class 对象
     * @return
     */
    public Map<String, Field> getFieldMap(Class<?> clazz) {
        if(clazz == null || clazz.getSimpleName().equals("Object")) {
            return new HashMap<>(0);
        }
        Map<String, Field> map = new HashMap<>();
        map.putAll(Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.toMap(Field::getName, e -> e)));
        map.putAll(getFieldMap(clazz.getSuperclass()));
        return map;
    }

    /**
     * 将基本数据类型转换为包装类
     * @param clazz     需要转换的 Class 对象
     * @param <T>       泛型
     * @return          包装类型的 Class 对象
     * @throws Exception
     */
    public <T> Class<T> convert2Wrapper(Class<T> clazz) throws Exception {
        String type = clazz.getSimpleName();
        return Character.isUpperCase(type.charAt(0)) ? clazz :
                type.equals("int") ? (Class<T>) Integer.class : (Class<T>) Class.forName("java.lang." + Character.toUpperCase(type.charAt(0)) + type.substring(1));
    }

    /**
     * 填充基本数据类型数据
     * @param resultSet     结果集
     * @param clazz         返回值类型
     * @param <T>           泛型
     * @return              返回值
     * @throws Exception
     */
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

    /**
     * 填充对象数据
     * @param resultSet     结果集
     * @param clazz         返回值类型
     * @param <T>           泛型
     * @return              返回值
     * @throws Exception
     */
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
