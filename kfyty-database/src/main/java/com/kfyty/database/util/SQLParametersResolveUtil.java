package com.kfyty.database.util;

import com.kfyty.database.jdbc.annotation.Param;
import com.kfyty.support.method.MethodParameter;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static com.kfyty.support.utils.CommonUtil.PARAMETERS_PATTERN;

/**
 * 描述: SQL 解析工具
 *
 * @author kfyty725
 * @date 2021/9/19 9:34
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class SQLParametersResolveUtil {
    /**
     * 将方法参数中有 @Param 注解的参数封装为 Map
     * 若 {@link Param} 注解不存在，则直接使用 {@link Parameter#getName()}
     *
     * @param method 方法
     * @param args   参数数组
     * @return 参数 Map
     */
    public static Map<String, MethodParameter> processMethodParameters(Method method, Object[] args) {
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
     * 替换 sql 中的 #{} 为 ?
     *
     * @param sql    sql 语句
     * @param params 解析出的 #{} 中的字符串集合
     * @return 替换后的 sql
     */
    public static String replaceParameters(String sql, List<String> params) {
        for (String param : params) {
            sql = sql.replace("#{" + param + "}", "?");
        }
        return sql;
    }

    /**
     * 解析 sql 中的 #{}/${} 中的字符串，并分别保存到 Map
     *
     * @param sql sql 语句
     * @return 解析得到的包含 #{}/${} 的 Map
     */
    public static Map<String, List<String>> resolvePlaceholderParameters(String sql) {
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

    /**
     * 解析 sql 中的 #{} 为 ? ，并将参数对应保存到集合中；
     * 解析 sql 中的 ${} ，并使用相应的值直接替换掉
     *
     * @param sql        sql 语句
     * @param parameters MethodParameter
     * @return Pair<String, MethodParameter [ ]>，包含解析后的 sql 以及对应的参数数组
     * @see SQLParametersResolveUtil#processMethodParameters(Method, Object[])
     * @see SQLParametersResolveUtil#resolvePlaceholderParameters(String)
     */
    public static Pair<String, MethodParameter[]> resolveSQL(String sql, Map<String, MethodParameter> parameters) {
        List<MethodParameter> args = new ArrayList<>();
        Map<String, List<String>> params = resolvePlaceholderParameters(sql);
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
        return new Pair<>(replaceParameters(sql, params.get("#")), args.toArray(new MethodParameter[0]));
    }

    /**
     * 根据参数属性/映射属性提取参数到 Map
     *
     * @param paramField  参数属性，对应为 obj 字段
     * @param mapperField 映射属性，根据下标，将提取的参数属性的值映射为另一个值
     * @param obj         数据提供对象
     * @return Map<String, MethodParameter>
     */
    public static Map<String, MethodParameter> resolveMappingParameters(String[] paramField, String[] mapperField, Object obj) {
        if (CommonUtil.empty(paramField) || CommonUtil.empty(mapperField)) {
            return Collections.emptyMap();
        }
        if (paramField.length != mapperField.length) {
            throw new IllegalArgumentException("parameters number and mapper field number can't match !");
        }
        Map<String, MethodParameter> param = new HashMap<>();
        for (int i = 0; i < paramField.length; i++) {
            Field field = ReflectUtil.getField(obj.getClass(), paramField[i]);
            param.put(mapperField[i], new MethodParameter(field.getType(), ReflectUtil.getFieldValue(obj, field)));
        }
        return param;
    }
}
