package org.springframework.jdbc;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.lang.Nullable;

import java.sql.SQLException;

/**
 * Fatal exception thrown when we can't connect to an RDBMS using JDBC.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class CannotGetJdbcConnectionException extends DataAccessResourceFailureException {
    /**
     * Constructor for CannotGetJdbcConnectionException.
     * @param msg the detail message
     * @since 5.0
     */
    public CannotGetJdbcConnectionException(String msg) {
        super(msg);
    }

    /**
     * Constructor for CannotGetJdbcConnectionException.
     * @param msg the detail message
     * @param ex the root cause SQLException
     */
    public CannotGetJdbcConnectionException(String msg, @Nullable SQLException ex) {
        super(msg, ex);
    }
}
