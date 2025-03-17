package com.kfyty.loveqq.framework.boot.data.jdbc;

import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 描述: {@link ThreadLocal} 实现
 *
 * @author kfyty725
 * @date 2022/5/30 14:55
 * @email kfyty725@hotmail.com
 */
@NoArgsConstructor
public class ThreadLocalRoutingDataSource extends AbstractRoutingDataSource {
    /**
     * 线程上下文数据源 key
     */
    public static final ThreadLocal<Object> DATA_SOURCE_KEY = new ThreadLocal<>();

    public ThreadLocalRoutingDataSource(DataSource defaultDataSource) {
        this(defaultDataSource, Collections.emptyMap());
    }

    public ThreadLocalRoutingDataSource(DataSource defaultDataSource, Map<Object, DataSource> dataSourceMap) {
        this.setDefaultDataSource(defaultDataSource);
        this.addDataSource(dataSourceMap);
    }

    @Override
    protected Object obtainTargetDataSourceKey() {
        return DATA_SOURCE_KEY.get();
    }

    /**
     * 设置线程上下文数据源 key
     * 仅提供一个设置方法，建议使用完毕后，恢复为前一个数据源 key，而不是删除线程上下文
     *
     * @param dataSourceKey 数据源 key
     * @return 前一个数据源 key
     */
    public static Object setCurrentDataSource(Object dataSourceKey) {
        Object prev = DATA_SOURCE_KEY.get();
        DATA_SOURCE_KEY.set(dataSourceKey);
        return prev;
    }

    /**
     * 在指定的数据源上执行
     *
     * @param dataSourceKey 数据源 key
     * @param supplier      结果提供者
     * @return 返回数据
     */
    public static <T> T proceedOnDataSource(Object dataSourceKey, Supplier<T> supplier) {
        Object prev = setCurrentDataSource(dataSourceKey);
        try {
            return supplier.get();
        } finally {
            setCurrentDataSource(prev);
        }
    }
}
