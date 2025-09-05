package com.kfyty.loveqq.framework.core.autoconfig.scope;

import com.kfyty.loveqq.framework.core.autoconfig.env.PropertyContext;

/**
 * 描述: 作用域刷新时回调接口
 * 当非单例 bean 的作用域刷新时，如果对应的 bean 实现了该接口，则回调该接口，而不是默认的销毁重建
 *
 * @author kfyty725
 * @date 2022/10/22 9:39
 * @email kfyty725@hotmail.com
 */
public interface ScopeRefreshed {
    /**
     * 作用域刷新时回调
     *
     * @param propertyContext 配置上下文
     */
    void onRefreshed(PropertyContext propertyContext);
}
