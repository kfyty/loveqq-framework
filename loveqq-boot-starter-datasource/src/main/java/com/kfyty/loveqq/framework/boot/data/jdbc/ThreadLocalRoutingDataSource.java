package com.kfyty.loveqq.framework.boot.data.jdbc;

/**
 * 描述: {@link ThreadLocal} 实现
 *
 * @author kfyty725
 * @date 2022/5/30 14:55
 * @email kfyty725@hotmail.com
 */
public class ThreadLocalRoutingDataSource extends AbstractRoutingDataSource {
    /**
     * 线程上下文数据源 key
     */
    private static final ThreadLocal<Object> DATA_SOURCE_KEY = new ThreadLocal<>();

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

    @Override
    protected Object obtainTargetDataSourceKey() {
        return DATA_SOURCE_KEY.get();
    }
}
