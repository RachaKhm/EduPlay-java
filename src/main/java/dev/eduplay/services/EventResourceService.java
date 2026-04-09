package dev.eduplay.services;

import dev.eduplay.entities.EventResource;
import dev.eduplay.tools.MyDataBase;

import java.sql.Connection;

public class EventResourceService {
    Connection cn;
    public EventResourceService() {
        cn = MyDataBase.getInstance().getCnx();
    }


}
