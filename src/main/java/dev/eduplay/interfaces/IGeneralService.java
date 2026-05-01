package dev.eduplay.interfaces;

import java.sql.SQLException;
import java.util.List;

public interface IGeneralService<T> {
    void ajouter(T t) throws SQLException;
    void supprimer(T t) throws SQLException;
    int chercher(T t) throws SQLException;
    void modifier(T t) throws SQLException;
    List<T> recuperer() throws SQLException;
}