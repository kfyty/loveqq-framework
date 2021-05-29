package com.kfyty.mvc.tomcat;

import lombok.Data;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/5/28 14:52
 * @email kfyty725@hotmail.com
 */
@Data
public class TomcatConfig {
    public static final String DEFAULT_PROTOCOL = "org.apache.coyote.http11.Http11NioProtocol";
    public static final String DEFAULT_DISPATCHER_MAPPING = "/";

    private int port;
    private String protocol;
    private String dispatcherMapping;
    private Class<?> primarySource;

    public TomcatConfig() {
        this(TomcatConfig.class);
    }

    public TomcatConfig(Class<?> primarySource) {
        this.port = 8080;
        this.protocol = DEFAULT_PROTOCOL;
        this.dispatcherMapping = DEFAULT_DISPATCHER_MAPPING;
        this.primarySource = primarySource;
    }
}
