package com.kfyty.boot.xxl.job;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.ApplicationContextAware;
import com.kfyty.support.autoconfig.ContextAfterRefreshed;
import com.kfyty.support.autoconfig.DestroyBean;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.ReflectUtil;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.handler.annotation.XxlJob;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/9/3 18:02
 * @email kfyty725@hotmail.com
 */
public class XxlJobBootExecutor extends XxlJobExecutor implements ApplicationContextAware, ContextAfterRefreshed, DestroyBean {
    private int jobCnt;
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onAfterRefreshed(ApplicationContext applicationContext) {
        this.initJobHandlerMethodRepository(applicationContext);

        BootGlueFactory.refreshInstance(this.applicationContext);

        try {
            if (this.jobCnt > 0) {
                super.start();
                ThreadPoolExecutor executor = this.applicationContext.getBean("defaultThreadPoolExecutor");
                executor.execute(() -> {});
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroy() {
        super.destroy();
    }

    private void initJobHandlerMethodRepository(ApplicationContext applicationContext) {
        if (applicationContext == null) {
            return;
        }
        Map<String, BeanDefinition> beanDefinitions = this.applicationContext.getBeanDefinitions();
        for (String beanName : beanDefinitions.keySet()) {
            Object bean = this.applicationContext.getBean(beanName);
            for (Method executeMethod : ReflectUtil.getMethods(bean.getClass())) {
                XxlJob xxlJob = AnnotationUtil.findAnnotation(executeMethod, XxlJob.class);
                if (xxlJob != null) {
                    registJobHandler(xxlJob, bean, executeMethod);
                    this.jobCnt++;
                }
            }
        }
    }
}
