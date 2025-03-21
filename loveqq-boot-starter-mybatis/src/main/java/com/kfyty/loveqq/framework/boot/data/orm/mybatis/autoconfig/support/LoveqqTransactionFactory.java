package com.kfyty.loveqq.framework.boot.data.orm.mybatis.autoconfig.support;

import com.kfyty.loveqq.framework.boot.data.orm.mybatis.autoconfig.adapter.MybatisTransactionAdapter;
import com.kfyty.loveqq.framework.boot.tx.spring.autoconfig.transaction.ManagedJdbcTransaction;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 描述: LoveqqTransactionFactory
 *
 * @author kfyty725
 * @date 2024/6/03 18:55
 * @email kfyty725@hotmail.com
 */
public class LoveqqTransactionFactory implements TransactionFactory {

    @Override
    public Transaction newTransaction(Connection conn) {
        throw new UnsupportedOperationException("New transactions require a DataSource");
    }

    @Override
    public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
        return new MybatisTransactionAdapter(new ManagedJdbcTransaction(dataSource));
    }
}
