package com.kfyty.database.jdbc.sql;

import com.kfyty.database.jdbc.session.Configuration;
import com.kfyty.database.jdbc.sql.dialect.DialectProvider;
import com.kfyty.database.jdbc.sql.dialect.MySQLDialectProvider;
import com.kfyty.database.jdbc.sql.dynamic.DynamicProvider;
import com.kfyty.support.method.MethodParameter;
import com.kfyty.support.utils.CommonUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.kfyty.support.utils.ReflectUtil.getMethod;
import static com.kfyty.support.utils.ReflectUtil.invokeMethod;
import static com.kfyty.support.utils.ReflectUtil.invokeSimpleMethod;
import static com.kfyty.support.utils.ReflectUtil.newInstance;
import static java.util.Optional.ofNullable;

/**
 * 描述: SQL 提供者适配器
 *
 * @author kfyty725
 * @date 2021/6/8 10:49
 * @email kfyty725@hotmail.com
 */
public class ProviderAdapter {
    private static final String DEFAULT_PROVIDER_DIALECT = "mysql";

    private static final ThreadLocal<String> DIALECT_LOCAL = ThreadLocal.withInitial(() -> DEFAULT_PROVIDER_DIALECT);

    private static final Map<String, DialectProvider> DIALECT_MAP = new HashMap<>(4);

    private final Configuration configuration;

    static {
        addDialectProvider(DEFAULT_PROVIDER_DIALECT, new MySQLDialectProvider());
    }

    public ProviderAdapter(Configuration configuration) {
        this.configuration = Objects.requireNonNull(configuration);
    }

    public static void addDialectProvider(String dialect, DialectProvider provider) {
        DIALECT_MAP.put(dialect, provider);
    }

    public static void setDialect(String dialect) {
        if (!DIALECT_MAP.containsKey(dialect)) {
            throw new IllegalArgumentException("does not support this dialect: " + dialect);
        }
        ProviderAdapter.DIALECT_LOCAL.set(dialect);
    }

    public static DialectProvider getDialect() {
        String dialect = ProviderAdapter.DIALECT_LOCAL.get();
        if (CommonUtil.empty(dialect) || !DIALECT_MAP.containsKey(dialect)) {
            throw new IllegalArgumentException("current thread dialect not exists !");
        }
        return DIALECT_MAP.get(dialect);
    }

    /**
     * 提供 SQL
     *
     * @param providerClass SQL provider class
     * @param mapperClass   mapper class
     * @param sourceMethod  代理方法
     * @param annotation    注解
     * @param params        方法参数
     * @return SQL
     */
    public String doProvide(Class<?> providerClass, Class<?> mapperClass, Method sourceMethod, Annotation annotation, Map<String, MethodParameter> params) {
        if (DynamicProvider.class.isAssignableFrom(providerClass)) {
            return this.configuration.getDynamicProvider().doProvide(mapperClass, sourceMethod, annotation, params);
        }
        Object provider = DialectProvider.class.isAssignableFrom(providerClass) ? getDialect() : newInstance(providerClass);
        String methodName = ofNullable(invokeSimpleMethod(annotation, "method")).map(e -> (String) e).filter(CommonUtil::notEmpty).orElse(sourceMethod.getName());
        Method method = getMethod(provider.getClass(), methodName, Class.class, Method.class, annotation.annotationType(), Map.class);
        return (String) invokeMethod(provider, method, mapperClass, sourceMethod, annotation, params);
    }
}
