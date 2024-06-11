package com.kfyty.loveqq.framework.data.jdbc.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.InitializingBean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBean;
import com.kfyty.loveqq.framework.core.jdbc.transaction.Transaction;
import com.kfyty.loveqq.framework.core.support.io.PathMatchingResourcePatternResolver;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;
import com.kfyty.loveqq.framework.data.jdbc.intercept.Interceptor;
import com.kfyty.loveqq.framework.data.jdbc.session.Configuration;
import com.kfyty.loveqq.framework.data.jdbc.session.SqlSessionProxyFactory;
import com.kfyty.loveqq.framework.data.jdbc.sql.dynamic.DynamicProvider;
import lombok.Data;

import javax.sql.DataSource;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * 描述: SqlSessionProxyFactoryBean
 *
 * @author kfyty725
 * @date 2021/8/8 11:07
 * @email kfyty725@hotmail.com
 */
@Data
public class SqlSessionProxyFactoryBean implements FactoryBean<SqlSessionProxyFactory>, InitializingBean {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver;

    @Autowired(value = "transactionFactory", required = false)
    private Supplier<Transaction> transactionFactory;

    @Autowired(required = false)
    private DynamicProvider<?> dynamicProvider;

    @Autowired(required = false)
    private List<Interceptor> interceptors;

    @Override
    public Class<?> getBeanType() {
        return SqlSessionProxyFactory.class;
    }

    @Override
    public SqlSessionProxyFactory getObject() {
        Configuration configuration = new Configuration()
                .setDataSource(this.dataSource)
                .setPathMatchingResourcePatternResolver(this.pathMatchingResourcePatternResolver)
                .setInterceptors(this.interceptors);
        if (this.transactionFactory != null) {
            configuration.setTransactionFactory(this.transactionFactory);
        }
        if (this.dynamicProvider != null) {
            configuration.setDynamicProvider(this.dynamicProvider);
        }
        return new SqlSessionProxyFactory(configuration);
    }

    @Override
    public void afterPropertiesSet() {
        if (this.interceptors != null) {
            interceptors.sort(Comparator.comparing(BeanUtil::getBeanOrder));
        }
    }
}
