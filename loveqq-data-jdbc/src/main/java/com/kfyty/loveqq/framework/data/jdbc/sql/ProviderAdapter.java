package com.kfyty.loveqq.framework.data.jdbc.sql;

import com.kfyty.loveqq.framework.core.lang.Value;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.data.jdbc.session.Configuration;
import com.kfyty.loveqq.framework.data.jdbc.sql.dialect.DialectProvider;
import com.kfyty.loveqq.framework.data.jdbc.sql.dialect.MySQLDialectProvider;
import com.kfyty.loveqq.framework.data.jdbc.sql.dynamic.DynamicProvider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.newInstance;

/**
 * 描述: SQL 提供者适配器
 *
 * @author kfyty725
 * @date 2021/6/8 10:49
 * @email kfyty725@hotmail.com
 */
public class ProviderAdapter {
    /**
     * 默认方言
     */
    private static final String DEFAULT_PROVIDER_DIALECT = "mysql";

    /**
     * 当前线程方言
     */
    private static final ThreadLocal<String> DIALECT_LOCAL = ThreadLocal.withInitial(() -> DEFAULT_PROVIDER_DIALECT);

    /**
     * 方言缓存
     */
    private static final Map<String, DialectProvider> DIALECT_MAP = new HashMap<>(4);

    /**
     * 配置
     */
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
    @SuppressWarnings({"rawtypes", "unchecked"})
    public String doProvide(Class<?> providerClass, Class<?> mapperClass, Method sourceMethod, Value<Annotation> annotation, Map<String, MethodParameter> params) {
        if (DynamicProvider.class.isAssignableFrom(providerClass)) {
            return this.configuration.getDynamicProvider().doProvide(mapperClass, sourceMethod, annotation, params);
        }
        if (DialectProvider.class.isAssignableFrom(providerClass)) {
            return getDialect().doProvide(mapperClass, sourceMethod, annotation, params);
        }
        return ((Provider) newInstance(providerClass)).doProvide(mapperClass, sourceMethod, annotation, params);
    }
}
