package com.kfyty.loveqq.framework.boot.context;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ApplicationScope;
import com.kfyty.loveqq.framework.core.autoconfig.env.GenericPropertiesContext;
import com.kfyty.loveqq.framework.core.autoconfig.scope.ScopeRefreshed;
import com.kfyty.loveqq.framework.core.support.BootLauncher;
import com.kfyty.loveqq.framework.core.thread.ContextRefreshThread;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.IOC;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 描述: 上下文刷新器
 *
 * @author kfyty725
 * @date 2021/7/3 11:05
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class ContextRefresher {
    /**
     * 刷新上下文
     */
    public static void refresh() {
        refresh(IOC.getApplicationContext());
    }

    /**
     * 刷新上下文
     *
     * @param context 上下文
     */
    public static void refresh(ApplicationContext context) {
        refresh(context, 200);
    }

    /**
     * 刷新上下文
     *
     * @param context 上下文
     * @param delay   延迟刷新毫秒数
     */
    public static void refresh(ApplicationContext context, int delay) {
        // start daemon
        RefreshContextDaemon daemon = new RefreshContextDaemon(delay);
        daemon.start();

        // start refresh
        refresh(daemon, context);
    }

    /**
     * 刷新上下文
     * 要新启线程刷新，否则 http 线程无法返回
     *
     * @param daemon  刷新守护
     * @param context 上下文
     */
    public static void refresh(RefreshContextDaemon daemon, ApplicationContext context) {
        new ContextRefreshThread(() -> {
            try {
                // 等待一秒，让响应能够返回
                CommonUtil.sleep(daemon.getDelay());

                // 设置线程上下文
                BootLauncher.setContextClassLoader(context.getClass().getClassLoader());

                // 执行刷新
                context.refresh();

                // 执行应用作用域回调
                GenericPropertiesContext propertyContext = IOC.getPropertyContext();
                for (Map.Entry<String, Object> entry : context.getBeanWithAnnotation(ApplicationScope.class).entrySet()) {
                    if (entry.getValue() instanceof ScopeRefreshed scopeRefreshed) {
                        scopeRefreshed.onRefreshed(propertyContext);
                    }
                }
            } finally {
                // 结束守护任务
                daemon.finish();
            }
        }).start();
    }

    /**
     * 上下文刷新守护任务
     * 由于某些 web server 使用的是守护线程，因此当销毁后 jvm 会直接结束，导致刷新中断，因此需要该守护任务
     */
    @RequiredArgsConstructor
    public static class RefreshContextDaemon implements Runnable {
        /**
         * 延迟刷新毫秒数
         */
        @Getter
        private final int delay;

        /**
         * 快开始守护时间
         */
        private volatile long start;

        /**
         * 是否守护结束
         */
        private volatile boolean finished;

        public void start() {
            Thread daemon = new Thread(this);
            daemon.setName("ContextRefreshDaemonThread");
            daemon.setDaemon(false);
            daemon.start();

            // wait daemon started
            while (start == 0L) ;
        }

        public void finish() {
            this.finished = true;
        }

        @Override
        public void run() {
            this.start = System.currentTimeMillis();

            while (!this.finished) {
                CommonUtil.sleep(20);
            }

            log.info("Refresh application context finished in {} ms", (System.currentTimeMillis() - this.start - this.delay));
        }
    }
}
