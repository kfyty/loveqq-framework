package com.kfyty.database.generator.info;

import lombok.Data;

/**
 * 功能描述: 字段结构信息
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/12 17:17
 * @since JDK 1.8
 */
@Data
public class AbstractFieldStructInfo {
    protected String tableName;
    protected String field;
    protected String fieldType;
    protected String primaryKey;
    protected String nullable;
    protected String fieldComment;

    public boolean primaryKey() {
        return Boolean.parseBoolean(primaryKey);
    }

    public boolean nullable() {
        return Boolean.parseBoolean(nullable);
    }
}
