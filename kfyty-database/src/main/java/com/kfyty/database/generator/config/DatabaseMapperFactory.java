package com.kfyty.database.generator.config;

import com.kfyty.database.generator.mapper.AbstractDatabaseMapper;
import com.kfyty.support.autoconfig.beans.FactoryBean;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/12 12:41
 * @email kfyty725@hotmail.com
 */
public class DatabaseMapperFactory implements FactoryBean<Class<AbstractDatabaseMapper>> {
    private final Class<AbstractDatabaseMapper> databaseClass;

    public DatabaseMapperFactory(Class<AbstractDatabaseMapper> databaseClass) {
        this.databaseClass = databaseClass;
    }

    @Override
    public Class<?> getBeanType() {
        return this.databaseClass;
    }

    @Override
    public Class<AbstractDatabaseMapper> getObject() {
        return this.databaseClass;
    }
}
