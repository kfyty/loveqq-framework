package com.kfyty.loveqq.framework.data.jdbc.util;

import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import com.kfyty.loveqq.framework.core.lang.Value;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.data.jdbc.annotation.Param;
import com.kfyty.loveqq.framework.data.jdbc.annotation.Query;
import com.kfyty.loveqq.framework.data.jdbc.annotation.SubQuery;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.invokeMethod;

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
     * 检查注解中的 key 属性，并设置到 {@link SimpleGeneric#mapKey}
     *
     * @param annotation 注解
     * @param returnType 返回值类型
     */
    public static void checkMapKey(Annotation annotation, SimpleGeneric returnType) {
        if (!(annotation instanceof Query || annotation instanceof SubQuery)) {
            return;
        }
        returnType.setMapKey(invokeMethod(annotation, "key"));
    }

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
        Map<String, MethodParameter> params = new LinkedHashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Param annotation = AnnotationUtil.findAnnotation(parameter, Param.class);
            String paramName = annotation != null && CommonUtil.notEmpty(annotation.value()) ? annotation.value() : parameter.getName();
            params.put(paramName, new MethodParameter(method, parameter, args[i], paramName));
        }
        return params;
    }

    /**
     * 解析 sql 中的 #{}/${} 中的字符串，并分别保存到 Map
     *
     * @param value sql 语句
     * @return 解析得到的包含 #{}/${} 的 Map
     */
    public static Map<String, List<String>> resolvePlaceholderParameters(Value<String> value) {
        String sql = value.get();
        StringBuilder builder = new StringBuilder();
        List<String> hashesParams = new ArrayList<>();
        List<String> dollarParams = new ArrayList<>();

        int offset = 0;
        char[] src = value.get().toCharArray();                                                 // 从数组中复制，避免内存拷贝
        int begin1 = sql.indexOf("#{");
        int begin2 = sql.indexOf("${");
        int begin = begin1 == -1 ? begin2 : (begin2 == -1 ? begin1 : Math.min(begin1, begin2));
        int end = sql.indexOf("}", begin);
        while (begin != -1 && end != -1) {
            // 构建 SQL
            builder.append(src, offset, begin - offset);
            String variable = sql.substring(begin + 2, end).trim();
            if (sql.charAt(begin) == '$') {
                builder.append(src, begin, end - begin + 1);
                dollarParams.add(variable);
            } else {
                builder.append('?');
                hashesParams.add(variable);
            }

            // 更新索引
            begin1 = begin1 == -1 ? -1 : sql.indexOf("#{", end);
            begin2 = begin2 == -1 ? -1 : sql.indexOf("${", end);
            begin = begin1 == -1 ? begin2 : (begin2 == -1 ? begin1 : Math.min(begin1, begin2));
            if (begin != -1) {
                builder.append(src, end + 1, begin - end - 1);
            } else {
                builder.append(src, end + 1, src.length - end - 1);                     // 到达结尾，复制剩下的内容
            }
            end = sql.indexOf("}", begin);
            offset = begin;
        }

        // 更新 SQL
        if (!builder.isEmpty()) {
            value.set(builder.toString());
        }

        Map<String, List<String>> params = new HashMap<>(4);
        params.put("#", hashesParams);
        params.put("$", dollarParams);
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
     * @see SQLParametersResolveUtil#resolvePlaceholderParameters(Value)
     */
    public static Pair<String, MethodParameter[]> resolveSQL(String sql, Map<String, MethodParameter> parameters) {
        int index = 0;
        Value<String> valueSQL = new Value<>(sql);
        Map<String, List<String>> params = resolvePlaceholderParameters(valueSQL);
        MethodParameter[] args = new MethodParameter[params.get("#").size()];
        for (Map.Entry<String, List<String>> next : params.entrySet()) {
            for (String param : next.getValue()) {
                Object value = null;
                Class<?> paramType = null;
                if (!param.contains(".")) {
                    MethodParameter methodParam = parameters.get(param);
                    value = methodParam.getValue();
                    paramType = methodParam.getParamType();
                } else {
                    int rootIndex = param.indexOf(".");
                    String nested = param.substring(rootIndex + 1);
                    Object root = parameters.get(param.substring(0, rootIndex)).getValue();
                    value = ReflectUtil.resolveValue(nested, root);
                    paramType = value == null ? null : value.getClass();
                }
                if (value == null && log.isDebugEnabled()) {
                    log.debug("discovery null parameter: [{}] !", param);
                }
                if ("#".equals(next.getKey())) {
                    args[index++] = new MethodParameter(paramType, value, param);
                    continue;
                }
                valueSQL.set(valueSQL.get().replace("${" + param + "}", String.valueOf(value)));
            }
        }
        return new Pair<>(valueSQL.get(), args);
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
            param.put(mapperField[i], new MethodParameter(field.getType(), ReflectUtil.getFieldValue(obj, field), mapperField[i]));
        }
        return param;
    }
}
