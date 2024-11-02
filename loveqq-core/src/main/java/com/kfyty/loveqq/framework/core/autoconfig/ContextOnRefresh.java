package com.kfyty.loveqq.framework.core.autoconfig;

/**
 * 描述: 刷新后的回调，一般用于内部使用
 *
 * @author kfyty725
 * @date 2021/5/21 16:57
 * @email kfyty725@hotmail.com
 */
public interface ContextOnRefresh {
    /**
     * 容器注册全部 bean 定义，开始实例化单例 bean 之前调用，主要是内部使用
     *
     * @param applicationContext 应用上下文
     */
    void onRefresh(ApplicationContext applicationContext);
}
