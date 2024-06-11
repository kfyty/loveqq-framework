package com.kfyty.loveqq.framework.core.jdbc.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/3 11:17
 * @email kfyty725@hotmail.com
 */
public class TimestampTypeHandler implements TypeHandler<Timestamp> {

    @Override
    public void setParameter(PreparedStatement ps, int i, Timestamp parameter) throws SQLException {
        if(parameter == null) {
            ps.setNull(i, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(i, parameter);
        }
    }

    @Override
    public Timestamp getResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getTimestamp(columnName);
    }
}
