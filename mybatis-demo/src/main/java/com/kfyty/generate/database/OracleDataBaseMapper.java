package com.kfyty.generate.database;

import com.kfyty.generate.info.AbstractDataBaseInfo;
import com.kfyty.generate.info.AbstractTableInfo;
import com.kfyty.jdbc.annotation.Param;
import com.kfyty.jdbc.annotation.Query;

import java.util.List;

/**
 * 功能描述: oracle 数据库映射
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/19 14:16:41
 * @since JDK 1.8
 */
public interface OracleDataBaseMapper extends DataBaseMapper {
    @Override
    @Query("select OWNER \"dataBaseName\", TABLE_NAME, COMMENTS \"tableComment\" from all_tab_comments where OWNER = #{dataBaseName}")
    List<AbstractDataBaseInfo> findDataBaseInfo(@Param("dataBaseName") String dataBaseName);

    @Override
    @Query("SELECT t.table_name, t.column_name \"field\", t.data_type \"fieldType\", c.comments \"fieldComment\" from user_tab_columns t join user_col_comments c on t.table_name = c.table_name and t.column_name = c.column_name where t.table_name = upper(#{tableName})")
    List<AbstractTableInfo> findTableInfo(@Param("dataBaseName") String dataBaseName, @Param("tableName") String tableName);
}
