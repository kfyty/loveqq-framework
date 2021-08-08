package com.kfyty.database.jdbc.session;

import com.kfyty.support.transaction.Transaction;
import lombok.Getter;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * 描述: SqlSessionProxyFactory
 *
 * @author kfyty725
 * @date 2021/5/19 9:40
 * @email kfyty725@hotmail.com
 */
public class SqlSessionProxyFactory {
    @Getter
    private final Configuration configuration;

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

    /**
     * 创建接口代理对象
     * 根据数据源和接口的快捷创建方法
     *
     * @param interfaces 接口 Class 对象
     * @param <T>        泛型
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(DataSource dataSource, Class<T> interfaces) {
        Configuration configuration = new Configuration().setDataSource(dataSource);
        return (T) Proxy.newProxyInstance(interfaces.getClassLoader(), new Class[]{interfaces}, new SqlSession(interfaces, configuration));
    }

    /**
     * 获取代理对象的事务
     *
     * @param proxy 代理对象
     * @return 事务
     */
    public static Transaction getTransaction(Object proxy) {
        return getSqlSession(proxy).getTransaction();
    }

    /**
     * 获取代理对象的 SqlSession
     *
     * @param proxy 代理对象
     * @return SqlSession
     */
    private static SqlSession getSqlSession(Object proxy) {
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(proxy);
        if (invocationHandler instanceof SqlSession) {
            return (SqlSession) invocationHandler;
        }
        throw new IllegalArgumentException("the proxy object is not a proxy of SqlSession !");
    }
}
