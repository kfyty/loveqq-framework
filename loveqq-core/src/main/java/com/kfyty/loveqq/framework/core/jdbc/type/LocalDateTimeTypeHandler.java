package com.kfyty.loveqq.framework.core.jdbc.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

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
        Timestamp timestamp = rs.getTimestamp(columnName);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
