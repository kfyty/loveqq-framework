package com.kfyty.loveqq.framework.cloud.bootstrap.loadbalancer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 描述: 微服务
 *
 * @author kfyty725
 * @date 2023/9/10 22:00
 * @email kfyty725@hotmail.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Server {
    /**
     * 服务名称，也是用于注册的服务 id
     */
    private String name;

    /**
     * 服务实例
     */
    private List<ServerInstance> instances;
}
