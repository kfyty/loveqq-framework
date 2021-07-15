package com.kfyty.database.generator.mapper;

import com.kfyty.database.generator.info.AbstractFieldStructInfo;
import com.kfyty.database.generator.info.AbstractTableStructInfo;
import com.kfyty.database.jdbc.annotation.Param;
import com.kfyty.database.jdbc.annotation.Query;

import java.util.List;

/**
 * 功能描述: 数据库映射接口
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/19 14:15:05
 * @since JDK 1.8
 */
public interface AbstractDatabaseMapper {
    @Query("")
    default List<String> findTableList() {
        return null;
    }

    @Query("")
    List<? extends AbstractTableStructInfo> findTableInfos(@Param("databaseName") String dataBaseName);

    @Query("")
    List<? extends AbstractFieldStructInfo> findFieldInfos(@Param("databaseName") String dataBaseName, @Param("tableName")  String tableName);
}
