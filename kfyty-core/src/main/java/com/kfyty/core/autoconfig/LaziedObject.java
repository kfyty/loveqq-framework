package com.kfyty.core.autoconfig;

/**
 * 描述: 惰性对象支持标记接口
 * <p>
 * 默认实现 {@link com.kfyty.core.lang.Lazy}
 *
 * @author kfyty725
 * @date 2021/8/10 18:02
 * @email kfyty725@hotmail.com
 */
public interface LaziedObject<T> {
    /**
     * 获取实际对象
     *
     * @return 实际对象
     */
    T get();
}
