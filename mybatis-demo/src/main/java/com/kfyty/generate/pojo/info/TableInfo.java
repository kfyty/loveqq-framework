package com.kfyty.generate.pojo.info;

import lombok.Data;

/**
 * 功能描述: 数据表信息
 *
 * @author zhangkun@wisdombud.com
 * @date 2019/8/12 17:17
 * @since JDK 1.8
 */
@Data
public class TableInfo {
    private String tableName;
    private String field;
    private String fieldType;
    private String fieldComment;
}
