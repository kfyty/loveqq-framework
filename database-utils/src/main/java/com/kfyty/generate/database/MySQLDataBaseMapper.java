package com.kfyty.generate.database;

import com.kfyty.generate.info.AbstractDataBaseInfo;
import com.kfyty.generate.info.AbstractTableInfo;
import com.kfyty.jdbc.annotation.Param;
import com.kfyty.jdbc.annotation.Query;

import java.util.List;

/**
 * 功能描述: mysql 数据库映射
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/19 14:17:49
 * @since JDK 1.8
 */
public interface MySQLDataBaseMapper extends AbstractDataBaseMapper {
    @Override
    @Query("select table_schema dataBaseName, table_name, table_comment from information_schema.tables where table_schema = #{dataBaseName}")
    List<? extends AbstractDataBaseInfo> findDataBaseInfo(@Param("dataBaseName") String dataBaseName);

    @Override
    @Query("select table_name, column_name field, data_type fieldType, column_comment fieldComment from information_schema.COLUMNS where table_schema = #{dataBaseName} and table_name = #{tableName}")
    List<? extends AbstractTableInfo> findTableInfo(@Param("dataBaseName") String dataBaseName, @Param("tableName") String tableName);
}
