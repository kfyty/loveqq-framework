package com.kfyty.loveqq.framework.core.jdbc.type;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/3 11:17
 * @email kfyty725@hotmail.com
 */
public class LocalDateTypeHandler implements TypeHandler<LocalDate> {

    @Override
    public void setParameter(PreparedStatement ps, int i, LocalDate parameter) throws SQLException {
        if(parameter == null) {
            ps.setNull(i, Types.JAVA_OBJECT);
        } else {
            ps.setObject(i, parameter);
        }
    }

    @Override
    public LocalDate getResult(ResultSet rs, String columnName) throws SQLException {
        Date date = rs.getDate(columnName);
        return date == null ? null : date.toLocalDate();
    }
}
