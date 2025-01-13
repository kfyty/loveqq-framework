package com.kfyty.loveqq.framework.boot.uid.baidu.autoconfig.mapper;

import com.baidu.fsg.uid.worker.entity.WorkerNodeEntity;
import com.kfyty.loveqq.framework.boot.uid.baidu.autoconfig.entity.WorkerNode;
import com.kfyty.loveqq.framework.data.korm.BaseMapper;
import com.kfyty.loveqq.framework.data.korm.annotation.Param;
import com.kfyty.loveqq.framework.data.korm.annotation.Query;
import com.kfyty.loveqq.framework.data.korm.autoconfig.Mapper;

/**
 * 描述: WorkerNodeEntity mapper
 *
 * @author kfyty725
 * @date 2021/7/23 13:06
 * @email kfyty725@hotmail.com
 */
@Mapper
public interface WorkerNodeMapper extends BaseMapper<Long, WorkerNode> {
    @Query("select * from worker_node where host = #{host} and port = #{port}")
    WorkerNodeEntity findByHostPort(@Param("host") String host, @Param("port") String port);
}
