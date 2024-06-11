package com.kfyty.loveqq.framework.codegen.info;

import lombok.Data;
import lombok.SneakyThrows;

/**
 * 功能描述: 字段结构信息
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/12 17:17
 * @since JDK 1.8
 */
@Data
public class AbstractFieldStructInfo implements Cloneable {
    /**
     * 表名
     */
    protected String tableName;

    /**
     * 字段名
     */
    protected String field;

    /**
     * 字段类型
     */
    protected String fieldType;

    /**
     * 对应的 jdbcType
     */
    protected String jdbcType;

    /**
     * 是否是主键
     */
    protected boolean primaryKey;

    /**
     * 是否可为空
     */
    protected boolean nullable;

    /**
     * 字段注释
     */
    protected String fieldComment;

    @Override
    @SneakyThrows
    public AbstractFieldStructInfo clone() {
        return (AbstractFieldStructInfo) super.clone();
    }
}
