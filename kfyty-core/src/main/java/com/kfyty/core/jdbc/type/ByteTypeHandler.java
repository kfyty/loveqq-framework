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
public class ByteTypeHandler implements TypeHandler<Byte> {

    @Override
    public List<Class<?>> supportTypes() {
        return Collections.singletonList(byte.class);
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, Byte parameter) throws SQLException {
        if(parameter == null) {
            ps.setNull(i, Types.BIT);
        } else {
            ps.setByte(i, parameter);
        }
    }

    @Override
    public Byte getResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getByte(columnName);
    }
}
