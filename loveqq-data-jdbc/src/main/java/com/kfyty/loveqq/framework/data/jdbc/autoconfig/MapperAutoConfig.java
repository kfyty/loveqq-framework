package com.kfyty.loveqq.framework.data.jdbc.autoconfig;

import com.jfinal.template.Engine;
import com.kfyty.loveqq.framework.boot.tx.spring.autoconfig.transaction.ManagedJdbcTransaction;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ImportBeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.jdbc.transaction.Transaction;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.data.jdbc.intercept.internal.IfInternalInterceptor;
import com.kfyty.loveqq.framework.data.jdbc.intercept.internal.SubQueryInternalInterceptor;
import com.kfyty.loveqq.framework.data.jdbc.sql.dynamic.DynamicProvider;
import com.kfyty.loveqq.framework.data.jdbc.sql.dynamic.enjoy.EnjoyDynamicProvider;

import javax.sql.DataSource;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.kfyty.loveqq.framework.core.autoconfig.beans.builder.BeanDefinitionBuilder.genericBeanDefinition;

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
    public IfInternalInterceptor ifInternalInterceptor() {
        return new IfInternalInterceptor();
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
