package com.kfyty.database.jdbc.autoconfig;

import com.kfyty.database.jdbc.SqlSessionFactory;
import com.kfyty.support.autoconfig.BeanDefine;
import com.kfyty.support.autoconfig.ImportBeanDefine;
import com.kfyty.support.autoconfig.InstantiateBean;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 描述: 自动配置 Mapper 注解
 *
 * @author kfyty725
 * @date 2021/5/22 13:13
 * @email kfyty725@hotmail.com
 */
@Configuration
public class MapperAutoConfig implements ImportBeanDefine, InstantiateBean {
    @Autowired("mapperDataSource")
    private DataSource dataSource;

    @Override
    public Set<BeanDefine> doImport(Set<Class<?>> scanClasses) {
        return scanClasses.stream().filter(e -> e.isAnnotationPresent(Mapper.class)).map(BeanDefine::new).collect(Collectors.toSet());
    }

    @Override
    public boolean canInstantiate(BeanDefine beanDefine) {
        return beanDefine.getBeanType().isAnnotationPresent(Mapper.class);
    }

    @Override
    public Object doInstantiate(BeanDefine beanDefine) {
        return SqlSessionFactory.createProxy(this.dataSource, beanDefine.getBeanType());
    }
}
