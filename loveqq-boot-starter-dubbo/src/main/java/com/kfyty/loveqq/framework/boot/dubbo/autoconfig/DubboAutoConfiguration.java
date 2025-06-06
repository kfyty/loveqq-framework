package com.kfyty.loveqq.framework.boot.dubbo.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ContextOnRefresh;
import com.kfyty.loveqq.framework.core.autoconfig.DestroyBean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ComponentFilter;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ComponentScan;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.apache.dubbo.rpc.model.FrameworkModel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

/**
 * 描述: dubbo 自动配置
 *
 * @author kfyty725
 * @date 2024/10/29 20:31
 * @email kfyty725@hotmail.com
 */
@Configuration
@ComponentScan(includeFilter = @ComponentFilter(annotations = DubboService.class))
public class DubboAutoConfiguration implements ContextOnRefresh {
    @Autowired
    private DubboProperties dubboProperties;

    @Bean(destroyMethod = "stop", resolveNested = false, independent = true)
    public DubboBootstrap dubboBootstrap() {
        DubboBootstrap dubboBootstrap = DubboBootstrap.newInstance(FrameworkModel.defaultModel())
                .application(this.dubboProperties.getApplication())
                .module(this.dubboProperties.getModule());
        Mapping.from(this.dubboProperties.getRegistry()).whenNotNull(e -> e.forEach((k, v) -> v.setId(k))).whenNotNull(e -> dubboBootstrap.registries(new LinkedList<>(e.values())));
        Mapping.from(this.dubboProperties.getConfigCenter()).whenNotNull(e -> e.forEach((k, v) -> v.setId(k))).whenNotNull(e -> dubboBootstrap.configCenters(new LinkedList<>(e.values())));
        Mapping.from(this.dubboProperties.getProtocol()).whenNotNull(e -> e.forEach((k, v) -> v.setId(k))).whenNotNull(e -> dubboBootstrap.protocols(new LinkedList<>(e.values())));
        Mapping.from(this.dubboProperties.getMetadataReport()).whenNotNull(e -> e.forEach((k, v) -> v.setId(k))).whenNotNull(e -> dubboBootstrap.metadataReports(new LinkedList<>(e.values())));
        Mapping.from(this.dubboProperties.getProvider()).whenNotNull(dubboBootstrap::providers);
        Mapping.from(this.dubboProperties.getConsumer()).whenNotNull(dubboBootstrap::consumers);
        Mapping.from(this.dubboProperties.getMonitor()).whenNotNull(dubboBootstrap::monitor);
        Mapping.from(this.dubboProperties.getMetrics()).whenNotNull(dubboBootstrap::metrics);
        Mapping.from(this.dubboProperties.getTracing()).whenNotNull(dubboBootstrap::tracing);
        Mapping.from(this.dubboProperties.getSsl()).whenNotNull(dubboBootstrap::ssl);
        return dubboBootstrap;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void onRefresh(ApplicationContext applicationContext) {
        Map<String, ServiceConfig> serviceConfig = applicationContext.getBeanOfType(ServiceConfig.class);
        Map<String, ReferenceConfig> referenceConfig = applicationContext.getBeanOfType(ReferenceConfig.class);
        this.dubboBootstrap()
                .services(new ArrayList<>(serviceConfig.values()))
                .references(new ArrayList<>(referenceConfig.values()))
                .start();
    }

    /**
     * 由于 {@link org.apache.dubbo.common.utils.PojoUtils#GENERIC_WITH_CLZ} 的静态实例化，
     * 会导致调用 {@link ApplicationModel#defaultModel()}，从而导致无法真正销毁 {@link DubboBootstrap}
     */
    @Component
    @Order(Order.HIGHEST_PRECEDENCE)
    static class DubboDestroyBean implements DestroyBean {

        @Override
        public void destroy() {
            ApplicationModel.defaultModel().destroy();
        }
    }
}
