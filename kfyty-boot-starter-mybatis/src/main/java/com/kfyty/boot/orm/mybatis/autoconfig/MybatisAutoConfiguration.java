package com.kfyty.boot.orm.mybatis.autoconfig;

import com.kfyty.boot.orm.mybatis.autoconfig.support.ConcurrentSqlSession;
import com.kfyty.boot.orm.mybatis.autoconfig.support.IocTransactionFactory;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;

/**
 * 描述: mybatis 配置属性
 *
 * @author kfyty725
 * @date 2024/6/03 18:55
 * @email kfyty725@hotmail.com
 */
@Configuration
public class MybatisAutoConfiguration {

    @Bean
    public TransactionFactory mytbatisTransactionFactory() {
        return new IocTransactionFactory();
    }

    @Bean
    @ConditionalOnMissingBean(SqlSessionFactory.class)
    public SqlSessionFactoryBean sqlSessionFactoryBean() {
        return new SqlSessionFactoryBean();
    }

    @Bean
    public ConcurrentSqlSession sqlSession(SqlSessionFactory sqlSessionFactory) {
        return new ConcurrentSqlSession(sqlSessionFactory);
    }

    @Bean
    public MapperScanner mapperScanner() {
        return new MapperScanner();
    }
}
