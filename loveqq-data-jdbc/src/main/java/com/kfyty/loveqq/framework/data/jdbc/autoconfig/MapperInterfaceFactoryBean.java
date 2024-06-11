package com.kfyty.loveqq.framework.data.jdbc.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBean;
import com.kfyty.loveqq.framework.data.jdbc.session.SqlSessionProxyFactory;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * 描述: 导入 Mapper 注解的接口 bean 定义
 *
 * @author kfyty725
 * @date 2021/6/12 12:57
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class MapperInterfaceFactoryBean<T> implements FactoryBean<T> {
    @Setter
    @Autowired
    private SqlSessionProxyFactory sqlSessionProxyFactory;

    private final Class<?> mapperInterface;

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
