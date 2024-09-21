package com.kfyty.loveqq.framework.boot.discovery.nacos.autoconfig;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.utils.NetUtils;
import com.kfyty.loveqq.framework.boot.discovery.nacos.autoconfig.listener.NacosNamingEventListener;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.env.PropertyContext;
import com.kfyty.loveqq.framework.core.event.ApplicationListener;
import com.kfyty.loveqq.framework.core.event.ContextRefreshedEvent;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;

import java.util.Collections;

/**
 * 描述: nacos 服务注册
 *
 * @author kfyty725
 * @date 2024/03/10 10:58
 * @email kfyty725@hotmail.com
 */
public class NacosDiscoveryRegisterService implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private NacosDiscoveryProperties discoveryProperties;

    @Autowired
    private NamingService namingService;

    @Autowired
    private PropertyContext propertyContext;

    @Autowired(required = false)
    private NacosNamingEventListener nacosNamingEventListener;

    protected Instance buildInstance(String serverIp, int serverPort, String clusterName) {
        Instance instance = new Instance();
        instance.setIp(serverIp);
        instance.setPort(serverPort);
        instance.setClusterName(clusterName);
        instance.setWeight(this.discoveryProperties.getWeight());
        instance.setHealthy(this.discoveryProperties.getHealthy());
        instance.setEnabled(this.discoveryProperties.getEnabled());
        instance.setEphemeral(this.discoveryProperties.getEphemeral());
        instance.setMetadata(this.discoveryProperties.getMetadata());
        return instance;
    }

    public void registerService() {
        try {
            String groupName = this.discoveryProperties.getGroupName();
            String clusterName = this.discoveryProperties.getClusterName();
            String application = this.propertyContext.getProperty(ConstantConfig.APPLICATION_NAME_KEY);
            String serverIp = CommonUtil.empty(this.discoveryProperties.getIp()) ? NetUtils.localIP() : this.discoveryProperties.getIp();
            String serverPort = this.propertyContext.getProperty(ConstantConfig.SERVER_PORT_KEY);
            Instance instance = this.buildInstance(serverIp, Integer.parseInt(serverPort), clusterName);
            this.namingService.registerInstance(application, groupName, instance);
            if (this.nacosNamingEventListener != null) {
                this.namingService.subscribe(application, groupName, Collections.singletonList(clusterName), this.nacosNamingEventListener);
            }
        } catch (NacosException e) {
            throw new ResolvableException("Register service discovery failed", e);
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.registerService();
    }
}
