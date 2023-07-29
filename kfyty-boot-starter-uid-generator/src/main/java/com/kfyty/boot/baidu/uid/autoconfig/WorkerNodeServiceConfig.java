package com.kfyty.boot.baidu.uid.autoconfig;

import com.baidu.fsg.uid.worker.entity.WorkerNodeEntity;
import com.baidu.fsg.uid.worker.service.WorkerNodeService;
import com.kfyty.boot.baidu.uid.autoconfig.entity.WorkerNode;
import com.kfyty.boot.baidu.uid.autoconfig.mapper.WorkerNodeMapper;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Service;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/7/23 13:06
 * @email kfyty725@hotmail.com
 */
@Service
public class WorkerNodeServiceConfig implements WorkerNodeService {
    @Autowired
    private WorkerNodeMapper workerNodeMapper;

    @Override
    public int addWorkNode(WorkerNodeEntity workerNodeEntity) {
        WorkerNode workerNode = WorkerNode.convert(workerNodeEntity);
        int retValue = this.workerNodeMapper.insert(workerNode);
        workerNodeEntity.setId(workerNode.getId());
        return retValue;
    }

    @Override
    public WorkerNodeEntity findByHostPort(String host, String port) {
        return this.workerNodeMapper.findByHostPort(host, port);
    }
}
