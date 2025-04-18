package com.kfyty.loveqq.framework.boot.discovery.nacos.autoconfig;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.client.naming.utils.UtilAndComs;
import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBean;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import lombok.RequiredArgsConstructor;

import java.util.Properties;

/**
 * 描述: {@link NamingService} bean
 *
 * @author kfyty725
 * @date 2024/03/10 10:58
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class NamingServiceFactoryBean implements FactoryBean<NamingService> {
    private final NacosDiscoveryProperties discoveryProperties;

    @Override
    public Class<?> getBeanType() {
        return NamingService.class;
    }

    @Override
    public NamingService getObject() {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, this.discoveryProperties.getServerAddr());
        properties.put(PropertyKeyConst.NAMESPACE, this.discoveryProperties.getNamespace());
        properties.put(PropertyKeyConst.NAMING_LOAD_CACHE_AT_START, this.discoveryProperties.getNamingLoadCacheAtStart());
        if (this.discoveryProperties.getLogName() != null) {
            properties.put(UtilAndComs.NACOS_NAMING_LOG_NAME, this.discoveryProperties.getLogName());
        }
        if (this.discoveryProperties.getUsername() != null && this.discoveryProperties.getPassword() != null) {
            properties.put(PropertyKeyConst.USERNAME, this.discoveryProperties.getUsername());
            properties.put(PropertyKeyConst.PASSWORD, this.discoveryProperties.getPassword());
        }
        if (this.discoveryProperties.getAccessKey() != null && this.discoveryProperties.getSecretKey() != null) {
            properties.put(PropertyKeyConst.ACCESS_KEY, this.discoveryProperties.getAccessKey());
            properties.put(PropertyKeyConst.SECRET_KEY, this.discoveryProperties.getSecretKey());
        }
        if (this.discoveryProperties.isSecure()) {
            this.discoveryProperties.getMetadata().put("secure", "true");
        }
        if (this.discoveryProperties.getEndpoint() != null) {
            if (this.discoveryProperties.getEndpoint().indexOf(':') < 0) {
                properties.put(PropertyKeyConst.ENDPOINT, this.discoveryProperties.getEndpoint());
            } else {
                int index = this.discoveryProperties.getEndpoint().indexOf(":");
                properties.put(PropertyKeyConst.ENDPOINT, this.discoveryProperties.getEndpoint().substring(0, index));
                properties.put(PropertyKeyConst.ENDPOINT_PORT, this.discoveryProperties.getEndpoint().substring(index + 1));
            }
        }
        try {
            return NacosFactory.createNamingService(properties);
        } catch (NacosException e) {
            throw new ResolvableException("create nacos naming service failed.", e);
        }
    }
}
