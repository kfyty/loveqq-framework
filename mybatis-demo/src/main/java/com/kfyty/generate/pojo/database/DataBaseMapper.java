package com.kfyty.generate.pojo.database;

import com.kfyty.generate.pojo.info.AbstractDataBaseInfo;
import com.kfyty.generate.pojo.info.AbstractTableInfo;

import java.util.List;

/**
 * 功能描述: 数据库映射接口
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/19 14:15:05
 * @since JDK 1.8
 */
public interface DataBaseMapper {

    List<? extends AbstractDataBaseInfo> findDataBaseInfo(String dataBaseName);

    List<? extends AbstractTableInfo> findTableInfo(String dataBaseName, String tableName);
}
