package com.kfyty.boot.context.factory;

import com.kfyty.boot.K;
import com.kfyty.boot.context.DefaultApplicationContext;
import com.kfyty.support.autoconfig.ApplicationContext;

/**
 * 描述: 上下文创建工厂
 *
 * @author kfyty725
 * @date 2021/7/3 10:31
 * @email kfyty725@hotmail.com
 */
public class ApplicationContextFactory {

    public ApplicationContext create(K boot) {
        return new DefaultApplicationContext(boot);
    }
}
