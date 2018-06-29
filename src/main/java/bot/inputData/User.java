package bot.inputData;

import bot.database.UsersTable;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс, содержащий пользователей, которые вводят данные в реальном времени
 */
class Users
{
    /**
     * Установка университета пользователю
     * @param university Название университета
     * @param userId     Идентификатор пользователя
     */
    static void setUniversity(String university, long userId)
    {
        findUser(userId).setUniversity(university);
    }

    /**
     * Установка мероприятия
     * @param event  Название типа мероприятия
     * @param userId Идентификатор пользователя
     * @return Список всех мероприятий пользователя
     */
    static String setEvents(String event, long userId)
    {
        User user = findUser(userId);
        user.setEvents(event);

        return user.getEventsInfo();
    }

    /**
     * Установка времени для пользователя
     * @param time   Время
     * @param userId Идентификатор пользователя
     */
    static void setTime(String time, long userId)
    {
        findUser(userId).setTime(time);
    }

    /**
     * Загрузка пользователя в БД
     * @param userId Идентификатор пользователя
     * @throws SQLException Если были введены некорректные данные
     */
    static void downloadUserToDatabase(long userId) throws SQLException
    {
        User user = findUser(userId);
        user.downloadToDatabase();

        // Если сохранение прошло удачно, то удаляем пользователя из временного хранилища
        users.remove(userId);
    }

    /**
     * Удаление пользователя из временного хранилища
     * @param userId Идентификатор пользователя
     */
    static void deleteUser(long userId)
    {
        users.remove(userId);
    }

    /**
     * Получение информации о пользователе
     * @param userId Идентификатор пользователя
     * @return Университет, тематики, время
     */
    static String getUserInfo(long userId)
    {
        User user = findUser(userId);

        return user.getInfo();
    }

    /**
     * Поиск пользователя, если его нет, то создание нового
     * @param userId Идентификатор пользователя
     * @return Ссылка на пользователя
     */
    private static User findUser(long userId)
    {
        User user = users.get(userId);

        if (user == null)
        {
            user = new User(userId);
            users.put(userId, user);
        }

        return user;
    }

    // Временное хранилище пользователей
    private static ConcurrentHashMap<Long, User> users = new ConcurrentHashMap<>();
}

/**
 * Класс, представляющий аккаунт пользователя
 */
class User
{
    /**
     * Создание пользователя со стандартными настройками
     * @param userId Идентификатор пользователя
     */
    User(long userId)
    {
        this.userId = userId;
        university = "НИУ ВШЭ (Москва)";
        events = new LinkedList<>();
        time = "10:00";
    }

    /**
     * Установка университета
     * @param university Название университета
     */
    void setUniversity(String university)
    {
        this.university = university;
    }

    /**
     * Добавление мероприятия в список
     * @param event Название тематики
     */
    void setEvents(String event)
    {
        // Удаляем одинаковые мероприятия
        Iterator<String> i = events.iterator();
        while (i.hasNext())
        {
            String s = i.next();
            if (s.equals(event))
            {
                i.remove();
                return;
            }
        }

        events.add(event);
    }

    /**
     * Установка времени
     * @param time Время
     */
    void setTime(String time)
    {
        this.time = time;
    }

    /**
     * Получение информации о пользователе
     * @return Университет, темы мероприятий, время
     */
    String getInfo()
    {
        return "*Университет:* " + university +
                "\n*Приоритетные темы мероприятий:* " + getEventsInfo() +
                "\n*Время для получения подборки:* " + time;
    }

    /**
     * Получение информации об мероприятиях
     * @return Перечисление всех мероприятий
     */
    String getEventsInfo()
    {
        String eventsInfo = "";
        for (String event : events)
        {
            eventsInfo += event + ", ";
        }

        return eventsInfo.length() == 0 ? "-" : eventsInfo.substring(0, eventsInfo.length() - 2);
    }

    /**
     * Загрузка пользователя в БД
     * @throws SQLException Были введены несуществующие параметры
     */
    void downloadToDatabase() throws SQLException
    {
        UsersTable.addUser(userId, university, time, events);
    }

    private long userId;            // идентификатор пользователя
    private String university;      // университет пользователя
    private List<String> events;    // список тематик мероприятий
    private String time;            // время для получения подборки
}