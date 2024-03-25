package com.kfyty.database.generator.config;

import com.kfyty.core.autoconfig.beans.FactoryBean;
import com.kfyty.database.generator.mapper.DatabaseMapper;
import lombok.RequiredArgsConstructor;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/12 12:41
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class DatabaseMapperFactory implements FactoryBean<Class<DatabaseMapper>> {
    private final Class<DatabaseMapper> databaseClass;

    @Override
    public Class<?> getBeanType() {
        return this.databaseClass;
    }

    @Override
    public Class<DatabaseMapper> getObject() {
        return this.databaseClass;
    }
}
