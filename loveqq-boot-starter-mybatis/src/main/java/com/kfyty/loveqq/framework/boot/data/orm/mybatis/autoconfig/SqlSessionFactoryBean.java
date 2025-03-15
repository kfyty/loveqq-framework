package com.kfyty.loveqq.framework.boot.data.orm.mybatis.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBean;
import com.kfyty.loveqq.framework.core.event.ApplicationListener;
import com.kfyty.loveqq.framework.core.event.ContextRefreshedEvent;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.support.io.PathMatchingResourcePatternResolver;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.core.utils.PackageUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import lombok.Data;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.type.TypeHandler;

import javax.sql.DataSource;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 描述: {@link org.apache.ibatis.session.SqlSessionFactory} 配置
 *
 * @author kfyty725
 * @date 2024/6/03 18:55
 * @email kfyty725@hotmail.com
 */
@Data
public class SqlSessionFactoryBean implements FactoryBean<SqlSessionFactory>, ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private MybatisProperties mybatisProperties;

    @Autowired
    private PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TransactionFactory transactionFactory;

    @Autowired(required = false)
    private DatabaseIdProvider databaseIdProvider;

    @Autowired(required = false)
    private Cache cache;

    @Autowired(required = false)
    private List<Interceptor> plugins;

    @Autowired(required = false)
    private List<TypeHandler<?>> typeHandlers;

    @Autowired(required = false)
    private ObjectFactory objectFactory;

    @Autowired(required = false)
    private ObjectWrapperFactory objectWrapperFactory;

    @Autowired(required = false)
    private Configuration configuration;

    /**
     * {@link SqlSessionFactory}
     */
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public Class<?> getBeanType() {
        return this.sqlSessionFactory == null ? SqlSessionFactory.class : this.sqlSessionFactory.getClass();
    }

    @Override
    public SqlSessionFactory getObject() {
        if (this.sqlSessionFactory == null) {
            this.sqlSessionFactory = this.buildSqlSessionFactory();
        }
        return this.sqlSessionFactory;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.sqlSessionFactory.getConfiguration().getMappedStatementNames();
    }

    protected Class<? extends Configuration> obtainConfigurationClass() {
        return Configuration.class;
    }

    protected Pair<Configuration, XMLConfigBuilder> buildConfiguration() {
        // 构建配置
        Configuration configuration;
        XMLConfigBuilder xmlConfigBuilder = null;

        // 已存在配置
        if (this.configuration != null) {
            configuration = this.configuration;
            if (configuration.getVariables() == null) {
                configuration.setVariables(this.mybatisProperties.getConfigurationProperties());
            } else if (this.mybatisProperties.getConfigurationProperties() != null) {
                configuration.getVariables().putAll(this.mybatisProperties.getConfigurationProperties());
            }
        }
        // 存在 mybatis 配置文件
        else if (this.mybatisProperties.getConfigLocation() != null) {
            xmlConfigBuilder = new XMLConfigBuilder(this.obtainConfigurationClass(), IOUtil.load(this.mybatisProperties.getConfigLocation()), null, this.mybatisProperties.getConfigurationProperties());
            configuration = xmlConfigBuilder.getConfiguration();
        }
        // 默认配置
        else {
            configuration = ReflectUtil.newInstance(this.obtainConfigurationClass());
            Optional.ofNullable(this.mybatisProperties.getConfigurationProperties()).ifPresent(configuration::setVariables);
        }

        return new Pair<>(configuration, xmlConfigBuilder);
    }

    protected void buildEnvironment(Configuration configuration) {
        configuration.setEnvironment(new Environment(SqlSessionFactoryBean.class.getSimpleName(), this.transactionFactory, this.dataSource));
    }

    protected void buildMapperLocations() {
        if (CommonUtil.notEmpty(this.mybatisProperties.getMapperLocations())) {
            for (String mapperLocation : this.mybatisProperties.getMapperLocations()) {
                Set<URL> resources = this.pathMatchingResourcePatternResolver.findResources(mapperLocation);
                for (URL resource : resources) {
                    XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(IOUtil.newInputStream(resource), configuration, resource.getFile(), configuration.getSqlFragments());
                    xmlMapperBuilder.parse();
                }
            }
        }
    }

    protected SqlSessionFactory build(Configuration configuration) {
        return new SqlSessionFactoryBuilder().build(configuration);
    }

    protected SqlSessionFactory buildSqlSessionFactory() {
        // 构建配置
        Pair<Configuration, XMLConfigBuilder> configPair = this.buildConfiguration();
        Configuration configuration = configPair.getKey();
        XMLConfigBuilder xmlConfigBuilder = configPair.getValue();

        Optional.ofNullable(this.cache).ifPresent(configuration::addCache);
        Optional.ofNullable(this.plugins).ifPresent(p -> p.forEach(configuration::addInterceptor));
        Optional.ofNullable(this.typeHandlers).ifPresent(t -> t.forEach(configuration.getTypeHandlerRegistry()::register));
        Optional.ofNullable(this.objectFactory).ifPresent(configuration::setObjectFactory);
        Optional.ofNullable(this.objectWrapperFactory).ifPresent(configuration::setObjectWrapperFactory);
        Optional.ofNullable(this.mybatisProperties.getVfs()).ifPresent(configuration::setVfsImpl);
        Optional.ofNullable(this.mybatisProperties.getDefaultScriptingLanguageDriver()).ifPresent(configuration::setDefaultScriptingLanguage);

        if (this.databaseIdProvider != null) {
            try {
                configuration.setDatabaseId(this.databaseIdProvider.getDatabaseId(this.dataSource));
            } catch (SQLException e) {
                throw new ResolvableException("Failed getting a databaseId", e);
            }
        }

        if (CommonUtil.notEmpty(this.mybatisProperties.getTypeAliasesPackage())) {
            CommonUtil.split(this.mybatisProperties.getTypeAliasesPackage(), ",")
                    .stream()
                    .flatMap(p -> PackageUtil.scanClass(p, this.pathMatchingResourcePatternResolver).stream())
                    .filter(e -> !ReflectUtil.isAbstract(e))
                    .forEach(configuration.getTypeAliasRegistry()::registerAlias);
        }

        if (CommonUtil.notEmpty(this.mybatisProperties.getTypeHandlersPackage())) {
            CommonUtil.split(this.mybatisProperties.getTypeHandlersPackage(), ",")
                    .stream()
                    .flatMap(p -> PackageUtil.scanClass(p, this.pathMatchingResourcePatternResolver).stream())
                    .filter(e -> !ReflectUtil.isAbstract(e))
                    .forEach(configuration.getTypeHandlerRegistry()::register);
        }

        if (xmlConfigBuilder != null) {
            xmlConfigBuilder.parse();
        }

        this.buildEnvironment(configuration);

        this.buildMapperLocations();

        return this.build(configuration);
    }
}
