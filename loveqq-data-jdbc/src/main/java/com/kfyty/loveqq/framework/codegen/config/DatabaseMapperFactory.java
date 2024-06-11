package com.kfyty.loveqq.framework.codegen.config;

import com.kfyty.loveqq.framework.codegen.mapper.DatabaseMapper;
import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBean;
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
