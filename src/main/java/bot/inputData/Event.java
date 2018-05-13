package bot.inputData;

import bot.database.EventsTable;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс-контейнер. Хранит в себе мероприятия, которые еще не были загружены в базу данных.
 * После загрузки в базу мероприятие удаляется из контейнера.
 */
class Events
{
    /**
     * Установка параметра для мероприятия
     * @param paramNumber Определяемый параметр
     * @param value       Значение определяемого параметра
     * @param userId      ID пользователя, предлагающего мероприятие
     */
    static void setEventParam(int paramNumber, String value, long userId)
    {
        Event event = findEvent(userId);

        // Выбор уставливаемого параметра
        event.setParam(paramNumber, value);
    }

    /**
     * Загрузка мероприятия в БД
     * @param userId Идентификатор пользователя, предложившего мероприятие
     * @throws SQLException Были указаны не все параметры мероприятия, или неверные
     */
    static void downloadEventToDatabase(long userId) throws SQLException
    {
        Event event = findEvent(userId);
        event.downloadToDatabase();
        events.remove(userId);
    }

    /**
     * Удаление мероприятия из временного контейнера
     * @param userId Идентификатор пользователя, предложившего мероприятие
     */
    static void deleteEvent(long userId)
    {
        events.remove(userId);
    }

    /**
     * Получение информации о мероприятии
     * @param userId Идентификатор пользователя, предложившего мероприятие
     * @return Информация
     */
    static String getEventInfo(long userId)
    {
        Event event = findEvent(userId);

        return event.getInfo();
    }

    /**
     * Поиск мероприятия. Если его нет, то создается новое пустое мероприятие
     * @param userId Идентификатор пользователя, предложившего мероприятие
     * @return Ссылка на мероприятие
     */
    private static Event findEvent(long userId)
    {
        Event event = events.get(userId);

        if (event == null)
        {
            event = new Event();
            events.put(userId, event);
        }

        return event;
    }

    // Временное хранилище мероприятий
    private static ConcurrentHashMap<Long, Event> events = new ConcurrentHashMap<>();
}

/**
 * Класс, представляет собой мероприятие
 */
public class Event
{
    /**
     * Инициализация мероприятия с null полями
     */
    Event()
    {
        params = new String[PARAM_NUMBER];
    }

    /**
     * Инициализирует такими же значениями
     * @param params Значения полей мероприятия
     */
    public Event(String[] params)
    {
        if (params.length == PARAM_NUMBER)
        {
            this.params = params;
        }
    }

    /**
     * Геттер
     * @return Передает поля мероприятия
     */
    public String[] getEventParams()
    {
        return params;
    }

    /**
     * Геттер
     * @return Название мероприятия
     */
    public String getName()
    {
        return params[0];
    }

    /**
     * Геттер
     * @return Тип мероприятия
     */
    public int getType()
    {
        try
        {
            return Integer.parseInt(params[3]);
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    /**
     * Устанавливает значение параметра мероприятия
     * @param paramNumber Номер параметра
     * @param value Значение параметра
     */
    void setParam(int paramNumber, String value)
    {
        params[paramNumber] = value;
    }

    /**
     * Получение информации о мероприятии
     * @return Строка с перечислением полей
     */
    String getInfo()
    {
        StringBuilder info = new StringBuilder();
        for (int i = 0; i < PARAM_NUMBER; i++)
        {
            if (params[i] != null)
            {
                info.append('*').append(paramsName[i]).append(":* ").append(params[i]).append('\n');
            }
        }

        return info.toString();
    }

    /**
     * Загрузка мероприятия в БД
     * @throws SQLException Если не все параметры были заполнены, если были введены некорректные данные
     */
    void downloadToDatabase() throws SQLException
    {
        // Проверка, введены ли все данные (время и место необязательны)
        StringBuilder absentParams = new StringBuilder("\"Вы не добавили:\\n\\n\"");
        for (int i = 0; i < 7; i++)
        {
            if (params[i] == null)
            {
                absentParams.append('*').append(paramsName[i]).append("*\n");
            }
        }

        if (absentParams.toString().equals("Вы не добавили:\n\n"))
        {
            try
            {
                // Добавление в БД
                EventsTable.addEvent(params);
            }
            catch (SQLException e)
            {
                throw new SQLException("Вы ввели некорректные данные, пожалуйста проверьте:\n\n*Университетет* или *Тематика*");
            }
        }
        else
        {
            absentParams.append("\nПожулуйста, добавьте недостающие параметры");
            throw new SQLException(absentParams.toString());
        }
    }

    public static final int PARAM_NUMBER = 9;       // количество параметров
    private String[] params;                        // значения параметров
    private static String[] paramsName = {"Название", "Описание", "Университет", "Тематика", "Ссылка на фотографию",
            "Ссылка на пост", "Дата", "Время", "Место"};
    // 0 - name
    // 1 - description
    // 2 - university
    // 3 - type
    // 4 - photoRef
    // 5 - ref
    // 6 - data
    // 7 - time (необязательный)
    // 8 - place (необязательный)
}