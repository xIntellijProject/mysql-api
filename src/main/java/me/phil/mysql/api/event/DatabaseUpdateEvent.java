package me.phil.mysql.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.sql.Connection;

public class DatabaseUpdateEvent extends Event
{

    private static HandlerList handlerList = new HandlerList();

    private Connection connection;

    public DatabaseUpdateEvent(Connection connection)
    {
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