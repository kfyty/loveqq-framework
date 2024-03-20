package com.kfyty.database.jdbc.autoconfig;

import com.jfinal.template.Engine;
import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.ImportBeanDefinition;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.core.jdbc.transaction.Transaction;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.database.jdbc.intercept.internal.ForEachInternalInterceptor;
import com.kfyty.database.jdbc.intercept.internal.SubQueryInternalInterceptor;
import com.kfyty.database.jdbc.sql.dynamic.DynamicProvider;
import com.kfyty.database.jdbc.sql.dynamic.enjoy.EnjoyDynamicProvider;
import com.kfyty.database.jdbc.transaction.ManagedJdbcTransaction;

import javax.sql.DataSource;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.kfyty.core.autoconfig.beans.builder.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * 描述: 自动配置 Mapper 注解
 *
 * @author kfyty725
 * @date 2021/5/22 13:13
 * @email kfyty725@hotmail.com
 */
@Configuration
public class MapperAutoConfig implements ImportBeanDefinition {

    @Bean
    @ConditionalOnMissingBean
    public DynamicProvider<?> dynamicProvider() {
        return new EnjoyDynamicProvider().setEngine(Engine.createIfAbsent("dynamicProvider", e -> {}));
    }

    @Bean
    @ConditionalOnMissingBean(name = "transactionFactory")
    public Supplier<Transaction> transactionFactory(DataSource dataSource) {
        return () -> new ManagedJdbcTransaction(dataSource);
    }

    @Bean
    public ForEachInternalInterceptor forEachInternalInterceptor() {
        return new ForEachInternalInterceptor();
    }

    @Bean
    public SubQueryInternalInterceptor subQueryInternalInterceptor() {
        return new SubQueryInternalInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlSessionProxyFactoryBean sqlSessionProxyFactory() {
        return new SqlSessionProxyFactoryBean();
    }

    @Override
    public Predicate<Class<?>> classesFilter(ApplicationContext applicationContext) {
        return e -> AnnotationUtil.hasAnnotation(e, Mapper.class);
    }

    @Override
    public BeanDefinition buildBeanDefinition(ApplicationContext applicationContext, Class<?> clazz) {
        return genericBeanDefinition(MapperInterfaceFactoryBean.class).addConstructorArgs(Class.class, clazz).getBeanDefinition();
    }
}
