package com.kfyty.core.jdbc.type;

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
public class DoubleTypeHandler implements TypeHandler<Double> {

    @Override
    public List<Class<?>> supportTypes() {
        return Collections.singletonList(double.class);
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, Double parameter) throws SQLException {
        if(parameter == null) {
            ps.setNull(i, Types.DOUBLE);
        } else {
            ps.setDouble(i, parameter);
        }
    }

    @Override
    public Double getResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getDouble(columnName);
    }
}
