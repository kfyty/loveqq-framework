package com.kfyty.support.jdbc.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/3 11:17
 * @email kfyty725@hotmail.com
 */
public class InstantTypeHandler implements TypeHandler<Instant> {

    @Override
    public void doSetParameter(PreparedStatement ps, int i, Instant parameter) throws SQLException {
        if(parameter == null) {
            ps.setNull(i, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(i, Timestamp.from(parameter));
        }
    }

    @Override
    public Instant getResult(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        return timestamp == null ? null : timestamp.toInstant();
    }
}
