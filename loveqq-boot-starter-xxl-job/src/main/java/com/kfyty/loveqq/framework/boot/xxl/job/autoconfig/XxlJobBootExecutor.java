package com.kfyty.loveqq.framework.boot.xxl.job.autoconfig;

import com.kfyty.loveqq.framework.boot.xxl.job.autoconfig.annotation.XxlJob;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ContextAfterRefreshed;
import com.kfyty.loveqq.framework.core.autoconfig.DestroyBean;
import com.kfyty.loveqq.framework.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
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
                ExecutorService executor = this.applicationContext.getBean(ConstantConfig.DEFAULT_THREAD_POOL_EXECUTOR);
                executor.execute(() -> {});
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    private void initJobHandlerMethodRepository(ApplicationContext applicationContext) {
        if (applicationContext == null) {
            return;
        }
        Map<String, BeanDefinition> beanDefinitions = this.applicationContext.getBeanDefinitionWithAnnotation(XxlJob.class, true);
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitions.entrySet()) {
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
