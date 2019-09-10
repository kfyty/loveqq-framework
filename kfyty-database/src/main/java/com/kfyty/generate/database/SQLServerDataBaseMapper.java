package com.kfyty.generate.database;

import com.kfyty.generate.info.AbstractDataBaseInfo;
import com.kfyty.generate.info.AbstractTableInfo;
import com.kfyty.jdbc.annotation.Param;
import com.kfyty.jdbc.annotation.Query;

import java.util.List;

/**
 * 功能描述: SQLServer 数据库映射
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/5 10:11:12
 * @since JDK 1.8
 */
public interface SQLServerDataBaseMapper extends AbstractDataBaseMapper {
    @Override
    @Query("select distinct i.table_schema dataBaseName, t.name tableName, e.value tableComment from information_schema.columns i join sys.tables t on i.table_name = t.name left join sys.extended_properties e on e.major_id = t.object_id and e.minor_id = 0 where i.table_schema = #{dataBaseName}")
    List<? extends AbstractDataBaseInfo> findDataBaseInfo(@Param("dataBaseName") String dataBaseName);

    @Override
    @Query("select distinct t.name tableName, c.name field, ty.name fieldType, e.value fieldComment from information_schema.columns i join sys.tables t on t.name = i.table_name join sys.columns c on c.object_id = t.object_id join sys.types ty on ty.user_type_id = c.user_type_id left join sys.extended_properties e on e.major_id = c.object_id AND e.minor_id = c.column_id where i.table_schema = #{dataBaseName} and i.table_name = #{tableName}")
    List<? extends AbstractTableInfo> findTableInfo(@Param("dataBaseName") String dataBaseName,@Param("tableName")  String tableName);
}
