package me.phil.mysql.api.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.sql.Connection;

public class SuccessfullyDisconnectedEvent extends Event
{

    /*
     * Will get called when connection to database will successfully disconnect.
     */

    private static HandlerList handlerList = new HandlerList();

    @Getter
    private Connection connection;

    public SuccessfullyDisconnectedEvent(Connection connection)
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