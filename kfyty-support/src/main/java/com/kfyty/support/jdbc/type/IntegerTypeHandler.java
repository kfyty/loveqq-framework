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
public class IntegerTypeHandler implements TypeHandler<Integer> {

    @Override
    public List<Class<?>> supportTypes() {
        return Collections.singletonList(int.class);
    }

    @Override
    public void doSetParameter(PreparedStatement ps, int i, Integer parameter) throws SQLException {
        if(parameter == null) {
            ps.setNull(i, Types.INTEGER);
        } else {
            ps.setInt(i, parameter);
        }
    }

    @Override
    public Integer getResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getInt(columnName);
    }
}
