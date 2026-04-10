package dev.eduplay.interfaces;

import java.util.List;

public interface ICrud<T> {
    void ajouter(T t);
    void modifier(T t);
    void supprimer(int id);
    List<T> afficher();
}