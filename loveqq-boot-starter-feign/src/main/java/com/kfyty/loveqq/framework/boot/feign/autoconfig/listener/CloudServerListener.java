package com.kfyty.loveqq.framework.boot.feign.autoconfig.listener;

import com.kfyty.loveqq.framework.cloud.bootstrap.event.ServerEvent;
import com.kfyty.loveqq.framework.cloud.bootstrap.loadbalancer.ServerInstance;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.event.ApplicationListener;
import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAwareLoadBalancer;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 描述: 服务变更事件监听器
 *
 * @author kfyty725
 * @date 2024/3/08 18:55
 * @email kfyty725@hotmail.com
 */
public class CloudServerListener implements ApplicationListener<ServerEvent> {
    @Autowired
    protected ZoneAwareLoadBalancer<Server> loadBalancer;

    @Override
    public void onApplicationEvent(ServerEvent event) {
        synchronized (this) {
            List<Server> servers = this.loadBalancer.getAllServers();
            Map<String, ServerInstance> instanceMap = event.getSource().getInstances().stream().collect(Collectors.toMap(e -> e.getIp() + ':' + e.getPort(), v -> v));
            for (Server server : servers) {
                server.setAlive(instanceMap.containsKey(server.getId()));
                instanceMap.remove(server.getId());
            }

            // 更新已有的
            this.loadBalancer.setServersList(servers);

            // 创建新的
            for (ServerInstance instance : instanceMap.values()) {
                Server server = new Server(instance.getId());
                server.setHost(instance.getIp());
                server.setPort(instance.getPort());
                server.setAlive(true);
                ReflectUtil.setFieldValue(server, "simpleMetaInfo", new Metadata(server.getId(), instance.getMetadata() == null ? null : instance.getMetadata().get(ConstantConfig.APPLICATION_NAME_KEY)));
                this.loadBalancer.addServer(server);
            }
        }
    }

    @RequiredArgsConstructor
    protected static class Metadata implements Server.MetaInfo {
        private final String id;
        private final String appName;

        @Override
        public String getAppName() {
            return this.appName;
        }

        @Override
        public String getServerGroup() {
            return "";
        }

        @Override
        public String getServiceIdForDiscovery() {
            return "";
        }

        @Override
        public String getInstanceId() {
            return this.id;
        }
    }
}
