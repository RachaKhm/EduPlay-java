package dev.eduplay.services;

import dev.eduplay.tools.MyDataBase;

import java.sql.Connection;

public class UserService {
    Connection cn;
    public UserService(){
        cn = MyDataBase.getInstance().getCnx();
    }
}
