package com.kfyty.loveqq.framework.core.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;

/**
 * 描述: order，作用同 {@link com.kfyty.loveqq.framework.core.autoconfig.annotation.Order}，但优先级更高
 *
 * @author kfyty725
 * @date 2021/5/28 15:50
 * @email kfyty725@hotmail.com
 */
public interface Ordered {
    /**
     * 返回 order
     *
     * @return order
     */
    default int getOrder() {
        return Order.DEFAULT_PRECEDENCE;
    }
}
