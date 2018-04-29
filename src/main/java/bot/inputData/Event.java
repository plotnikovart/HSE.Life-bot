package bot.inputData;

import bot.database.EventsTable;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс-контейнер. Хранит в себе мероприятия, которые еще не были загружены в базу данных.
 * После загрузки в базу мероприятие удаляется отсюда.
 */
class Events
{
    /**
     * Метод определения мероприятия
     * @param type  Определяемый параметр
     * @param param Значение определяемого параметра
     * @param id    ID пользователя, предлагающего мероприятие
     */
    static void setEvent(String type, String param, int id)
    {
        Event event = findEvent(id);

        // Выбор уставливаемого параметра
        switch (type)
        {
            case (Functions.B010):      // название
                event.setName(param);
                break;
            case (Functions.B011):      // опописание
                event.setDescription(param);
                break;
            case (Functions.B012):      // университет
                event.setUniversity(param);
                break;
            case (Functions.B013):      // тематика
                event.setType(param);
                break;
            case (Functions.B014):      // ссылка на фотографию
                event.setPhotoRef(param);
                break;
            case (Functions.B015):      // ссылка на пост
                event.setRef(param);
                break;
            case (Functions.B016):      // дата
                event.setDate(param);
                break;
            case (Functions.B017):      // время
                event.setTime(param);
                break;
            case (Functions.B018):      // место
                event.setPlace(param);
                break;
        }
    }

    static void downloadEventToDatabase(int id) throws SQLException
    {
        Event event = events.get(id);
        if (event == null) throw new SQLException("");

        event.downloadToDatabase();
        events.remove(id);
    }

    static void deleteEvent(int id)
    {
        events.remove(id);
    }

    static String getEventInfo(int id)
    {
        Event event = findEvent(id);

        return event.getInfo();
    }

    private static Event findEvent(int id)
    {
        Event event = events.get(id);

        if (event == null)
        {
            event = new Event();
            events.put(id, event);
        }

        return event;
    }

    private static ConcurrentHashMap<Integer, Event> events = new ConcurrentHashMap<>();
}


// todo подумать насчет downloadToDatabase
public class Event
{
    Event()
    {
        params = new String[PARAM_NUMBER];
    }

    public Event(String[] params)
    {
        if (params.length == PARAM_NUMBER)
        {
            this.params = params;
        }
    }

    String[] getEventDescription()
    {
        return params;
    }

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

    void setName(String name)
    {
        params[0] = name;
    }

    void setDescription(String description)
    {
        params[1] = description;
    }

    void setUniversity(String university)
    {
        params[2] = university;
    }

    void setType(String type)
    {
        params[3] = type;
    }

    void setPhotoRef(String photoRef)
    {
        params[4] = photoRef;
    }

    void setRef(String ref)
    {
        params[5] = ref;
    }

    void setDate(String date)
    {
        params[6] = date;
    }

    void setTime(String time)
    {
        params[7] = time;
    }

    void setPlace(String place)
    {
        params[8] = place;
    }

    String getInfo()
    {
        String info = "*Название:* " + params[0] + "\n" +
                "*Описание:* " + params[1] + "\n" +
                "*Университет:* " + params[2] + "\n" +
                "*Тематика:* " + params[3] + "\n" +
                "*Дата:* " + params[6] + "\n";

        if (params[7] != null)
        {
            info += "*Время:* " + params[7] + "\n";
        }
        if (params[8] != null)
        {
            info += "*Место:* " + params[8] + "\n";
        }

        return info;
    }

    void downloadToDatabase() throws SQLException
    {
        // Проверка, введены ли все данные (время и место необязательны)
        for (int i = 0; i < 7; i++)
        {
            if (params[i] == null)
            {
                throw new SQLException("");
            }
        }

        EventsTable.addEvent(params);
    }

    public static final int PARAM_NUMBER = 9;
    private String[] params;
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