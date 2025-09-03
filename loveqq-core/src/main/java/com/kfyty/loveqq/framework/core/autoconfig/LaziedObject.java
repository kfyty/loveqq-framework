package com.kfyty.loveqq.framework.core.autoconfig;

import com.kfyty.loveqq.framework.core.lang.Lazy;

/**
 * 描述: 惰性对象支持标记接口
 * <p>
 * 默认实现 {@link Lazy}
 *
 * @author kfyty725
 * @date 2021/8/10 18:02
 * @email kfyty725@hotmail.com
 */
public interface LaziedObject<T> {
    /**
     * 创建新的值
     *
     * @return value
     */
    T create();

    /**
     * 获取实际对象
     * 如果已创建过，则返回创建过的值
     *
     * @return 实际对象
     */
    T get();
}
