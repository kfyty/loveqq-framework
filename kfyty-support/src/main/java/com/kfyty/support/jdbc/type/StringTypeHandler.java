package com.kfyty.support.jdbc.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/3 11:17
 * @email kfyty725@hotmail.com
 */
public class StringTypeHandler implements TypeHandler<String> {

    @Override
    public void doSetParameter(PreparedStatement ps, int i, String parameter) throws SQLException {
        if(parameter == null) {
            ps.setNull(i, Types.VARCHAR);
        } else {
            ps.setString(i, parameter);
        }
    }

    @Override
    public String getResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getString(columnName);
    }
}
