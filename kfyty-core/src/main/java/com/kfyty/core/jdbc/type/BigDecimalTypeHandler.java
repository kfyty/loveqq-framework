package com.kfyty.core.jdbc.type;

import java.math.BigDecimal;
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
public class BigDecimalTypeHandler implements TypeHandler<BigDecimal> {

    @Override
    public void setParameter(PreparedStatement ps, int i, BigDecimal parameter) throws SQLException {
        if(parameter == null) {
            ps.setNull(i, Types.DECIMAL);
        } else {
            ps.setBigDecimal(i, parameter);
        }
    }

    @Override
    public BigDecimal getResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getBigDecimal(columnName);
    }
}
