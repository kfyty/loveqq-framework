package com.kfyty.boot.orm.mybatis.autoconfig;

import com.kfyty.boot.orm.mybatis.autoconfig.support.ConcurrentSqlSession;
import com.kfyty.core.autoconfig.InitializingBean;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.beans.FactoryBean;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.Configuration;

/**
 * 描述: 导入 Mapper 注解的接口 bean 定义
 *
 * @author kfyty725
 * @date 2021/6/12 12:57
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class MapperInterfaceFactoryBean<T> implements FactoryBean<T>, InitializingBean {
    /**
     * mapper 接口
     */
    private final Class<T> mapperInterface;

    @Autowired
    private ConcurrentSqlSession sqlSession;

    @Override
    public Class<?> getBeanType() {
        return this.mapperInterface;
    }

    @Override
    public T getObject() {
        return this.sqlSession.getMapper(this.mapperInterface);
    }

    @Override
    public void afterPropertiesSet() {
        Configuration configuration = this.sqlSession.getConfiguration();
        if (!configuration.hasMapper(this.mapperInterface)) {
            configuration.addMapper(this.mapperInterface);
        }
    }
}
