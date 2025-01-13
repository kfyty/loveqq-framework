package com.kfyty.loveqq.framework.boot.uid.baidu.autoconfig.entity;

import com.baidu.fsg.uid.worker.entity.WorkerNodeEntity;
import com.kfyty.loveqq.framework.data.korm.annotation.TableId;
import com.kfyty.loveqq.framework.data.korm.annotation.TableName;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 描述: WorkerNodeEntity 子类，用于设置表名
 *
 * @author kfyty725
 * @date 2021/7/23 15:40
 * @email kfyty725@hotmail.com
 */
@Data
@TableName("worker_node")
@EqualsAndHashCode(callSuper = true)
public class WorkerNode extends WorkerNodeEntity {
    @TableId
    private long id;

    public static WorkerNode convert(WorkerNodeEntity entity) {
        return BeanUtil.copyProperties(entity, new WorkerNode());
    }
}
