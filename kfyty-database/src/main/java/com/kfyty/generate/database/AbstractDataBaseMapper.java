package com.kfyty.generate.database;

import com.kfyty.generate.info.AbstractDataBaseInfo;
import com.kfyty.generate.info.AbstractTableInfo;
import com.kfyty.jdbc.annotation.Param;
import com.kfyty.jdbc.annotation.Query;

import java.util.List;

/**
 * 功能描述: 数据库映射接口
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/19 14:15:05
 * @since JDK 1.8
 */
public interface AbstractDataBaseMapper {
    @Query("")
    default List<String> findTableList() {
        return null;
    }

    @Query("")
    List<? extends AbstractDataBaseInfo> findDataBaseInfo(@Param("dataBaseName") String dataBaseName);

    @Query("")
    List<? extends AbstractTableInfo> findTableInfo(@Param("dataBaseName") String dataBaseName,@Param("tableName")  String tableName);
}
