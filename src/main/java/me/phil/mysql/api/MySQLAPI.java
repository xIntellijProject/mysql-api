package me.phil.mysql.api;

import lombok.Getter;
import me.phil.mysql.api.database.DataBaseManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedList;
import java.util.List;

public final class MySQLAPI extends JavaPlugin {

    @Getter
    private static List<DataBaseManager> connections = new LinkedList<>();

    @Override
    public final void onLoad() {
        super.onLoad();
    }

    @Override
    public final void onEnable() {
        super.onEnable();
    }

    @Override
    public final void onDisable() {
        super.onDisable();
        connections.forEach(connection -> {
            if (connection == null) return;
            connection.disconnect();
        });
    }
}