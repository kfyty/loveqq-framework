package com.kfyty.cloud.discovery.nacos.autoconfig;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.utils.NetUtils;
import com.kfyty.cloud.discovery.nacos.autoconfig.listener.NacosNamingEventListener;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.env.PropertyContext;
import com.kfyty.core.event.ApplicationListener;
import com.kfyty.core.event.ContextRefreshedEvent;
import com.kfyty.core.exception.SupportException;
import com.kfyty.core.utils.CommonUtil;

import java.util.Collections;

/**
 * 描述: nacos 服务注册
 *
 * @author kfyty725
 * @date 2024/03/10 10:58
 * @email kfyty725@hotmail.com
 */
public class NacosDiscoveryRegisterService implements ApplicationListener<ContextRefreshedEvent> {
    private static final String APPLICATION_NAME_KEY = "k.application.name";

    private static final String SERVER_PORT_KEY = "k.server.port";

    @Autowired
    private NacosDiscoveryProperties discoveryProperties;

    @Autowired
    private NamingService namingService;

    @Autowired
    private PropertyContext propertyContext;

    @Autowired(required = false)
    private NacosNamingEventListener nacosNamingEventListener;

    public void registerService() {
        try {
            String groupName = this.discoveryProperties.getGroupName();
            String clusterName = this.discoveryProperties.getClusterName();
            String application = this.propertyContext.getProperty(APPLICATION_NAME_KEY);
            String serverPort = this.propertyContext.getProperty(SERVER_PORT_KEY);
            String serverIp = CommonUtil.empty(this.discoveryProperties.getIp()) ? NetUtils.localIP() : this.discoveryProperties.getIp();
            this.namingService.registerInstance(application, groupName, serverIp, Integer.parseInt(serverPort), clusterName);
            if (this.nacosNamingEventListener != null) {
                this.namingService.subscribe(application, groupName, Collections.singletonList(clusterName), this.nacosNamingEventListener);
            }
        } catch (NacosException e) {
            throw new SupportException("Register service discovery failed", e);
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.registerService();
    }
}
