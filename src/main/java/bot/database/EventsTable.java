package bot.database;


import bot.inputData.Event;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.*;

/**
 * Класс для работы с таблицей events
 */
public class EventsTable
{
    /**
     * Иницализация коннектора к базе и шаблонов запросов
     * @param dataSource Пул коннекторов к БД
     */
    static void initialize(ComboPooledDataSource dataSource)
    {
        EventsTable.dataSource = dataSource;
//        // Инициализация шаблонов запросов
//        insertPS = connection.prepareStatement();
//
//        getEventsPS = new PreparedStatement[5];
//        for (int i = 0; i < 5; i++)
//        {
//            getEventsPS[i] = connection.prepareStatement("SELECT  name, description, university, type, photo, refference, DATE(datetime) date, TIME(datetime) time, place " +
//                    "FROM events " +
//                    "WHERE checked = TRUE && university = ? && (datetime >= NOW()) " +
//                    "ORDER BY datetime");
//        }
    }

    /**
     * Регистрация нового мероприятия
     * @param parameters Параметры мероприятия
     * @throws SQLException Были переданы некорректные параметры
     */
    public static void addEvent(String... parameters) throws SQLException
    {
        if (parameters.length != Event.PARAM_NUMBER)
        {
            throw new SQLException("Были переданы не все параметры");
        }

        try (Connection connection = dataSource.getConnection())
        {
            PreparedStatement insertPS = connection.prepareStatement(INSERT);

            int i;
            for (i = 0; i < parameters.length - 3; i++)
            {
                insertPS.setString(i + 1, parameters[i]);
            }

            // Вставка даты и времени
            insertPS.setString(i + 1, parameters[i] + " " + parameters[i + 1]);

            // Место
            i++;
            insertPS.setString(i + 1, parameters[i + 1]);

            insertPS.execute();
        }
    }

    /**
     * Получение списка актуальных мероприятий для университета
     * @param university Идентификатор университета
     * @return Список мероприятий, null если ошибка
     */
    public static ResultSet getEvents(int university, Connection connection)
    {
        try
        {
            PreparedStatement getEventsPS = connection.prepareStatement(GET_EVENTS);
            getEventsPS.setInt(1, university);

            return getEventsPS.executeQuery();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static void deleteOldEvents(){

    }


    private static ComboPooledDataSource dataSource;

    // Шаблоны запросов
    private static final String INSERT =            // добавление нового мероприятия
            "INSERT INTO events (name, description, university, type, photo, refference, datetime, place) " +
            "VALUES (?, ?, " +
            "(SELECT id FROM university_list WHERE name = ?), " +
            "(SELECT id FROM event_type_list WHERE name = ?), " +
            "?, ?, ?, ?)";

    private static final String GET_EVENTS =        // получение мероприятий из одного университета
            "SELECT  name, description, university, type, photo, refference, DATE(datetime) date, TIME(datetime) time, place " +
            "FROM events " +
            "WHERE checked = TRUE && university = ? && (datetime >= NOW()) " +
            "ORDER BY datetime";
}