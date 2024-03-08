package com.kfyty.database.generator.config;

import com.kfyty.core.autoconfig.beans.FactoryBean;
import com.kfyty.database.generator.mapper.AbstractDatabaseMapper;
import lombok.RequiredArgsConstructor;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/12 12:41
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class DatabaseMapperFactory implements FactoryBean<Class<AbstractDatabaseMapper>> {
    private final Class<AbstractDatabaseMapper> databaseClass;

    @Override
    public Class<?> getBeanType() {
        return this.databaseClass;
    }

    @Override
    public Class<AbstractDatabaseMapper> getObject() {
        return this.databaseClass;
    }
}
