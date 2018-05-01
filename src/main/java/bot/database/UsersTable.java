package bot.database;

import java.sql.*;
import java.util.List;

/**
 * Класс для работы с таблицей users и users_events
 */
public class UsersTable
{
    /**
     * Иницализация коннектора к базе и шаблонов запросов
     * @param connection Коннектор
     * @throws SQLException Произошла ошибка при формировании запросов
     */
    static void initialize(Connection connection) throws SQLException
    {
        // Инициализация шаблонов запросов
        checkPS = connection.prepareStatement("SELECT user_id FROM users WHERE user_id = ?");

        updatePS = connection.prepareStatement("UPDATE users SET " +
                "university = (SELECT id FROM university_list u WHERE u.name = ?), " +
                "time = (SELECT id FROM time_list t WHERE t.time = ?)  " +
                "WHERE user_id = ?");

        insertPS = connection.prepareStatement("INSERT INTO users (user_id, university, time) " +
                "VALUES (?, " +
                "(SELECT id FROM university_list u WHERE u.name = ?), " +
                "(SELECT id FROM time_list t WHERE t.time = ?))");

        deleteEventsPS = connection.prepareStatement("DELETE FROM users_events WHERE user_id = ?");

        insertEventsPS = connection.prepareStatement("INSERT INTO users_events (user_id, event) VALUES (?, " +
                "(SELECT id FROM event_type_list WHERE name = ?))");

        getUsersEventsTypePS = connection.prepareStatement("(SELECT u.user_id, 0 events" +
                " FROM users u LEFT JOIN users_events ue on u.user_id = ue.user_id" +
                " WHERE ue.event is NULL && u.university = ? && u.time = ?)" +
                "UNION ALL" +
                "(SELECT u.user_id, ue.event" +
                " FROM users_events ue LEFT JOIN users u ON ue.user_id = u.user_id" +
                " WHERE u.university = ? && u.time = ?" +
                " ORDER BY ue.user_id)");
    }

    /**
     * Добавление нового пользователя в базу или изменение текущего
     * @param id         Уникальный идентификатор пользователя в Telegram
     * @param university Название университета
     * @param time       Время
     * @param events     Список мероприятий
     * @throws SQLException Были переданы некорректные параметры
     */
    public static void addUser(long id, String university, String time, List<String> events) throws SQLException
    {
        if (checkPS == null)
        {
            throw new SQLException("Коннектор не определен");
        }

        // Если пользователь уже есть в БД
        if (isContained(id))
        {
            // Обновляем данные пользователя
            updatePS.setString(1, university);
            updatePS.setString(2, time);
            updatePS.setLong(3, id);

            updatePS.execute();

            // Очистка мероприятий пользователя
            deleteEventsPS.setLong(1, id);

            deleteEventsPS.execute();
        }
        else
        {
            // Вставляем нового пользователя
            insertPS.setLong(1, id);
            insertPS.setString(2, university);
            insertPS.setString(3, time);

            insertPS.execute();
        }

        // Вставляем мероприятия пользователя
        for (String event : events)
        {
            insertEventsPS.setLong(1, id);
            insertEventsPS.setString(2, event);

            insertEventsPS.execute();
        }
    }

    /**
     * Добавление пользователя со стандартными настройками.
     * Если такой пользователь есть, то не будем его добавлять
     * @param id Идентификатор пользователя
     */
    public static void addDefaultUser(long id)
    {
        try
        {
            // Если такого пользователя нет, то вставляем его
            if (!isContained(id))
            {
                // Вставляем нового пользователя
                insertPS.setLong(1, id);
                insertPS.setString(2, "НИУ ВШЭ (Москва)");
                insertPS.setString(3, "10:00");

                insertPS.execute();
            }
        }
        catch (SQLException e)  // Если ошибка, то ничего не делаем
        {
        }
    }

    /**
     * Получение списка пользователей с их мероприятиями
     * @param university Идентификатор университета
     * @param time       Идентификатор времени для получения
     * @return Пользователи и их мероприятия
     */
    synchronized public static ResultSet getUsersEventsType(int university, int time)
    {
        try
        {
            getUsersEventsTypePS.setInt(1, university);
            getUsersEventsTypePS.setInt(2, time);
            getUsersEventsTypePS.setInt(3, university);
            getUsersEventsTypePS.setInt(4, time);

            return getUsersEventsTypePS.executeQuery();
        }
        catch (SQLException e)
        {
            return null;
        }
    }

    /**
     * Проверка, содержится ли пользователь в базе
     * @param id Уникальный идентификатор пользователя в Telegram
     * @return Сдержится или нет
     * @throws SQLException Ошибка обращения к БД
     */
    private static boolean isContained(long id) throws SQLException
    {
        boolean contains = false;

        checkPS.setLong(1, id);

        ResultSet resultSet = checkPS.executeQuery();

        if (resultSet.next())
        {
            contains = true;
        }

        return contains;
    }


    // Шаблоны запросов
    private static PreparedStatement checkPS;               // проверка, содержится ли пользователь в базе
    private static PreparedStatement updatePS;              // обновление университета и времени
    private static PreparedStatement insertPS;              // вставка id, университета, времени
    private static PreparedStatement deleteEventsPS;        // удаление мероприятий пользователя
    private static PreparedStatement insertEventsPS;        // вставка мероприятий пользователя

    private static PreparedStatement getUsersEventsTypePS;  // получение пользователей с их мероприятиями
}