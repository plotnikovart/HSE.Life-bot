package bot.database;


import bot.inputData.Event;

import java.sql.*;

/**
 * Класс для работы с таблицей events
 */
public class EventsTable
{
    /**
     * Иницализация коннектора к базе и шаблонов запросов
     * @param connection Коннектор
     * @throws SQLException Произошла ошибка при формировании запросов
     */
    static void initialize(Connection connection) throws SQLException
    {
        // Инициализация шаблонов запросов
        insertPS = connection.prepareStatement("INSERT INTO events (name, description, university, type, photo, refference, datetime, place) " +
                "VALUES (?, ?, " +
                "(SELECT id FROM university_list WHERE name = ?), " +
                "(SELECT id FROM event_type_list WHERE name = ?), " +
                "?, ?, ?, ?)");

        getEventsPS = connection.prepareStatement("SELECT  name, description, university, type, photo, refference, DATE(datetime) date, TIME(datetime) time, place " +
                "FROM events " +
                "WHERE checked = TRUE && university = ? && (datetime >= NOW()) " +
                "ORDER BY datetime");
    }

    /**
     * Регистрация нового мероприятия
     * @param parameters Параметры мероприятия
     * @throws SQLException Были переданы некорректные параметры
     */
    public static void addEvent(String... parameters) throws SQLException
    {
        if (insertPS == null)
        {
            throw new SQLException("Коннектор не определен");
        }

        if (parameters.length != Event.PARAM_NUMBER)
        {
            throw new SQLException("Были переданы не все параметры");
        }

        int i;
        for (i = 0; i < parameters.length - 3; i++)
            insertPS.setString(i + 1, parameters[i]);

        // Вставка даты и времени
        insertPS.setString(i + 1, parameters[i] + " " + parameters[i + 1]);

        // Место
        i++;
        insertPS.setString(i + 1, parameters[i + 1]);

        insertPS.execute();
    }

    /**
     * Получение списка актуальных мероприятий для университета
     * @param university Идентификатор университета
     * @return Список мероприятий, null если ошибка
     */
    synchronized public static ResultSet getEvents(int university)
    {
        try
        {
            getEventsPS.setInt(1, university);

            return getEventsPS.executeQuery();
        }
        catch (SQLException e)
        {
            return null;
        }

    }

    // Шаблоны запросов
    private static PreparedStatement insertPS;      // добавление нового мероприятия
    private static PreparedStatement getEventsPS;   // получение мероприятий из одного университета
}