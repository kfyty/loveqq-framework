package com.kfyty.loveqq.framework.boot.tx.spring.autoconfig;

import com.kfyty.loveqq.framework.aop.Advisor;
import com.kfyty.loveqq.framework.aop.support.annotated.AnnotationPointcutAdvisor;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

/**
 * 描述: 数据源事务自动配置
 *
 * @author kfyty725
 * @date 2021/7/29 13:07
 * @email kfyty725@hotmail.com
 */
@Configuration
@ConditionalOnBean(DataSource.class)
public class DataSourceTransactionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DataSourceTransactionManager dataSourceTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public TransactionalInterceptorProxy transactionalInterceptorProxy(BeanFactory beanFactory) {
        return new TransactionalInterceptorProxy(beanFactory);
    }

    @Bean
    @ConditionalOnMissingBean(name = "transactionInterceptorAdvisor")
    public Advisor transactionInterceptorAdvisor(TransactionalInterceptorProxy transactionalInterceptorProxy) {
        return new AnnotationPointcutAdvisor(Transactional.class, transactionalInterceptorProxy);
    }
}
