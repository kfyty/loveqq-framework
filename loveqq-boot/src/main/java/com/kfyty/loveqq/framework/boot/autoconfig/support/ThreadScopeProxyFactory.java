package com.kfyty.loveqq.framework.boot.autoconfig.support;

import com.kfyty.loveqq.framework.core.thread.ContextRefreshThread;
import com.kfyty.loveqq.framework.core.autoconfig.DestroyBean;
import com.kfyty.loveqq.framework.core.autoconfig.InitializingBean;
import com.kfyty.loveqq.framework.core.autoconfig.aware.BeanFactoryAware;
import com.kfyty.loveqq.framework.core.autoconfig.aware.PropertyContextAware;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.env.GenericPropertiesContext;
import com.kfyty.loveqq.framework.core.autoconfig.scope.ScopeProxyFactory;
import com.kfyty.loveqq.framework.core.autoconfig.scope.ScopeRefreshed;
import com.kfyty.loveqq.framework.core.thread.SingleThreadTask;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.kfyty.loveqq.framework.core.lang.ConstantConfig.THREAD_SCOPE_MONITOR_INTERNAL;

/**
 * 描述: 线程代理工厂
 *
 * @author kfyty725
 * @date 2022/10/22 10:17
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class ThreadScopeProxyFactory implements ScopeProxyFactory, InitializingBean, DestroyBean, PropertyContextAware, BeanFactoryAware {
    /**
     * 线程状态监听器
     */
    protected final ThreadStateMonitor monitor;

    /**
     * 线程缓存
     */
    protected final ThreadLocal<Map<String, Object>> threadCache;

    /**
     * bean 缓存，存储的是实现了 {@link ScopeRefreshed} 接口的 bean
     */
    protected final Map<String, Object> cache;

    /**
     * 配置上下文
     */
    protected GenericPropertiesContext propertiesContext;

    /**
     * bean 工厂
     */
    protected BeanFactory beanFactory;

    public ThreadScopeProxyFactory() {
        this.monitor = new ThreadStateMonitor("thread-scope-state-monitor");
        this.cache = new ConcurrentHashMap<>();
        this.threadCache = ThreadLocal.withInitial(HashMap::new);
    }

    @Override
    public void setPropertyContext(GenericPropertiesContext propertiesContext) {
        this.propertiesContext = propertiesContext;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterPropertiesSet() {
        this.monitor.start();
    }

    @Override
    public Object getObject(BeanDefinition beanDefinition, BeanFactory beanFactory) {
        Map<String, Object> threadLocalCache = this.threadCache.get();
        this.monitor.addThread(Thread.currentThread(), threadLocalCache);
        return threadLocalCache.computeIfAbsent(beanDefinition.getBeanName(), beanName -> {
            if (ScopeRefreshed.class.isAssignableFrom(beanDefinition.getBeanType())) {
                return this.cache.computeIfAbsent(beanName, key -> beanFactory.registerBean(beanDefinition));
            }
            return beanFactory.registerBean(beanDefinition);
        });
    }

    @Override
    public void destroy() {
        for (Map.Entry<String, Object> entry : this.cache.entrySet()) {
            this.beanFactory.destroyBean(entry.getKey(), entry.getValue());
        }
    }

    protected class ThreadStateMonitor extends SingleThreadTask {
        /**
         * 线程集合
         */
        protected Map<Thread, Map<String, Object>> threads = new ConcurrentHashMap<>();

        /**
         * 添加监听线程
         *
         * @param thread 线程
         */
        public void addThread(Thread thread, Map<String, Object> threadLocal) {
            this.threads.put(thread, threadLocal);
        }

        /**
         * 应该是单例模式
         *
         * @param task 任务名称
         */
        protected ThreadStateMonitor(String task) {
            super(task);
        }

        @Override
        protected void sleep() {
            String delay = System.getProperty(THREAD_SCOPE_MONITOR_INTERNAL, "200");
            CommonUtil.sleep(Long.parseLong(delay));
        }

        @Override
        protected void doRun() {
            Iterator<Map.Entry<Thread, Map<String, Object>>> iterator = this.threads.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Thread, Map<String, Object>> entry = iterator.next();

                // 上下文刷新线程不处理
                if (entry.getKey() instanceof ContextRefreshThread) {
                    iterator.remove();
                    continue;
                }

                // 监听线程状态
                Thread.State state = entry.getKey().getState();
                if (state == Thread.State.TERMINATED) {
                    iterator.remove();
                    destroyThreadScope(CommonUtil.sortBeanOrder(entry.getValue()));
                }
            }
        }

        protected void destroyThreadScope(Map<String, Object> beans) {
            Iterator<Map.Entry<String, Object>> iterator = beans.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                try {
                    if (entry.getValue() instanceof ScopeRefreshed scopeRefreshed) {
                        scopeRefreshed.onRefreshed(propertiesContext);
                    } else if (!cache.containsKey(entry.getKey())) {
                        beanFactory.destroyBean(entry.getKey(), entry.getValue());
                    }
                } catch (Throwable e) {
                    log.error("Refresh bean error: {}", entry.getKey(), e);
                }
                iterator.remove();
            }
        }
    }
}
