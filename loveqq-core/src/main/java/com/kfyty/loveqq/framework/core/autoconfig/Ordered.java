package com.kfyty.loveqq.framework.core.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;

/**
 * 描述: bean 排序，作用类似 {@link Order}，但优先级更高
 * 该接口只能用于 bean 实例排序，不能用于 {@link BeanDefinition} 排序
 * 更适合用于需要跨多个实例传递排序的场景
 *
 * @author kfyty725
 * @date 2021/5/28 15:50
 * @email kfyty725@hotmail.com
 * @see com.kfyty.loveqq.framework.core.utils.BeanUtil#getBeanOrder(Object)
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
