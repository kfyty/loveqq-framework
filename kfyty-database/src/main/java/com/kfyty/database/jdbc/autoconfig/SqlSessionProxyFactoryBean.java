package com.kfyty.database.jdbc.autoconfig;

import com.kfyty.core.jdbc.transaction.Transaction;
import com.kfyty.database.jdbc.intercept.Interceptor;
import com.kfyty.database.jdbc.session.Configuration;
import com.kfyty.database.jdbc.session.SqlSessionProxyFactory;
import com.kfyty.database.jdbc.sql.dynamic.DynamicProvider;
import com.kfyty.core.autoconfig.InitializingBean;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.beans.FactoryBean;
import com.kfyty.core.utils.BeanUtil;
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

    @Autowired(required = false)
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
                .setDataSource(this.getDataSource())
                .setInterceptors(this.getInterceptors());
        if (this.transactionFactory != null) {
            configuration.setTransactionFactory(this.getTransactionFactory());
        }
        if (this.dynamicProvider != null) {
            this.dynamicProvider.setConfiguration(configuration.setDynamicProvider(dynamicProvider));
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
