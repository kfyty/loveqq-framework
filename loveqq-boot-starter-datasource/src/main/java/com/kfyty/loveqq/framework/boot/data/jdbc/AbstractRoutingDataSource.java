package com.kfyty.loveqq.framework.boot.data.jdbc;

import lombok.Setter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述: 动态数据源支持
 *
 * @author kfyty725
 * @date 2022/5/30 14:55
 * @email kfyty725@hotmail.com
 */
public abstract class AbstractRoutingDataSource extends AbstractDataSource implements DataSource {
    /**
     * 默认的数据源
     */
    @Setter
    private DataSource defaultDataSource;

    /**
     * 数据源集合
     */
    private final Map<Object, DataSource> dataSources = new ConcurrentHashMap<>();

    @Override
    public Connection getConnection() throws SQLException {
        return this.obtainTargetDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return this.obtainTargetDataSource().getConnection(username, password);
    }

    /**
     * 添加单个数据源
     *
     * @param key        数据源 key
     * @param dataSource 数据源
     */
    public void addDataSource(Object key, DataSource dataSource) {
        this.dataSources.put(key, dataSource);
    }

    /**
     * 添加数据源
     *
     * @param dataSources 数据源集合
     */
    public void addDataSource(Map<Object, DataSource> dataSources) {
        this.dataSources.putAll(dataSources);
    }

    /**
     * 获取一个数据源
     *
     * @return 数据源
     */
    protected DataSource obtainTargetDataSource() {
        Object dataSourceKey = this.obtainTargetDataSourceKey();
        DataSource dataSource = dataSourceKey == null ? this.defaultDataSource : this.dataSources.getOrDefault(dataSourceKey, this.defaultDataSource);
        if (dataSource == null) {
            throw new IllegalStateException("Cannot determine target DataSource for key [" + dataSourceKey + "]");
        }
        return dataSource;
    }

    /**
     * 获取数据源 key
     *
     * @return 数据源 key
     */
    protected abstract Object obtainTargetDataSourceKey();
}
