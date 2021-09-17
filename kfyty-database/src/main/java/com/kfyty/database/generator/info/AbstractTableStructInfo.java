package com.kfyty.database.generator.info;

import lombok.Data;

import java.util.List;

/**
 * 功能描述: 表结构信息
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/12 11:18
 * @since JDK 1.8
 */
@Data
public class AbstractTableStructInfo {
    /**
     * 数据库名
     */
    protected String databaseName;

    /**
     * 表名
     */
    protected String tableName;

    /**
     * 表注释
     */
    protected String tableComment;

    /**
     * 字段信息
     */
    protected List<? extends AbstractFieldStructInfo> fieldInfos;
}
