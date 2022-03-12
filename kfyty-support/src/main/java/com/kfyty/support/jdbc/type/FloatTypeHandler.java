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
public class FloatTypeHandler implements TypeHandler<Float> {

    @Override
    public List<Class<?>> supportTypes() {
        return Collections.singletonList(float.class);
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, Float parameter) throws SQLException {
        if(parameter == null) {
            ps.setNull(i, Types.FLOAT);
        } else {
            ps.setFloat(i, parameter);
        }
    }

    @Override
    public Float getResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getFloat(columnName);
    }
}
