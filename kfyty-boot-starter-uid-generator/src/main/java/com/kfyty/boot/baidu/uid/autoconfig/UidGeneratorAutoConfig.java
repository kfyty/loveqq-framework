package com.kfyty.boot.baidu.uid.autoconfig;

import com.baidu.fsg.uid.UidGenerator;
import com.baidu.fsg.uid.impl.CachedUidGenerator;
import com.baidu.fsg.uid.worker.DisposableWorkerIdAssigner;
import com.baidu.fsg.uid.worker.WorkerIdAssigner;
import com.kfyty.boot.baidu.uid.autoconfig.mapper.WorkerNodeMapper;
import com.kfyty.database.jdbc.intercept.internal.GeneratedKeysInterceptor;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.annotation.Import;

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
    public GeneratedKeysInterceptor generatedKeysInterceptor() {
        return new GeneratedKeysInterceptor();
    }

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
}
