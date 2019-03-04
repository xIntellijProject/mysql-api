package me.phil.mysql.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.sql.Connection;

public class TableCreatedEvent extends Event
{

    private static HandlerList handlerList = new HandlerList();

    private String tableName;
    private Connection connection;

    public TableCreatedEvent(String tableName, Connection connection)
    {
        this.tableName = tableName;
        this.connection = connection;
    }

    public static HandlerList getHandlerList()
    {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlerList;
    }
}