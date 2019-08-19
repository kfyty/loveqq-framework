package com.kfyty.generate.pojo.info;

import lombok.Data;

/**
 * 功能描述: 数据表信息
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/12 17:17
 * @since JDK 1.8
 */
@Data
public class AbstractTableInfo {
    protected String tableName;
    protected String field;
    protected String fieldType;
    protected String fieldComment;
}
