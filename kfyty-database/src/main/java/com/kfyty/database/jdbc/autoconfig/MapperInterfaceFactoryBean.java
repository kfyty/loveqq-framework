package com.kfyty.database.jdbc.autoconfig;

import com.kfyty.database.jdbc.session.SqlSessionProxyFactory;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.beans.FactoryBean;

/**
 * 描述: 导入 Mapper 注解的接口 bean 定义
 *
 * @author kfyty725
 * @date 2021/6/12 12:57
 * @email kfyty725@hotmail.com
 */
public class MapperInterfaceFactoryBean<T> implements FactoryBean<T> {
    @Autowired
    private SqlSessionProxyFactory sqlSessionProxyFactory;

    private final Class<?> mapperInterface;

    public MapperInterfaceFactoryBean(Class<?> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public void setSqlSessionProxyFactory(SqlSessionProxyFactory sqlSessionProxyFactory) {
        this.sqlSessionProxyFactory = sqlSessionProxyFactory;
    }

    @Override
    public Class<?> getBeanType() {
        return this.mapperInterface;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() {
        return (T) this.sqlSessionProxyFactory.createProxy(this.mapperInterface);
    }
}
