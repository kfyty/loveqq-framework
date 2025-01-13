package com.kfyty.loveqq.framework.codegen.mapper;

import com.kfyty.loveqq.framework.codegen.info.AbstractFieldStructInfo;
import com.kfyty.loveqq.framework.codegen.info.AbstractTableStructInfo;
import com.kfyty.loveqq.framework.data.korm.annotation.Param;
import com.kfyty.loveqq.framework.data.korm.annotation.Query;

import java.util.List;

/**
 * 功能描述: mysql 数据库映射
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/19 14:17:49
 * @since JDK 1.8
 */
public interface MySQLDatabaseMapper extends DatabaseMapper {
    @Override
    @Query("select table_schema databaseName, table_name, table_comment from information_schema.tables where table_schema = #{databaseName} and table_name = #{tableName}")
    AbstractTableStructInfo findTableInfo(@Param("databaseName") String dataBaseName, @Param("tableName") String tableName);

    @Override
    @Query("select table_schema databaseName, table_name, table_comment from information_schema.tables where table_schema = #{databaseName}")
    List<? extends AbstractTableStructInfo> findTableInfos(@Param("databaseName") String databaseName);

    @Override
    @Query("select table_name, column_name field, data_type fieldType, if(column_key = 'PRI', 'true', 'false') primaryKey, if(is_nullable = 'YES', 'true', 'false') nullable, column_comment fieldComment from information_schema.COLUMNS where table_schema = #{databaseName} and table_name = #{tableName}")
    List<? extends AbstractFieldStructInfo> findFieldInfos(@Param("databaseName") String databaseName, @Param("tableName") String tableName);
}
