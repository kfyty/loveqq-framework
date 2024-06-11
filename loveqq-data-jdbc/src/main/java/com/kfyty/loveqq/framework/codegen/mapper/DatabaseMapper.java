package com.kfyty.loveqq.framework.codegen.mapper;

import com.kfyty.loveqq.framework.codegen.info.AbstractFieldStructInfo;
import com.kfyty.loveqq.framework.codegen.info.AbstractTableStructInfo;
import com.kfyty.loveqq.framework.data.jdbc.annotation.Param;
import com.kfyty.loveqq.framework.data.jdbc.annotation.Query;

import java.util.List;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.EMPTY_STRING;

/**
 * 功能描述: 数据库映射接口
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/19 14:15:05
 * @since JDK 1.8
 */
public interface DatabaseMapper {
    @Query(EMPTY_STRING)
    default List<String> findTableList() {
        return null;
    }

    @Query(EMPTY_STRING)
    AbstractTableStructInfo findTableInfo(@Param("databaseName") String dataBaseName, @Param("tableName") String tableName);

    @Query(EMPTY_STRING)
    List<? extends AbstractTableStructInfo> findTableInfos(@Param("databaseName") String dataBaseName);

    @Query(EMPTY_STRING)
    List<? extends AbstractFieldStructInfo> findFieldInfos(@Param("databaseName") String dataBaseName, @Param("tableName") String tableName);
}
