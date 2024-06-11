package com.kfyty.loveqq.framework.codegen.mapper;

import com.kfyty.loveqq.framework.codegen.info.AbstractFieldStructInfo;
import com.kfyty.loveqq.framework.codegen.info.AbstractTableStructInfo;
import com.kfyty.loveqq.framework.data.jdbc.annotation.Param;
import com.kfyty.loveqq.framework.data.jdbc.annotation.Query;
import com.kfyty.loveqq.framework.data.jdbc.annotation.SubQuery;

import java.util.List;

/**
 * 功能描述: oracle 数据库映射
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/19 14:16:41
 * @since JDK 1.8
 */
public interface OracleDatabaseMapper extends DatabaseMapper {
    @Override
    @Query("select OWNER \"databaseName\", TABLE_NAME, COMMENTS \"tableComment\" from all_tab_comments where OWNER = #{databaseName} and TABLE_NAME = #{tableName}")
    AbstractTableStructInfo findTableInfo(@Param("databaseName") String dataBaseName, @Param("tableName") String tableName);

    @Override
    @Query("select OWNER \"databaseName\", TABLE_NAME, COMMENTS \"tableComment\" from all_tab_comments where OWNER = #{databaseName}")
    List<? extends AbstractTableStructInfo> findTableInfos(@Param("databaseName") String databaseName);

    @Override
    @Query(value = "SELECT t.table_name, t.column_name \"field\", t.data_type \"fieldType\", decode(nullable, 'N', 'false', 'Y', 'true') nullable, c.comments \"fieldComment\" from user_tab_columns t join user_col_comments c on t.table_name = c.table_name and t.column_name = c.column_name where t.table_name = upper(#{tableName})",
    subQuery = @SubQuery(value = "select decode(count(a.constraint_name), 0, 'false', 1, 'true') primaryKey from user_cons_columns a join user_constraints b on a.constraint_name = b.constraint_name where b.constraint_type = 'P' and a.table_name = upper(#{tableName}) and a.column_name = upper(#{field})", paramField = {"tableName", "field"}, mapperField = {"tableName", "field"}, returnField = "primaryKey"))
    List<? extends AbstractFieldStructInfo> findFieldInfos(@Param("databaseName") String databaseName, @Param("tableName") String tableName);
}
