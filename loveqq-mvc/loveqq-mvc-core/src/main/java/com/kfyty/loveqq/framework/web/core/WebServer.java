package com.kfyty.loveqq.framework.web.core;

/**
 * 描述: web 服务器
 *
 * @author kfyty725
 * @date 2021/5/28 14:49
 * @email kfyty725@hotmail.com
 */
public interface WebServer {
    /**
     * 启动
     */
    void start();

    /**
     * 停止
     */
    void stop();

    /**
     * 是否已启动
     *
     * @return true if started
     */
    boolean isStart();

    /**
     * 获取启动端口
     *
     * @return port
     */
    int getPort();

    /**
     * 返回请求分发器
     *
     * @return {@link AbstractDispatcher}
     */
    AbstractDispatcher<?> getDispatcher();
}
