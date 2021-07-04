package com.kfyty.support.autoconfig;

/**
 * 描述: 刷新后的回调，一般用于内部使用
 *
 * @author kfyty725
 * @date 2021/5/21 16:57
 * @email kfyty725@hotmail.com
 */
public interface ContextAfterRefreshed {
    void onAfterRefreshed(ApplicationContext applicationContext);
}
