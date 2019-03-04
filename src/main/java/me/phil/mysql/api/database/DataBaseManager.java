package me.phil.mysql.api.database;

import lombok.Getter;
import me.phil.mysql.api.MySQLAPI;
import me.phil.mysql.api.event.DatabaseUpdateEvent;
import me.phil.mysql.api.event.SuccessfullyConnectedEvent;
import me.phil.mysql.api.event.SuccessfullyDisconnectedEvent;
import me.phil.mysql.api.event.TableCreatedEvent;
import org.bukkit.Bukkit;

import java.sql.*;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;

@Getter
public final class DataBaseManager
{

    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private Connection connection;

    /**
     * Creates an instance of {@link me.phil.mysql.api.database.DataBaseManager} and initialize all local objects.
     *
     * @param host     the IP that is trying to connect to
     * @param port
     * @param database
     * @param username
     * @param password
     */
    public DataBaseManager(String host, int port, String database, String username, String password)
    {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    /**
     * Creates a table if the connection done successfully.
     *
     * @param tableName   The name of the table
     * @param columnNames The columns of the table; syntax must be written according to MySQL
     */
    public final void createTable(String tableName, String columnNames)
    {
        String query = MessageFormat.format("CREATE TABLE IF NOT EXISTS {0}({1});", tableName, columnNames);
        this.update(query);
        Bukkit.getPluginManager().callEvent(new TableCreatedEvent(tableName, this.connection));
        Bukkit.getPluginManager().callEvent(new DatabaseUpdateEvent(this.connection));
    }

    /**
     * Adds the {@link java.util.function.Consumer<Object>} the information if the connection done successfully
     * and if the information is placed on this section.
     *
     * @param tableName  The Table which is used to get the information
     * @param key        The
     * @param value      The
     * @param columnName The column which is used to get the information
     * @param consumer   Information ads to the {@link java.util.function.Consumer<Object>}
     */
    public final void getValue(String tableName, String key, Object value, String columnName, Consumer<Object> consumer)
    {
        CompletableFuture.runAsync(() ->
        {
            if (this.isConnected())
            {
                try
                {
                    String s = "SELECT * FROM " + tableName + " WHERE " + key + "='" + value + "';";
                    PreparedStatement statement = this.connection.prepareStatement(s);
                    ResultSet resultSet = statement.executeQuery(s);
                    Object object = null;
                    while (resultSet.next())
                        object = resultSet.getObject(columnName);
                    consumer.accept(object);
                } catch (SQLException ex)
                {
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * Connects to the MySQL database.
     */
    public final void connect()
    {
        String url = MessageFormat.format("jdbc:mysql://{0}:{1}/{2}", this.host, String.valueOf(this.port).replace(".", ""), this.database);
        try
        {
            this.connection = DriverManager.getConnection(url + "autoReconnect=true", username, password);
            MySQLAPI.getConnections().add(this);
            MySQLAPI.getPlugin(MySQLAPI.class).getLogger().log(Level.INFO, "Successfully connected to \"" + url + "\"!");
            Bukkit.getPluginManager().callEvent(new SuccessfullyConnectedEvent(this.connection));
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Connects to the MySQL database.
     *
     * @param runnable {@link java.lang.Runnable} will run before adding the connection.
     */
    public final void connect(Runnable runnable)
    {
        String url = MessageFormat.format("jdbc:mysql://{0}:{1}/{2}", this.host, String.valueOf(this.port).replace(".", ""), this.database);
        try
        {
            this.connection = DriverManager.getConnection(url + "&autoReconnect=true", username, password);
            runnable.run();

            MySQLAPI.getConnections().add(this);
            MySQLAPI.getPlugin(MySQLAPI.class).getLogger().log(Level.INFO, "Successfully connected to \"" + url + "\"!");
            Bukkit.getPluginManager().callEvent(new SuccessfullyConnectedEvent(this.connection));
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }


    /**
     * Disconnects to the MySQL database.
     *
     * @param runnable {@link java.lang.Runnable} will run before removing the connection.
     */
    public final void disconnect(Runnable runnable)
    {
        if (this.connection != null)
        {
            try
            {
                this.connection.close();
                MySQLAPI.getConnections().remove(this);
                runnable.run();

                String url = MessageFormat.format("jdbc:mysql://{0}:{1}/{2}", this.host, String.valueOf(this.port).replace(".", ""), this.database);
                MySQLAPI.getPlugin(MySQLAPI.class).getLogger().log(Level.INFO, "Disconnected from \"" + url + "\"!");
                Bukkit.getPluginManager().callEvent(new SuccessfullyDisconnectedEvent(this.connection));
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            }
            this.connection = null;
        }
    }

    /**
     * Disconnects to the MySQL database.
     */
    public final void disconnect()
    {
        if (this.connection != null)
        {
            try
            {
                this.connection.close();
                MySQLAPI.getConnections().remove(this);

                String url = MessageFormat.format("jdbc:mysql://{0}:{1}/{2}", this.host, String.valueOf(this.port).replace(".", ""), this.database);
                MySQLAPI.getPlugin(MySQLAPI.class).getLogger().log(Level.INFO, "Disconnected from \"" + url + "\"!");
                Bukkit.getPluginManager().callEvent(new SuccessfullyDisconnectedEvent(this.connection));
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            }
            this.connection = null;
        }
    }

    /**
     * @return if database is connected.
     */
    public final boolean isConnected()
    {
        return connection != null;
    }

    /**
     * Updates the MySQL database.
     *
     * @param query The query will be added to the {@link java.sql.PreparedStatement}; syntax must be written according to MySQL
     */
    public final void update(String query)
    {
        CompletableFuture.runAsync(() ->
        {
            try
            {
                PreparedStatement statement = this.connection.prepareStatement(query);
                statement.executeUpdate(query);
                Bukkit.getPluginManager().callEvent(new DatabaseUpdateEvent(this.connection));
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        });
    }

    /**
     * Updates the MySQL database and adds the {@link java.util.function.Consumer<ResultSet>} a {@link java.sql.ResultSet}.
     *
     * @param query    The query will be added to the {@link java.sql.PreparedStatement}; syntax must be written according to MySQL
     * @param consumer Information ads to the {@link java.util.function.Consumer<ResultSet>}
     */
    public final void query(String query, Consumer<ResultSet> consumer)
    {
        CompletableFuture.runAsync(() ->
        {
            try
            {
                PreparedStatement statement = this.connection.prepareStatement(query);
                consumer.accept(statement.executeQuery(query));
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        });
    }
}