package com.kfyty.loveqq.framework.boot.data.orm.mybatis.autoconfig;

import com.kfyty.loveqq.framework.boot.data.orm.mybatis.autoconfig.support.ConcurrentSqlSession;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBean;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import org.apache.ibatis.session.Configuration;

/**
 * 描述: 导入 Mapper 注解的接口 bean 定义
 *
 * @author kfyty725
 * @date 2021/6/12 12:57
 * @email kfyty725@hotmail.com
 */
public class MapperInterfaceFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware {
    /**
     * mapper 接口
     */
    protected final Class<T> mapperInterface;

    /**
     * {@link ConcurrentSqlSession}
     * 由于 {@link SqlSessionFactoryBean} 作为条件可能还未解析，因此需要延迟获取 SqlSession
     */
    protected Lazy<ConcurrentSqlSession> sqlSession;

    /**
     * {@link ApplicationContext}
     */
    protected ApplicationContext applicationContext;

    public MapperInterfaceFactoryBean(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public MapperInterfaceFactoryBean(Class<T> mapperInterface, ConcurrentSqlSession sqlSession) {
        this(mapperInterface, new Lazy<>(() -> sqlSession));
    }

    public MapperInterfaceFactoryBean(Class<T> mapperInterface, Lazy<ConcurrentSqlSession> sqlSession) {
        this.mapperInterface = mapperInterface;
        this.sqlSession = sqlSession;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Class<?> getBeanType() {
        return this.mapperInterface;
    }

    @Override
    public T getObject() {
        this.ensureSqlSession();
        this.afterPropertiesSet();
        return this.sqlSession.get().getMapper(this.mapperInterface);
    }

    public void afterPropertiesSet() {
        Configuration configuration = this.sqlSession.get().getConfiguration();
        if (!configuration.hasMapper(this.mapperInterface)) {
            configuration.addMapper(this.mapperInterface);
        }
    }

    protected void ensureSqlSession() {
        if (this.sqlSession == null) {
            this.sqlSession = new Lazy<>(() -> this.applicationContext.getBean(ConcurrentSqlSession.class));
        }
    }
}
