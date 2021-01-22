package com.kfyty.generate.database;

import com.kfyty.generate.info.AbstractTableStructInfo;
import com.kfyty.generate.info.AbstractFieldStructInfo;
import com.kfyty.jdbc.annotation.Param;
import com.kfyty.jdbc.annotation.Query;
import com.kfyty.jdbc.annotation.SubQuery;

import java.util.List;

/**
 * 功能描述: oracle 数据库映射
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/19 14:16:41
 * @since JDK 1.8
 */
public interface OracleDataBaseMapper extends AbstractDataBaseMapper {
    @Override
    @Query("select OWNER \"dataBaseName\", TABLE_NAME, COMMENTS \"tableComment\" from all_tab_comments where OWNER = #{dataBaseName}")
    List<? extends AbstractTableStructInfo> findTableInfos(@Param("dataBaseName") String dataBaseName);

    @Override
    @Query(value = "SELECT t.table_name, t.column_name \"field\", t.data_type \"fieldType\", decode(nullable, 'N', 'false', 'Y', 'true') nullable, c.comments \"fieldComment\" from user_tab_columns t join user_col_comments c on t.table_name = c.table_name and t.column_name = c.column_name where t.table_name = upper(#{tableName})",
    subQuery = @SubQuery(value = "select decode(count(a.constraint_name), 0, 'false', 1, 'true') primaryKey from user_cons_columns a join user_constraints b on a.constraint_name = b.constraint_name where b.constraint_type = 'P' and a.table_name = upper(#{tableName}) and a.column_name = upper(#{field})", paramField = {"tableName", "field"}, mapperField = {"tableName", "field"}, returnField = "primaryKey"))
    List<? extends AbstractFieldStructInfo> findFieldInfos(@Param("dataBaseName") String dataBaseName, @Param("tableName") String tableName);
}
