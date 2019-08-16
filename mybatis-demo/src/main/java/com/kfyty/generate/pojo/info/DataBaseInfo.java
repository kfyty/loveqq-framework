package com.kfyty.generate.pojo.info;

import lombok.Data;

import java.util.List;

/**
 * 功能描述: 数据库信息
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/12 11:18
 * @since JDK 1.8
 */
@Data
public class DataBaseInfo {
    private String dataBaseName;
    private String tableName;
    private String tableComment;
    private List<TableInfo> tableInfos;
}
