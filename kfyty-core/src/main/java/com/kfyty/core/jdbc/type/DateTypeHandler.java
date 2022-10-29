package com.kfyty.core.jdbc.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/3 11:17
 * @email kfyty725@hotmail.com
 */
public class DateTypeHandler implements TypeHandler<Date> {

    @Override
    public void setParameter(PreparedStatement ps, int i, Date parameter) throws SQLException {
        if(parameter == null) {
            ps.setNull(i, Types.DATE);
        } else {
            ps.setTimestamp(i, new Timestamp(parameter.getTime()));
        }
    }

    @Override
    public Date getResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getDate(columnName);
    }
}
