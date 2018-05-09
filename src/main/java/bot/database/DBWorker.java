package bot.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.sql.*;

/**
 * Подключение к базе данных
 */
public class DBWorker
{
    /**
     * Иницализация подключения к базе данных
     * @throws PropertyVetoException Если не удалось подключиться
     */
    public static void initialize() throws PropertyVetoException
    {
        // Настройка пула соединений к БД
        dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass("com.mysql.jdbc.Driver");
        dataSource.setJdbcUrl(URL);
        dataSource.setUser(USER);
        dataSource.setPassword(PASSWORD);
        dataSource.setInitialPoolSize(5);
        dataSource.setMinPoolSize(5);
        dataSource.setMaxPoolSize(5);

        // Определение классов для работы с таблицами базы данных
        UsersTable.initialize(dataSource);
        EventsTable.initialize(dataSource);
        EnumTable.initialize(dataSource);
    }

    /**
     * Получение соединение из пула соединений к БД
     * @return Соединение (его необходимо закрыть после использования)
     */
    public static Connection getConnection()
    {
        Connection connection = null;
        try
        {
            connection = dataSource.getConnection();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return connection;
    }


    private static ComboPooledDataSource dataSource;    // пул потоков

    private static final String URL = "jdbc:mysql://localhost:3306/hse_life_database?characterEncoding=utf8&autoReconnect=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "root";
}