package com.kfyty.loveqq.framework.data.korm.autoconfig;

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
import com.kfyty.loveqq.framework.data.korm.intercept.internal.IfInternalInterceptor;
import com.kfyty.loveqq.framework.data.korm.intercept.internal.SubQueryInternalInterceptor;
import com.kfyty.loveqq.framework.data.korm.sql.dynamic.DynamicProvider;
import com.kfyty.loveqq.framework.data.korm.sql.dynamic.enjoy.EnjoyDynamicProvider;

import javax.sql.DataSource;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.kfyty.loveqq.framework.core.autoconfig.beans.builder.BeanDefinitionBuilder.genericBeanDefinition;
import static com.kfyty.loveqq.framework.data.korm.autoconfig.SqlSessionProxyFactoryBean.TRANSACTION_FACTORY_BEAN_NAME;

/**
 * 描述: 自动配置 Mapper 注解
 *
 * @author kfyty725
 * @date 2021/5/22 13:13
 * @email kfyty725@hotmail.com
 */
@Configuration
public class KormAutoConfig implements ImportBeanDefinition {

    @ConditionalOnMissingBean
    @Bean(resolveNested = false, independent = true)
    public DynamicProvider<?> dynamicProvider() {
        return new EnjoyDynamicProvider().setEngine(Engine.createIfAbsent("dynamicProvider", e -> {}));
    }

    @Bean(value = TRANSACTION_FACTORY_BEAN_NAME, resolveNested = false, independent = true)
    @ConditionalOnMissingBean(name = TRANSACTION_FACTORY_BEAN_NAME)
    public Supplier<Transaction> jdbcTransactionFactory(DataSource dataSource) {
        return () -> new ManagedJdbcTransaction(dataSource);
    }

    @Bean(resolveNested = false, independent = true)
    public IfInternalInterceptor ifInternalInterceptor() {
        return new IfInternalInterceptor();
    }

    @Bean(resolveNested = false, independent = true)
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
