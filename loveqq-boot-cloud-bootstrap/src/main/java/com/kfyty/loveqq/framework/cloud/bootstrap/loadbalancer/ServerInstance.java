package com.kfyty.loveqq.framework.cloud.bootstrap.loadbalancer;

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
    private String id;
    private String scheme;
    private String ip;
    private int port;
    private Map<String, String> metadata;
}
