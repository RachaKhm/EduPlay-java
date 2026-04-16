package dev.eduplay.interfaces;

import java.util.List;

public interface IUser <T> {
    void ajouter(T t);
    void modifier(T t);
    void supprimer(T t);
    List<T> getAll();
}
