package dev.eduplay.mains;

import dev.eduplay.services.UserService;
import dev.eduplay.tools.MyDataBase;

public class Main {
    public static void main(String[] args){
        UserService us = new UserService();
        System.out.println(us);

    }
}
