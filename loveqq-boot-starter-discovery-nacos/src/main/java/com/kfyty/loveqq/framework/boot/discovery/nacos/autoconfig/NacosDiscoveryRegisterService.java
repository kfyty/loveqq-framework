package com.kfyty.loveqq.framework.boot.discovery.nacos.autoconfig;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.api.utils.NetUtils;
import com.kfyty.loveqq.framework.boot.discovery.nacos.autoconfig.listener.NacosNamingEventListener;
import com.kfyty.loveqq.framework.core.autoconfig.BeanFactoryPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Value;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.beans.builder.BeanDefinitionBuilder;
import com.kfyty.loveqq.framework.core.event.ApplicationListener;
import com.kfyty.loveqq.framework.core.event.ContextRefreshedEvent;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 描述: nacos 服务注册
 *
 * @author kfyty725
 * @date 2024/03/10 10:58
 * @email kfyty725@hotmail.com
 */
public class NacosDiscoveryRegisterService implements BeanFactoryPostProcessor, ApplicationListener<ContextRefreshedEvent> {
    /**
     * 应用服务名称
     */
    @Value("${k.application.name:}")
    private String applicationName;

    /**
     * 应用端口
     */
    @Value("${k.server.port:-1}")
    private Integer serverPort;

    /**
     * bean 工厂
     */
    private BeanFactory beanFactory;

    /**
     * 配置属性
     */
    @Autowired
    private NacosDiscoveryProperties discoveryProperties;

    /**
     * 监听器
     */
    @Autowired(required = false)
    private NacosNamingEventListener nacosNamingEventListener;

    @Override
    public void postProcessBeanFactory(BeanFactory beanFactory) {
        for (Map.Entry<String, NacosDiscoveryProperties> entry : this.discoveryProperties.getDiscoveries().entrySet()) {
            BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(NamingServiceFactoryBean.class)
                    .setBeanName(entry.getKey())
                    .addConstructorArgs(NacosDiscoveryProperties.class, entry.getValue())
                    .setDestroyMethod("shutDown")
                    .getBeanDefinition();
            beanFactory.registerBeanDefinition(beanDefinition.getBeanName(), beanDefinition, false);
        }
        this.beanFactory = beanFactory;
    }

    public void registerService() {
        for (Map.Entry<String, NacosDiscoveryProperties> entry : this.discoveryProperties.getDiscoveries().entrySet()) {
            NacosDiscoveryProperties properties = entry.getValue();
            NamingService namingService = this.beanFactory.getBean(entry.getKey());
            String groupName = properties.getGroupName();
            String clusterName = properties.getClusterName();
            String application = CommonUtil.empty(properties.getService()) ? this.applicationName : properties.getService();
            String serverIp = CommonUtil.empty(properties.getIp()) ? NetUtils.localIP() : properties.getIp();
            int serverPort = properties.getPort() < 0 ? this.serverPort : properties.getPort();
            Instance instance = this.buildInstance(serverIp, serverPort, clusterName, properties);
            try {
                namingService.registerInstance(application, groupName, instance);
                if (this.nacosNamingEventListener != null) {
                    this.subscribeAllServerOfNamespace(groupName, namingService);
                }
            } catch (NacosException e) {
                throw new ResolvableException("Register service discovery failed.", e);
            }
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.registerService();
    }

    protected Instance buildInstance(String serverIp, int serverPort, String clusterName, NacosDiscoveryProperties properties) {
        Instance instance = new Instance();
        instance.setInstanceId(serverIp + ':' + serverPort);
        instance.setIp(serverIp);
        instance.setPort(serverPort);
        instance.setClusterName(clusterName);
        instance.setWeight(properties.getWeight());
        instance.setHealthy(properties.getHealthy());
        instance.setEnabled(properties.getEnabled());
        instance.setEphemeral(properties.getEphemeral());
        instance.setMetadata(properties.getMetadata());
        if (instance.getMetadata() != null) {
            instance.getMetadata().put(ConstantConfig.APPLICATION_NAME_KEY, this.applicationName);
        } else {
            Map<String, String> metadata = new HashMap<>(4);
            instance.getMetadata().put(ConstantConfig.APPLICATION_NAME_KEY, this.applicationName);
            instance.setMetadata(metadata);
        }
        return instance;
    }

    /**
     * 订阅该命名空间下的所有服务变更事件
     */
    protected void subscribeAllServerOfNamespace(String groupName, NamingService namingService) throws NacosException {
        int pageNo = 1;
        List<String> serverNames = new LinkedList<>();
        while (true) {
            ListView<String> services = namingService.getServicesOfServer(pageNo++, 1000, groupName);
            if (services == null || services.getData() == null || services.getData().isEmpty()) {
                break;
            }
            serverNames.addAll(services.getData());
            if (serverNames.size() >= services.getCount()) {
                break;
            }
        }
        for (String serverName : serverNames) {
            // 订阅
            namingService.subscribe(serverName, groupName, this.nacosNamingEventListener);

            // 手动触发一次
            List<Instance> instances = namingService.getAllInstances(serverName);
            this.nacosNamingEventListener.onEvent(new NamingEvent(serverName, instances));
        }
    }
}
