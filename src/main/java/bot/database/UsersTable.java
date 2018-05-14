package bot.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.*;
import java.util.List;

/**
 * Класс для работы с таблицей users и users_events
 */
public class UsersTable
{
    /**
     * Иницализация коннектора к базе
     * @param dataSource Пул соединений к БД
     */
    static void initialize(ComboPooledDataSource dataSource)
    {
        UsersTable.dataSource = dataSource;
    }

    /**
     * Добавление нового пользователя в базу или изменение текущего
     * @param id         Уникальный идентификатор пользователя в Telegram
     * @param university Название университета
     * @param time       Время
     * @param events     Список мероприятий
     * @throws SQLException Были переданы некорректные параметры, ошибка подключения к БД
     */
    public static void addUser(long id, String university, String time, List<String> events) throws SQLException
    {
        try (Connection connection = dataSource.getConnection())
        {
            System.out.println(Thread.currentThread());

            // Если пользователь уже есть в БД
            if (isContained(id, connection))
            {
                // Обновляем данные пользователя
                PreparedStatement updatePS = connection.prepareStatement(UPDATE);
                updatePS.setString(1, university);
                updatePS.setString(2, time);
                updatePS.setLong(3, id);

                updatePS.execute();

                // Очистка мероприятий пользователя
                PreparedStatement deleteEventsPS = connection.prepareStatement(DELETE_EVENTS);
                deleteEventsPS.setLong(1, id);

                deleteEventsPS.execute();
            }
            else
            {
                // Вставляем нового пользователя
                PreparedStatement insertPS = connection.prepareStatement(INSERT);
                insertPS.setLong(1, id);
                insertPS.setString(2, university);
                insertPS.setString(3, time);

                insertPS.execute();
            }

            // Вставляем мероприятия пользователя
            PreparedStatement insertEventsPS = connection.prepareStatement(INSERT_EVENTS);
            for (String event : events)
            {
                insertEventsPS.setLong(1, id);
                insertEventsPS.setString(2, event);

                insertEventsPS.execute();
            }
        }
    }

    /**
     * Добавление пользователя со стандартными настройками.
     * Если такой пользователь есть, то не будем его добавлять
     * @param id Идентификатор пользователя
     */
    public static void addDefaultUser(long id)
    {
        try (Connection connection = dataSource.getConnection())
        {
            // Если такого пользователя нет, то вставляем его
            if (!isContained(id, connection))
            {
                // Вставляем нового пользователя
                PreparedStatement insertPS = connection.prepareStatement(INSERT);
                insertPS.setLong(1, id);
                insertPS.setString(2, "НИУ ВШЭ (Москва)");
                insertPS.setString(3, "10:00");

                insertPS.execute();
            }
        }
        catch (SQLException e)  // Если ошибка, то ничего не делаем
        {
            e.printStackTrace();
        }
    }

    /**
     * Получение списка пользователей с их мероприятиями
     * @param university Идентификатор университета
     * @param time       Идентификатор времени для получения
     * @param connection Коннектор к БД, нужен для параллельной работы
     * @return Пользователи и их мероприятия
     */
    public static ResultSet getUsersEventsType(int university, int time, Connection connection)
    {
        try
        {
            PreparedStatement getUsersEventsTypePS = connection.prepareStatement(GET_USERS_EVENTS_TYPE);
            getUsersEventsTypePS.setInt(1, university);
            getUsersEventsTypePS.setInt(2, time);
            getUsersEventsTypePS.setInt(3, university);
            getUsersEventsTypePS.setInt(4, time);

            return getUsersEventsTypePS.executeQuery();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Проверка, содержится ли пользователь в базе
     * @param id Уникальный идентификатор пользователя в Telegram
     * @return Содержится или нет
     * @throws SQLException Ошибка обращения к БД
     */
    private static boolean isContained(long id, Connection connection) throws SQLException
    {
        boolean contains = false;
        PreparedStatement preparedStatement = connection.prepareStatement(CHECK);
        preparedStatement.setLong(1, id);

        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next())
        {
            contains = true;
        }

        return contains;
    }


    private static ComboPooledDataSource dataSource;            // пул коннекторов к БД

    // Шаблоны запросов
    private static final String CHECK =                         // проверка, содержится ли пользователь в базе
            "SELECT user_id FROM users WHERE user_id = ?";
    private static final String UPDATE =                        // обновление университета и времени
            "UPDATE users SET " +
                    "university = (SELECT id FROM university_list u WHERE u.name = ?), " +
                    "time = (SELECT id FROM time_list t WHERE t.time = ?)  " +
                    "WHERE user_id = ?";
    private static final String INSERT =                         // вставка id, университета, времени
            "INSERT INTO users (user_id, university, time) " +
                    "VALUES (?, " +
                    "(SELECT id FROM university_list u WHERE u.name = ?), " +
                    "(SELECT id FROM time_list t WHERE t.time = ?))";
    private static final String DELETE_EVENTS =                 // удаление мероприятий пользователя
            "DELETE FROM users_events WHERE user_id = ?";
    private static final String INSERT_EVENTS =                 // вставка мероприятий пользователя
            "INSERT INTO users_events (user_id, event) VALUES (?, " +
                    "(SELECT id FROM event_type_list WHERE name = ?))";

    private static final String GET_USERS_EVENTS_TYPE =         // получение пользователей с их мероприятиями
            "(SELECT u.user_id, 0 events" +
                    " FROM users u LEFT JOIN users_events ue ON u.user_id = ue.user_id" +
                    " WHERE ue.event IS NULL && u.university = ? && u.time = ?)" +
                    "UNION ALL" +
                    "(SELECT u.user_id, ue.event" +
                    " FROM users_events ue LEFT JOIN users u ON ue.user_id = u.user_id" +
                    " WHERE u.university = ? && u.time = ?" +
                    " ORDER BY ue.user_id)";
}