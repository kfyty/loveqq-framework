package com.kfyty.boot.baidu.uid.autoconfig;

import com.baidu.fsg.uid.UidGenerator;
import com.baidu.fsg.uid.impl.CachedUidGenerator;
import com.baidu.fsg.uid.worker.DisposableWorkerIdAssigner;
import com.baidu.fsg.uid.worker.WorkerIdAssigner;
import com.kfyty.boot.baidu.uid.autoconfig.mapper.WorkerNodeMapper;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.DestroyBean;
import com.kfyty.support.autoconfig.InitializingBean;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.annotation.Import;
import com.kfyty.support.utils.ReflectUtil;

import java.lang.reflect.Method;

/**
 * 描述: 集成百度 uid 自动配置
 *
 * @author kfyty725
 * @date 2021/7/23 13:03
 * @email kfyty725@hotmail.com
 */
@Configuration
@Import(config = {WorkerNodeServiceConfig.class, WorkerNodeMapper.class})
public class UidGeneratorAutoConfig implements InitializingBean, DestroyBean {
    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public WorkerIdAssigner disposableWorkerIdAssigner() {
        return new DisposableWorkerIdAssigner();
    }

    @Bean
    public UidGenerator cachedUidGenerator(WorkerIdAssigner workerIdAssigner) {
        CachedUidGenerator uidGenerator = new CachedUidGenerator();
        uidGenerator.setWorkerIdAssigner(workerIdAssigner);
        return uidGenerator;
    }

    @Override
    public void afterPropertiesSet() {
        UidGenerator uidGenerator = this.applicationContext.getBean(UidGenerator.class);
        Method method = ReflectUtil.getMethod(uidGenerator.getClass(), "afterPropertiesSet");
        if (method != null) {
            ReflectUtil.invokeMethod(uidGenerator, method);
        }
    }

    @Override
    public void onDestroy() {
        UidGenerator uidGenerator = this.applicationContext.getBean(UidGenerator.class);
        if (uidGenerator != null) {
            Method method = ReflectUtil.getMethod(uidGenerator.getClass(), "destroy");
            if (method != null) {
                ReflectUtil.invokeMethod(uidGenerator, method);
            }
        }
    }
}
