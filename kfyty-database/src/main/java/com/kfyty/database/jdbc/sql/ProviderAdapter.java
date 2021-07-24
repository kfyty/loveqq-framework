package com.kfyty.database.jdbc.sql;

import com.kfyty.database.jdbc.sql.dialect.MySQLProvider;
import com.kfyty.support.method.MethodParameter;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 描述: SQL 提供者适配器，根据数据库方言进行转发
 *
 * @author kfyty725
 * @date 2021/6/8 10:49
 * @email kfyty725@hotmail.com
 */
public class ProviderAdapter {
    private static final String DEFAULT_PROVIDER_DIALECT = "mysql";

    private static final Map<String, Class<?>> DIALECT_MAP = new HashMap<>(4);

    private static String DIALECT = DEFAULT_PROVIDER_DIALECT;

    static {
        addDialectProvider(DEFAULT_PROVIDER_DIALECT, MySQLProvider.class);
    }

    public static void addDialectProvider(String dialect, Class<?> provider) {
        DIALECT_MAP.put(dialect, provider);
    }

    public static void setDialect(String dialect) {
        if (!DIALECT_MAP.containsKey(dialect)) {
            throw new IllegalArgumentException("does not support this dialect: " + dialect);
        }
        ProviderAdapter.DIALECT = dialect;
    }

    /**
     * 提供 SQL
     *
     * @param mapperClass  mapper class
     * @param sourceMethod 代理方法
     * @param annotation   注解
     * @param params       方法参数
     * @return SQL
     */
    public String doProvide(Class<?> mapperClass, Method sourceMethod, Annotation annotation, Map<String, MethodParameter> params) {
        Object provider = ReflectUtil.newInstance(ProviderAdapter.DIALECT_MAP.get(ProviderAdapter.DIALECT));
        String methodName = Optional.ofNullable(ReflectUtil.invokeSimpleMethod(annotation, "method")).map(e -> (String) e).filter(CommonUtil::notEmpty).orElse(sourceMethod.getName());
        Method method = ReflectUtil.getMethod(provider.getClass(), methodName, Class.class, Method.class, annotation.annotationType(), Map.class);
        return (String) ReflectUtil.invokeMethod(provider, method, mapperClass, sourceMethod, annotation, params);
    }
}
