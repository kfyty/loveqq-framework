package com.kfyty.database.generate.database;

import com.kfyty.database.generate.info.AbstractFieldStructInfo;
import com.kfyty.database.generate.info.AbstractTableStructInfo;
import com.kfyty.database.jdbc.annotation.Param;
import com.kfyty.database.jdbc.annotation.Query;
import com.kfyty.database.jdbc.annotation.SubQuery;

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
    List<? extends AbstractTableStructInfo> findTableInfos(@Param("dataBaseName") String dataBaseName);

    @Override
    @Query(value = "select distinct t.name tableName, c.name field, ty.name fieldType, case c.is_nullable when 0 then 'false' else 'true' end nullable, e.value fieldComment from information_schema.columns i join sys.tables t on t.name = i.table_name join sys.columns c on c.object_id = t.object_id join sys.types ty on ty.user_type_id = c.user_type_id left join sys.extended_properties e on e.major_id = c.object_id AND e.minor_id = c.column_id where i.table_schema = #{dataBaseName} and i.table_name = #{tableName}",
    subQuery = @SubQuery(value = "SELECT case when sik.id is not null then 'true' else 'false' end primaryKey FROM SYSCOLUMNS c left join SYSOBJECTS so1 on so1.parent_obj = c.id AND so1.xtype = 'PK' left join SYSINDEXKEYS sik on c.colid = sik.colid and sik.id = c.id left join SYSOBJECTS so2 on so2.id=c.id where so2.name = #{tableName} and c.name = #{field}", paramField = {"tableName", "field"}, mapperField = {"tableName", "field"}, returnField = "primaryKey"))
    List<? extends AbstractFieldStructInfo> findFieldInfos(@Param("dataBaseName") String dataBaseName, @Param("tableName")  String tableName);
}
