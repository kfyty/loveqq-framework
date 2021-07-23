package com.kfyty.boot.baidu.uid.autoconfig.mapper;

import com.baidu.fsg.uid.worker.entity.WorkerNodeEntity;
import com.kfyty.boot.baidu.uid.autoconfig.entity.WorkerNode;
import com.kfyty.database.jdbc.BaseMapper;
import com.kfyty.database.jdbc.annotation.Param;
import com.kfyty.database.jdbc.annotation.Query;
import com.kfyty.database.jdbc.autoconfig.Mapper;

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
