package com.kfyty.database.jdbc.session;

import lombok.Getter;

import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * 描述: SqlSessionProxyFactory
 *
 * @author kfyty725
 * @date 2021/5/19 9:40
 * @email kfyty725@hotmail.com
 */
@Getter
public class SqlSessionProxyFactory {
    protected final Configuration configuration;

    public SqlSessionProxyFactory(Configuration configuration) {
        this.configuration = Objects.requireNonNull(configuration);
    }

    /**
     * 创建接口代理对象
     *
     * @param interfaces 接口 Class 对象
     * @param <T>        泛型
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> interfaces) {
        return (T) Proxy.newProxyInstance(interfaces.getClassLoader(), new Class[]{interfaces}, new SqlSession(interfaces, this.configuration));
    }
}
