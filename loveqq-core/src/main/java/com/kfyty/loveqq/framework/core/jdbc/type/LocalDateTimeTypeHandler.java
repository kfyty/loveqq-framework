package com.kfyty.loveqq.framework.core.jdbc.type;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/3 11:17
 * @email kfyty725@hotmail.com
 */
public class LocalDateTimeTypeHandler implements TypeHandler<LocalDateTime> {

    @Override
    public void setParameter(PreparedStatement ps, int i, LocalDateTime parameter) throws SQLException {
        if(parameter == null) {
            ps.setNull(i, Types.JAVA_OBJECT);
        } else {
            ps.setObject(i, parameter);
        }
    }

    @Override
    public LocalDateTime getResult(ResultSet rs, String columnName) throws SQLException {
        Date date = rs.getDate(columnName);
        if(date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
