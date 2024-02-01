package com.kfyty.boot.xxl.job;

import com.kfyty.boot.xxl.job.annotation.XxlJob;
import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.ContextAfterRefreshed;
import com.kfyty.core.autoconfig.DestroyBean;
import com.kfyty.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.ReflectUtil;
import com.xxl.job.core.executor.XxlJobExecutor;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * 描述: xxl job 执行器
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
                ExecutorService executor = this.applicationContext.getBean("defaultThreadPoolExecutor");
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
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitions.entrySet()) {
            if (!entry.getValue().isAutowireCandidate() || !AnnotationUtil.hasAnnotation(entry.getValue().getBeanType(), XxlJob.class)) {
                continue;
            }
            Object bean = this.applicationContext.getBean(entry.getKey());
            for (Method executeMethod : ReflectUtil.getMethods(bean.getClass())) {
                XxlJob xxlJob = AnnotationUtil.findAnnotation(executeMethod, XxlJob.class);
                if (xxlJob != null) {
                    registJobHandler(new XxlJobAnnotationAdapter(xxlJob, executeMethod), bean, executeMethod);
                    this.jobCnt++;
                }
            }
        }
    }

    @RequiredArgsConstructor
    @SuppressWarnings("ClassExplicitlyAnnotation")
    private static class XxlJobAnnotationAdapter implements com.xxl.job.core.handler.annotation.XxlJob {
        private final XxlJob xxlJob;
        private final Method executeMethod;

        @Override
        public String value() {
            return CommonUtil.EMPTY_STRING.equals(this.xxlJob.value()) ? this.executeMethod.getName() : this.xxlJob.value();
        }

        @Override
        public String init() {
            return this.xxlJob.init();
        }

        @Override
        public String destroy() {
            return this.xxlJob.destroy();
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return com.xxl.job.core.handler.annotation.XxlJob.class;
        }
    }
}
