package com.kfyty.database.generator.config;

import com.kfyty.database.generator.mapper.AbstractDatabaseMapper;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.beans.FactoryBean;
import lombok.NoArgsConstructor;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/12 12:41
 * @email kfyty725@hotmail.com
 */
@NoArgsConstructor
public class DatabaseMapperFactory implements FactoryBean<Class<AbstractDatabaseMapper>> {
    private Class<AbstractDatabaseMapper> databaseClass;

    @Autowired
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
