package com.kfyty.loveqq.framework.core.jdbc.type;

import java.math.BigDecimal;
import java.math.BigInteger;
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
public class BigIntegerTypeHandler implements TypeHandler<BigInteger> {

    @Override
    public void setParameter(PreparedStatement ps, int i, BigInteger parameter) throws SQLException {
        if(parameter == null) {
            ps.setNull(i, Types.DECIMAL);
        } else {
            ps.setBigDecimal(i, new BigDecimal(parameter));
        }
    }

    @Override
    public BigInteger getResult(ResultSet rs, String columnName) throws SQLException {
        BigDecimal decimal = rs.getBigDecimal(columnName);
        return decimal == null ? null : decimal.toBigInteger();
    }
}
