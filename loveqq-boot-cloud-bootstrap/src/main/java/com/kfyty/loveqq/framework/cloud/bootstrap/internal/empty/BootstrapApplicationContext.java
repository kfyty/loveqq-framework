package com.kfyty.loveqq.framework.cloud.bootstrap.internal.empty;

import com.kfyty.loveqq.framework.boot.context.DefaultConfigurableApplicationContext;

/**
 * 描述: 引导上下文
 *
 * @author kfyty725
 * @date 2021/7/3 10:31
 * @email kfyty725@hotmail.com
 */
public class BootstrapApplicationContext extends DefaultConfigurableApplicationContext {

    @Override
    protected void invokeBootstrap() {
        // 已经在引导内，不再执行
    }

    @Override
    protected boolean isBootstrap() {
        return true;
    }
}
