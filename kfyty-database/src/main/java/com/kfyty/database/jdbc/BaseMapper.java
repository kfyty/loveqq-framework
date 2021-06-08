package com.kfyty.database.jdbc;

import com.kfyty.database.jdbc.annotation.Execute;
import com.kfyty.database.jdbc.annotation.Param;
import com.kfyty.database.jdbc.annotation.Query;
import com.kfyty.database.jdbc.sql.DeleteAllProvider;
import com.kfyty.database.jdbc.sql.DeleteByPrimaryKeyProvider;
import com.kfyty.database.jdbc.sql.InsertAllProvider;
import com.kfyty.database.jdbc.sql.InsertProvider;
import com.kfyty.database.jdbc.sql.SelectAllProvider;
import com.kfyty.database.jdbc.sql.SelectByPrimaryKeyProvider;
import com.kfyty.database.jdbc.sql.UpdateAllProvider;
import com.kfyty.database.jdbc.sql.UpdateByPrimaryKeyProvider;

import java.util.List;

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
     * 批量插入数据
     * @param entity 数据
     */
    @Execute(provider = InsertAllProvider.class, value = BASE_MAPPER_SQL)
    void insertAll(@Param(PROVIDER_PARAM_ENTITY) List<T> entity);

    /**
     * 根据主键查询数据
     * @param pk 主键
     * @return 数据
     */
    @Query(provider = SelectByPrimaryKeyProvider.class, value = BASE_MAPPER_SQL)
    T selectByPk(@Param(PROVIDER_PARAM_PK) PrimaryKey pk);

    /**
     * 查询所有数据
     * @return 数据
     */
    @Query(provider = SelectAllProvider.class, value = BASE_MAPPER_SQL)
    List<T> selectAll();

    /**
     * 根据主键更新数据
     * @param entity 数据
     */
    @Execute(provider = UpdateByPrimaryKeyProvider.class, value = BASE_MAPPER_SQL)
    void updateByPk(@Param(PROVIDER_PARAM_ENTITY) T entity);

    /**
     * 根据主键批量更新数据
     * @param entity 数据
     */
    @Execute(provider = UpdateAllProvider.class, value = BASE_MAPPER_SQL)
    void updateAll(@Param(PROVIDER_PARAM_ENTITY) List<T> entity);

    /**
     * 根据主键删除数据
     * @param pk 主键
     */
    @Execute(provider = DeleteByPrimaryKeyProvider.class, value = BASE_MAPPER_SQL)
    void deleteByPk(@Param(PROVIDER_PARAM_PK) PrimaryKey pk);

    /**
     * 删除所有数据
     */
    @Execute(provider = DeleteAllProvider.class, value = BASE_MAPPER_SQL)
    void deleteAll();
}
