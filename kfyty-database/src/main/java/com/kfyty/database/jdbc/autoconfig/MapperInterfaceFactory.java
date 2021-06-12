package com.kfyty.database.jdbc.autoconfig;

import com.kfyty.database.jdbc.SqlSessionFactory;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.beans.FactoryBean;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/12 12:57
 * @email kfyty725@hotmail.com
 */
@NoArgsConstructor
public class MapperInterfaceFactory implements FactoryBean<Object> {
    @Autowired("mapperDataSource")
    private DataSource dataSource;

    private Class<?> mapperInterface;

    public MapperInterfaceFactory(Class<?> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @Override
    public Class<?> getBeanType() {
        return this.mapperInterface;
    }

    @Override
    public Object getObject() {
        return SqlSessionFactory.createProxy(this.dataSource, this.mapperInterface);
    }
}
