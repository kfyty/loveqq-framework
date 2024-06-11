package com.kfyty.loveqq.framework.boot.discovery.nacos.autoconfig.listener;

import com.alibaba.nacos.api.naming.listener.AbstractEventListener;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.kfyty.loveqq.framework.cloud.bootstrap.event.ServerEvent;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.aware.ApplicationContextAware;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 描述: nacos 服务变更事件监听器
 *
 * @author kfyty725
 * @date 2024/03/10 10:58
 * @email kfyty725@hotmail.com
 */
public class NacosNamingEventListener extends AbstractEventListener implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof NamingEvent) {
            this.onEvent((NamingEvent) event);
        }
    }

    /**
     * 转换为 ioc 容器通用事件并发布
     *
     * @param event 事件
     */
    protected void onEvent(NamingEvent event) {
        List<ServerEvent.Instance> instances = event.getInstances().stream().map(e -> new ServerEvent.Instance(e.getInstanceId(), e.getIp(), e.getPort(), e.getMetadata())).collect(Collectors.toList());
        this.applicationContext.publishEvent(new ServerEvent(new ServerEvent.Server(event.getServiceName(), instances)));
    }
}
