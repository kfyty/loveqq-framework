package com.kfyty.boot.context;

import com.kfyty.boot.K;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 功能描述: 应用配置
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/23 16:33
 * @since JDK 1.8
 */
@Slf4j
@NoArgsConstructor
public class DefaultApplicationContext extends AbstractApplicationContext {
    private K boot;

    public DefaultApplicationContext(K boot) {
        this.boot = boot;
    }

    @Override
    protected void beforeRefresh() {
        this.commanderArgs = this.boot.getCommanderArgs();
        this.primarySource = this.boot.getPrimarySource();
        this.scanClasses = this.boot.getScanClasses();
        this.excludeBeanNames = this.boot.getExcludeBeanNames();
        this.excludeBeanClasses = this.boot.getExcludeBeanClasses();
        this.includeFilterAnnotations = this.boot.getIncludeFilterAnnotations();
        this.excludeFilterAnnotations = this.boot.getExcludeFilterAnnotations();
        super.beforeRefresh();
    }
}
