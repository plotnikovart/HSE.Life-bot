package bot.database;

import bot.inputData.Events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.*;
import java.util.Date;

public class EventsTable
{
    /**
     * Иницализация коннектора к базе и шаблонов запросов
     * @param connection Коннектор
     */
    static void initialize(Connection connection) throws SQLException
    {
        // Инициализация шаблонов запросов
        insertPS = connection.prepareStatement("INSERT INTO events (name, description, university, type, photo, refference, date, time, place) " +
                "VALUES (?, ?, " +
                "(SELECT id FROM university_list WHERE name = ?), " +
                "(SELECT id FROM event_type_list WHERE name = ?), " +
                "?, ?, ?, ?, ?)");
    }

    public static void addEvent(String... parameters) throws SQLException
    {
        if (insertPS == null)
            throw new SQLException("Коннектор не определен");

        if (parameters.length != 9)
            throw new SQLException("Были переданы не все параметры");

        for (int i = 0; i < parameters.length; i++)
            insertPS.setString(i + 1, parameters[i]);

        insertPS.execute();
    }

    private static PreparedStatement insertPS;      // добавление нового мероприятия
}
