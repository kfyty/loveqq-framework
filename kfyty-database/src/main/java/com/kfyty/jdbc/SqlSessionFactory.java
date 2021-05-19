package com.kfyty.jdbc;

import com.kfyty.support.transaction.Transaction;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/5/19 9:40
 * @email kfyty725@hotmail.com
 */
public class SqlSessionFactory {
    /**
     * 创建接口代理对象，一般为内部使用，数据源后续自动注入
     * @param interfaces    接口 Class 对象
     * @param <T>           泛型
     * @return              代理对象
     */
    @SuppressWarnings("unchecked")
    public static  <T> T createProxy(Class<T> interfaces) {
        return (T) Proxy.newProxyInstance(SqlSessionFactory.class.getClassLoader(), new Class[] {interfaces}, new SqlSession());
    }

    /**
     * 创建接口代理对象
     * @param interfaces    接口 Class 对象
     * @param <T>           泛型
     * @return              代理对象
     */
    @SuppressWarnings("unchecked")
    public static  <T> T createProxy(DataSource dataSource, Class<T> interfaces) {
        return (T) Proxy.newProxyInstance(SqlSessionFactory.class.getClassLoader(), new Class[] {interfaces}, new SqlSession(dataSource));
    }

    /**
     * 获取代理对象的 SqlSession
     * @param proxy    代理对象
     * @return         事务
     */
    public static SqlSession getSqlSession(Object proxy) {
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(proxy);
        if(invocationHandler instanceof SqlSession) {
            return (SqlSession) invocationHandler;
        }
        throw new IllegalArgumentException("the proxy object is not a proxy of SqlSession !");
    }

    /**
     * 获取代理对象的事务
     * @param proxy    代理对象
     * @return         事务
     */
    public static Transaction getTransaction(Object proxy) {
        return getSqlSession(proxy).getTransaction();
    }
}
