package repository;

import java.sql.SQLException;

public class DatabaseException extends RuntimeException {
    public DatabaseException(SQLException throwables) {
        super(throwables);
    }
}
