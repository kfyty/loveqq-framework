package com.kfyty.loveqq.framework.cloud.bootstrap.loadbalancer;

import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 描述: 微服务实例
 *
 * @author kfyty725
 * @date 2023/9/10 22:00
 * @email kfyty725@hotmail.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerInstance {
    /**
     * 元数据内置固定 key，表示服务名称，不可覆盖
     */
    public static final String META_DATA_APPLICATION_NAME = ConstantConfig.APPLICATION_NAME_KEY;

    /**
     * 服务实例id
     */
    private String id;

    /**
     * schema
     */
    private String scheme;

    /**
     * ip
     */
    private String ip;

    /**
     * port
     */
    private int port;

    /**
     * 扩展元数据
     * 注意：内置固定 key({@link this#META_DATA_APPLICATION_NAME})，表示服务名称，不可覆盖
     */
    private Map<String, String> metadata;
}
