package com.kfyty.support.jdbc.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.List;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/3 11:17
 * @email kfyty725@hotmail.com
 */
public class ShortTypeHandler implements TypeHandler<Short> {

    @Override
    public List<Class<?>> supportTypes() {
        return Collections.singletonList(short.class);
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, Short parameter) throws SQLException {
        if(parameter == null) {
            ps.setNull(i, Types.SMALLINT);
        } else {
            ps.setShort(i, parameter);
        }
    }

    @Override
    public Short getResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getShort(columnName);
    }
}
