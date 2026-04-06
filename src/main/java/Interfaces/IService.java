package Interfaces;

import java.sql.SQLException;
import java.util.List;

public interface IService<T> {

    void add(T entity) throws SQLException;

    void delete(T entity) throws SQLException;

    void update(T entity) throws SQLException;

    List<T> display() throws SQLException;
}
