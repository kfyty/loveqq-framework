package com.kfyty.boot.baidu.uid.autoconfig;

import com.baidu.fsg.uid.UidGenerator;
import com.baidu.fsg.uid.impl.CachedUidGenerator;
import com.baidu.fsg.uid.worker.DisposableWorkerIdAssigner;
import com.baidu.fsg.uid.worker.WorkerIdAssigner;
import com.kfyty.boot.baidu.uid.autoconfig.mapper.WorkerNodeMapper;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.database.jdbc.intercept.internal.GeneratedKeysInterceptor;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.annotation.Import;

/**
 * 描述: 集成百度 uid 自动配置
 *
 * @author kfyty725
 * @date 2021/7/23 13:03
 * @email kfyty725@hotmail.com
 */
@Configuration
@Import(config = {WorkerNodeServiceConfig.class, WorkerNodeMapper.class})
public class UidGeneratorAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    public GeneratedKeysInterceptor generatedKeysInterceptor() {
        return new GeneratedKeysInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public WorkerIdAssigner disposableWorkerIdAssigner() {
        return new DisposableWorkerIdAssigner();
    }

    @Bean
    @ConditionalOnMissingBean
    public UidGenerator cachedUidGenerator(WorkerIdAssigner workerIdAssigner) {
        CachedUidGenerator uidGenerator = new CachedUidGenerator();
        uidGenerator.setWorkerIdAssigner(workerIdAssigner);
        return uidGenerator;
    }
}
