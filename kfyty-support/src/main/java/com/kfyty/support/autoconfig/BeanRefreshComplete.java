package com.kfyty.support.autoconfig;

/**
 * 描述: 刷新完成后的回调
 *
 * @author kfyty725
 * @date 2021/5/21 16:57
 * @email kfyty725@hotmail.com
 */
public interface BeanRefreshComplete {
    void onComplete(Class<?> primarySource);
}
