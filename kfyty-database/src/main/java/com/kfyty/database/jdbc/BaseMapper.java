package com.kfyty.database.jdbc;

import com.kfyty.database.jdbc.annotation.Execute;
import com.kfyty.database.jdbc.annotation.Param;
import com.kfyty.database.jdbc.annotation.Query;
import com.kfyty.database.jdbc.sql.DeleteProvider;
import com.kfyty.database.jdbc.sql.InsertProvider;
import com.kfyty.database.jdbc.sql.SelectProvider;
import com.kfyty.database.jdbc.sql.UpdateProvider;

import java.util.List;

import static com.kfyty.database.jdbc.sql.dialect.AbstractProvider.PROVIDER_PARAM_ENTITY;
import static com.kfyty.database.jdbc.sql.dialect.AbstractProvider.PROVIDER_PARAM_PK;

/**
 * 描述: 数据库基础操作
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
    int insert(@Param(PROVIDER_PARAM_ENTITY) T entity);

    /**
     * 批量插入数据
     * @param entity 数据
     */
    @Execute(provider = InsertProvider.class, value = BASE_MAPPER_SQL)
    int insertBatch(@Param(PROVIDER_PARAM_ENTITY) List<T> entity);

    /**
     * 根据主键查询数据
     * @param pk 主键
     * @return 数据
     */
    @Query(provider = SelectProvider.class, value = BASE_MAPPER_SQL)
    T selectByPk(@Param(PROVIDER_PARAM_PK) PrimaryKey pk);

    /**
     * 根据主键批量查询数据
     * @param pks 主键
     * @return 数据
     */
    @Query(provider = SelectProvider.class, value = BASE_MAPPER_SQL)
    List<T> selectByPks(@Param(PROVIDER_PARAM_PK) List<PrimaryKey> pks);

    /**
     * 查询所有数据
     * @return 数据
     */
    @Query(provider = SelectProvider.class, value = BASE_MAPPER_SQL)
    List<T> selectAll();

    /**
     * 根据主键更新数据
     * @param entity 数据
     */
    @Execute(provider = UpdateProvider.class, value = BASE_MAPPER_SQL)
    int updateByPk(@Param(PROVIDER_PARAM_ENTITY) T entity);

    /**
     * 根据主键批量更新数据
     * @param entity 数据
     */
    @Execute(provider = UpdateProvider.class, value = BASE_MAPPER_SQL)
    int updateBatch(@Param(PROVIDER_PARAM_ENTITY) List<T> entity);

    /**
     * 根据主键删除数据
     * @param pk 主键
     */
    @Execute(provider = DeleteProvider.class, value = BASE_MAPPER_SQL)
    int deleteByPk(@Param(PROVIDER_PARAM_PK) PrimaryKey pk);

    /**
     * 根据主键批量删除数据
     * @param pks 主键
     */
    @Execute(provider = DeleteProvider.class, value = BASE_MAPPER_SQL)
    int deleteByPks(@Param(PROVIDER_PARAM_PK) List<PrimaryKey> pks);

    /**
     * 删除所有数据
     */
    @Execute(provider = DeleteProvider.class, value = BASE_MAPPER_SQL)
    int deleteAll();
}
