package com.kfyty.loveqq.framework.core.autoconfig.delegate;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.By;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.meta.This;

/**
 * 描述: 委托接口，只需实现接口即可，无需实现具体方法
 *
 * @author kfyty725
 * @date 2021/8/10 18:02
 * @email kfyty725@hotmail.com
 * @see By
 */
@This
public interface Delegate {
    /**
     * 执行被委托的方法，使用默认实例，参数则默认使用当前委托方法的参数
     *
     * @return 被委托的方法返回值
     */
    default Object invoke() {
        throw new UnsupportedOperationException();
    }

    /**
     * 执行被委托的方法，默认使用给定的参数
     *
     * @param args 特定参数
     * @return 被委托的方法返回值
     */
    default Object invoke(Object... args) {
        throw new UnsupportedOperationException();
    }

    /**
     * 执行被委托的方法，默认使用给定的实例，以及给定的参数
     *
     * @param target 特定实例
     * @param args   特定参数
     * @return 被委托的方法返回值
     */
    default Object invoke(Object target, Object[] args) {
        throw new UnsupportedOperationException();
    }
}
