package com.kfyty.support.jdbc.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * 描述: 类型处理器
 *
 * @author kfyty725
 * @date 2021/6/3 11:14
 * @email kfyty725@hotmail.com
 */
public interface TypeHandler<T> {

    default List<Class<?>> supportTypes() {
        return Collections.emptyList();
    }

    default void setParameter(PreparedStatement ps, int i, Object parameter) throws SQLException {
        this.doSetParameter(ps, i, (T) parameter);
    }

    void doSetParameter(PreparedStatement ps, int i, T parameter) throws SQLException;

    T getResult(ResultSet rs, String columnName) throws SQLException;
}
