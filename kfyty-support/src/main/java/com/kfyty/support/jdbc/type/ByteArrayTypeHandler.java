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
public class ByteArrayTypeHandler implements TypeHandler<byte[]> {

    @Override
    public void setParameter(PreparedStatement ps, int i, byte[] parameter) throws SQLException {
        if(parameter == null) {
            ps.setNull(i, Types.BINARY);
        } else {
            ps.setBytes(i, parameter);
        }
    }

    @Override
    public byte[] getResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getBytes(columnName);
    }
}
