package me.phil.mysql.api.database;

import lombok.Getter;
import me.phil.mysql.api.MySQLAPI;

import java.sql.*;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;

@Getter
public final class DataBaseManager {

    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private Connection connection;

    public DataBaseManager(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public final void createTable(String tableName, String columnNames) {
        String query = MessageFormat.format("CREATE TABLE IF NOT EXISTS {0}({1});", tableName, columnNames);
        this.update(query);
    }

    public final void getValue(String tableName, String key, Object value, String columnName, Consumer<Object> consumer) {
        CompletableFuture.runAsync(() -> {
            if (this.isConnected()) {
                try {
                    String s = "SELECT * FROM " + tableName + " WHERE " + key + "='" + value + "';";
                    PreparedStatement statement = this.connection.prepareStatement(s);
                    ResultSet resultSet = statement.executeQuery(s);
                    Object object = null;
                    while (resultSet.next())
                        object = resultSet.getObject(columnName);
                    consumer.accept(object);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public final void connect() {
        String url = MessageFormat.format("jdbc:mysql://{0}:{1}/{2}", this.host, String.valueOf(this.port).replace(".", ""), this.database);
        url += "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&autoReconnect=true";
        try {
            this.connection = DriverManager.getConnection(url, username, password);
            MySQLAPI.getConnections().add(this);
            String rawUrl = MessageFormat.format("jdbc:mysql://{0}:{1}/{2}", this.host, String.valueOf(this.port).replace(".", ""), this.database);
            MySQLAPI.getPlugin(MySQLAPI.class).getLogger().log(Level.INFO, "Successfully connected to \"" + rawUrl + "\"!");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public final void connect(Runnable runnable) {
        String url = MessageFormat.format("jdbc:mysql://{0}:{1}/{2}", this.host, String.valueOf(this.port).replace(".", ""), this.database);
        url += "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&autoReconnect=true";
        try {
            this.connection = DriverManager.getConnection(url, username, password);
            MySQLAPI.getConnections().add(this);
            runnable.run();
            String rawUrl = MessageFormat.format("jdbc:mysql://{0}:{1}/{2}", this.host, String.valueOf(this.port).replace(".", ""), this.database);
            MySQLAPI.getPlugin(MySQLAPI.class).getLogger().log(Level.INFO, "Successfully connected to \"" + rawUrl + "\"!");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    public final void disconnect(Runnable runnable) {
        if (this.connection != null) {
            try {
                this.connection.close();
                MySQLAPI.getConnections().remove(this);
                runnable.run();
                String rawUrl = MessageFormat.format("jdbc:mysql://{0}:{1}/{2}", this.host, String.valueOf(this.port).replace(".", ""), this.database);
                MySQLAPI.getPlugin(MySQLAPI.class).getLogger().log(Level.INFO, "Disconnected from \"" + rawUrl + "\"!");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            this.connection = null;
        }
    }

    public final void disconnect() {
        if (this.connection != null) {
            try {
                this.connection.close();
                MySQLAPI.getConnections().remove(this);
                String rawUrl = MessageFormat.format("jdbc:mysql://{0}:{1}/{2}", this.host, String.valueOf(this.port).replace(".", ""), this.database);
                MySQLAPI.getPlugin(MySQLAPI.class).getLogger().log(Level.INFO, "Disconnected from \"" + rawUrl + "\"!");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            this.connection = null;
        }
    }

    public final boolean isConnected() {
        return connection != null;
    }

    public final void update(String query) {
        CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement statement = this.connection.prepareStatement(query);
                statement.executeUpdate(query);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    public final void query(String query, Consumer<ResultSet> consumer) {
        CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement statement = this.connection.prepareStatement(query);
                consumer.accept(statement.executeQuery(query));
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }
}