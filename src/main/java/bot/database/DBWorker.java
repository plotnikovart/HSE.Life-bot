package bot.database;

import java.sql.*;
import java.util.Vector;

import com.mysql.fabric.jdbc.FabricMySQLDriver;


/**
 * Подключение к базе данных
 */
public class DBWorker
{
    /**
     * Иницализация подключения к базе данных
     * @throws SQLException Если не удалось подключиться
     */
    public static void initialize() throws SQLException
    {
        DriverManager.registerDriver(new FabricMySQLDriver());
        connection = DriverManager.getConnection(URL, USER, PASSWORD);

        // Определение классов для работы с таблицами базы данных
        UsersTable.initialize(connection);
        EventsTable.initialize(connection);
        EnumTable.initialize(connection);
    }


    private static Connection connection;

    private static final String URL = "jdbc:mysql://localhost:3306/hse_life_database?characterEncoding=utf8&?autoReconnect=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "root";
}