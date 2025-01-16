package com.kfyty.loveqq.framework.core.jdbc.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalTime;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/3 11:17
 * @email kfyty725@hotmail.com
 */
public class LocalTimeTypeHandler implements TypeHandler<LocalTime> {

    @Override
    public void setParameter(PreparedStatement ps, int i, LocalTime parameter) throws SQLException {
        if(parameter == null) {
            ps.setNull(i, Types.JAVA_OBJECT);
        } else {
            ps.setObject(i, parameter);
        }
    }

    @Override
    public LocalTime getResult(ResultSet rs, String columnName) throws SQLException {
        Time time = rs.getTime(columnName);
        return time == null ? null : time.toLocalTime();
    }
}
