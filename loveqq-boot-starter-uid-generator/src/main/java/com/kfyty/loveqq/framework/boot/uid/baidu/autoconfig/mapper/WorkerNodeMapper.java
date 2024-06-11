package com.kfyty.loveqq.framework.boot.uid.baidu.autoconfig.mapper;

import com.baidu.fsg.uid.worker.entity.WorkerNodeEntity;
import com.kfyty.loveqq.framework.boot.uid.baidu.autoconfig.entity.WorkerNode;
import com.kfyty.loveqq.framework.data.jdbc.BaseMapper;
import com.kfyty.loveqq.framework.data.jdbc.annotation.Param;
import com.kfyty.loveqq.framework.data.jdbc.annotation.Query;
import com.kfyty.loveqq.framework.data.jdbc.autoconfig.Mapper;

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
