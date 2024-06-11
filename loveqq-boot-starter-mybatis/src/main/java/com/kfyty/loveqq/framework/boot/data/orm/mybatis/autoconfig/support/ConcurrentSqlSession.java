package com.kfyty.loveqq.framework.boot.data.orm.mybatis.autoconfig.support;

import com.kfyty.loveqq.framework.core.utils.ExceptionUtil;
import com.kfyty.loveqq.framework.core.utils.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * 描述: 线程安全的 {@link org.apache.ibatis.session.SqlSession}
 *
 * @author kfyty725
 * @date 2024/6/03 18:55
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class ConcurrentSqlSession implements SqlSession {
    private final SqlSessionFactory sqlSessionFactory;

    private final ExecutorType executorType;

    private final SqlSession sqlSessionProxy;

    public ConcurrentSqlSession(SqlSessionFactory sqlSessionFactory) {
        this(sqlSessionFactory, sqlSessionFactory.getConfiguration().getDefaultExecutorType());
    }

    public ConcurrentSqlSession(SqlSessionFactory sqlSessionFactory, ExecutorType executorType) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.executorType = executorType;
        this.sqlSessionProxy = (SqlSession) Proxy.newProxyInstance(ConcurrentSqlSession.class.getClassLoader(), new Class[]{SqlSession.class}, new SqlSessionProxy());
    }

    @Override
    public <T> T selectOne(String statement) {
        return this.sqlSessionProxy.selectOne(statement);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        return this.sqlSessionProxy.selectOne(statement, parameter);
    }

    @Override
    public <E> List<E> selectList(String statement) {
        return this.sqlSessionProxy.selectList(statement);
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        return this.sqlSessionProxy.selectList(statement, parameter);
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        return this.sqlSessionProxy.selectList(statement, parameter, rowBounds);
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
        return this.sqlSessionProxy.selectMap(statement, mapKey);
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
        return this.sqlSessionProxy.selectMap(statement, parameter, mapKey);
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
        return this.sqlSessionProxy.selectMap(statement, parameter, mapKey, rowBounds);
    }

    @Override
    public <T> Cursor<T> selectCursor(String statement) {
        return this.sqlSessionProxy.selectCursor(statement);
    }

    @Override
    public <T> Cursor<T> selectCursor(String statement, Object parameter) {
        return this.sqlSessionProxy.selectCursor(statement, parameter);
    }

    @Override
    public <T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds) {
        return this.sqlSessionProxy.selectCursor(statement, parameter, rowBounds);
    }

    @Override
    public void select(String statement, Object parameter, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, parameter, handler);
    }

    @Override
    public void select(String statement, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, handler);
    }

    @Override
    public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
        this.sqlSessionProxy.select(statement, parameter, rowBounds, handler);
    }

    @Override
    public int insert(String statement) {
        return this.sqlSessionProxy.insert(statement);
    }

    @Override
    public int insert(String statement, Object parameter) {
        return this.sqlSessionProxy.insert(statement, parameter);
    }

    @Override
    public int update(String statement) {
        return this.sqlSessionProxy.update(statement);
    }

    @Override
    public int update(String statement, Object parameter) {
        return this.sqlSessionProxy.update(statement, parameter);
    }

    @Override
    public int delete(String statement) {
        return this.sqlSessionProxy.delete(statement);
    }

    @Override
    public int delete(String statement, Object parameter) {
        return this.sqlSessionProxy.delete(statement, parameter);
    }

    @Override
    public void commit() {
        throw new UnsupportedOperationException("Manual commit is not allowed over a ioc managed SqlSession");
    }

    @Override
    public void commit(boolean force) {
        throw new UnsupportedOperationException("Manual commit is not allowed over a ioc managed SqlSession");
    }

    @Override
    public void rollback() {
        throw new UnsupportedOperationException("Manual rollback is not allowed over a ioc managed SqlSession");
    }

    @Override
    public void rollback(boolean force) {
        throw new UnsupportedOperationException("Manual rollback is not allowed over a ioc managed SqlSession");
    }

    @Override
    public List<BatchResult> flushStatements() {
        return this.sqlSessionProxy.flushStatements();
    }

    @Override
    public void clearCache() {
        this.sqlSessionProxy.clearCache();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Manual close is not allowed over a ioc managed SqlSession");
    }

    @Override
    public Configuration getConfiguration() {
        return this.sqlSessionFactory.getConfiguration();
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return this.getConfiguration().getMapper(type, this);
    }

    @Override
    public Connection getConnection() {
        return this.sqlSessionProxy.getConnection();
    }

    private class SqlSessionProxy implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            SqlSession sqlSession = getSqlSession(sqlSessionFactory, executorType);
            try {
                Object result = method.invoke(sqlSession, args);
                if (!isSqlSessionTransactional(sqlSession, sqlSessionFactory)) {
                    sqlSession.commit(true);
                }
                return result;
            } catch (Throwable e) {
                throw ExceptionUtil.wrap(e);
            } finally {
                if (sqlSession != null) {
                    closeSqlSession(sqlSession, sqlSessionFactory);
                }
            }
        }
    }

    public static SqlSession getSqlSession(SqlSessionFactory sessionFactory, ExecutorType executorType) {
        SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
        SqlSession session = sessionHolder(executorType, holder);
        if (session != null) {
            return session;
        }

        LogUtil.logIfWarnEnabled(log, log -> log.debug("Creating a new SqlSession"));
        session = sessionFactory.openSession(executorType);
        registerSessionHolder(sessionFactory, executorType, session);
        return session;
    }

    public static boolean isSqlSessionTransactional(SqlSession session, SqlSessionFactory sessionFactory) {
        SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
        return (holder != null) && (holder.getSqlSession() == session);
    }

    private static SqlSession sessionHolder(ExecutorType executorType, SqlSessionHolder holder) {
        SqlSession session = null;
        if (holder != null && holder.isSynchronizedWithTransaction()) {
            if (holder.getExecutorType() != executorType) {
                throw new TransientDataAccessResourceException("Cannot change the ExecutorType when there is an existing transaction");
            }
            holder.requested();
            LogUtil.logIfDebugEnabled(log, log -> log.debug("Fetched SqlSession [" + holder.getSqlSession() + "] from current transaction"));
            session = holder.getSqlSession();
        }
        return session;
    }

    public static void closeSqlSession(SqlSession session, SqlSessionFactory sessionFactory) {
        SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
        if (holder != null && holder.getSqlSession() == session) {
            LogUtil.logIfDebugEnabled(log, log -> log.debug("Releasing transactional SqlSession [" + session + "]"));
            holder.released();
        } else {
            LogUtil.logIfDebugEnabled(log, log -> log.debug("Closing non transactional SqlSession [" + session + "]"));
            session.close();
        }
    }

    private static void registerSessionHolder(SqlSessionFactory sessionFactory, ExecutorType executorType, SqlSession session) {
        SqlSessionHolder holder;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            Environment environment = sessionFactory.getConfiguration().getEnvironment();
            if (environment.getTransactionFactory() instanceof IocTransactionFactory) {
                LogUtil.logIfDebugEnabled(log, log -> log.debug("Registering transaction synchronization for SqlSession [" + session + "]"));
                holder = new SqlSessionHolder(session, executorType);
                TransactionSynchronizationManager.bindResource(sessionFactory, holder);
                TransactionSynchronizationManager.registerSynchronization(new SqlSessionSynchronization(holder, sessionFactory));
                holder.setSynchronizedWithTransaction(true);
                holder.requested();
            } else {
                if (TransactionSynchronizationManager.getResource(environment.getDataSource()) == null) {
                    LogUtil.logIfDebugEnabled(log, log -> log.debug("SqlSession [" + session + "] was not registered for synchronization because DataSource is not transactional"));
                } else {
                    throw new TransientDataAccessResourceException("SqlSessionFactory must be using a IocTransactionFactory in order to use Ioc transaction synchronization");
                }
            }
        } else {
            LogUtil.logIfDebugEnabled(log, log -> log.debug("SqlSession [" + session + "] was not registered for synchronization because synchronization is not active"));
        }
    }

    @Slf4j
    private static final class SqlSessionSynchronization extends TransactionSynchronizationAdapter {
        private final SqlSessionHolder holder;

        private final SqlSessionFactory sessionFactory;

        private boolean holderActive = true;

        public SqlSessionSynchronization(SqlSessionHolder holder, SqlSessionFactory sessionFactory) {
            this.holder = holder;
            this.sessionFactory = sessionFactory;
        }

        @Override
        public int getOrder() {
            return DataSourceUtils.CONNECTION_SYNCHRONIZATION_ORDER - 1;
        }

        @Override
        public void suspend() {
            if (this.holderActive) {
                LogUtil.logIfDebugEnabled(log, log -> log.debug("Transaction synchronization suspending SqlSession [" + this.holder.getSqlSession() + "]"));
                TransactionSynchronizationManager.unbindResource(this.sessionFactory);
            }
        }

        @Override
        public void resume() {
            if (this.holderActive) {
                LogUtil.logIfDebugEnabled(log, log -> log.debug("Transaction synchronization resuming SqlSession [" + this.holder.getSqlSession() + "]"));
                TransactionSynchronizationManager.bindResource(this.sessionFactory, this.holder);
            }
        }

        @Override
        public void beforeCommit(boolean readOnly) {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                LogUtil.logIfDebugEnabled(log, log -> log.debug("Transaction synchronization committing SqlSession [" + this.holder.getSqlSession() + "]"));
                this.holder.getSqlSession().commit();
            }
        }

        @Override
        public void beforeCompletion() {
            if (!this.holder.isOpen()) {
                LogUtil.logIfDebugEnabled(log, log -> log.debug("Transaction synchronization deregistering SqlSession [" + this.holder.getSqlSession() + "]"));
                TransactionSynchronizationManager.unbindResource(sessionFactory);

                this.holderActive = false;
                LogUtil.logIfDebugEnabled(log, log -> log.debug("Transaction synchronization closing SqlSession [" + this.holder.getSqlSession() + "]"));
                this.holder.getSqlSession().close();
            }
        }

        @Override
        public void afterCompletion(int status) {
            if (this.holderActive) {
                LogUtil.logIfDebugEnabled(log, log -> log.debug("Transaction synchronization deregistering SqlSession [" + this.holder.getSqlSession() + "]"));
                TransactionSynchronizationManager.unbindResourceIfPossible(sessionFactory);

                this.holderActive = false;
                LogUtil.logIfDebugEnabled(log, log -> log.debug("Transaction synchronization closing SqlSession [" + this.holder.getSqlSession() + "]"));
                this.holder.getSqlSession().close();
            }
            this.holder.reset();
        }
    }
}
