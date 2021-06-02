package com.kfyty.database.jdbc;

import com.kfyty.database.jdbc.annotation.Execute;
import com.kfyty.database.jdbc.annotation.Param;
import com.kfyty.database.jdbc.annotation.Query;
import com.kfyty.database.jdbc.sql.DeleteByPrimaryKeyProvider;
import com.kfyty.database.jdbc.sql.InsertProvider;
import com.kfyty.database.jdbc.sql.SelectByPrimaryKeyProvider;
import com.kfyty.database.jdbc.sql.UpdateByPrimaryKeyProvider;

import static com.kfyty.database.jdbc.sql.Provider.PROVIDER_PARAM_ENTITY;
import static com.kfyty.database.jdbc.sql.Provider.PROVIDER_PARAM_PK;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/2 16:20
 * @email kfyty725@hotmail.com
 */
public interface BaseMapper<PrimaryKey, T> {
    String BASE_MAPPER_SQL = "PROVIDER";

    /**
     * 插入一条数据
     * @param entity 数据
     */
    @Execute(provider = InsertProvider.class, value = BASE_MAPPER_SQL)
    void insert(@Param(PROVIDER_PARAM_ENTITY) T entity);

    /**
     * 根据主键查询数据
     * @param pk 主键
     * @return 数据
     */
    @Query(provider = SelectByPrimaryKeyProvider.class, value = BASE_MAPPER_SQL)
    T selectByPk(@Param(PROVIDER_PARAM_PK) PrimaryKey pk);

    /**
     * 根据主键更新数据
     * @param entity 数据
     */
    @Execute(provider = UpdateByPrimaryKeyProvider.class, value = BASE_MAPPER_SQL)
    void updateByPk(@Param(PROVIDER_PARAM_ENTITY) T entity);

    /**
     * 根据主键删除数据
     * @param pk 主键
     */
    @Execute(provider = DeleteByPrimaryKeyProvider.class, value = BASE_MAPPER_SQL)
    void deleteByPk(@Param(PROVIDER_PARAM_PK) PrimaryKey pk);
}
