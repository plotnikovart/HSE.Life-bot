package bot.inputData;

import bot.database.EventsTable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс-контейнер. Хранит в себе мероприятия, которые еще не были загружены в базу данных.
 * После загрузки в базу мероприятие удаляется отсюда.
 */
public class Events
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
                event.setTheme(param);
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


class Event
{
    void setName(String name)
    {
        this.name = name;
    }

    void setDescription(String description)
    {
        this.description = description;
    }

    void setUniversity(String university)
    {
        this.university = university;
    }

    void setTheme(String theme)
    {
        this.theme = theme;
    }

    void setPhotoRef(String photoRef)
    {
        this.photoRef = photoRef;
    }

    void setRef(String ref)
    {
        this.ref = ref;
    }

    void setDate(String date)
    {
        this.date = date;
    }

    void setTime(String time)
    {
        this.time = time;
    }

    void setPlace(String place)
    {
        this.place = place;
    }

    String getInfo()
    {
        String info = "*Название:* " + name + "\n" +
                "*Описание:* " + description + "\n" +
                "*Университет:* " + university + "\n" +
                "*Тематика:* " + theme + "\n" +
                "*Дата:* " + date + "\n";

        if (time != null) info += "*Время:* " + time + "\n";
        if (place != null) info += "*Место:* " + place + "\n";

        return info;
    }

    void downloadToDatabase() throws SQLException
    {
        // Проверка, введены ли все данные (время и место необязательны)
        if (name == null || description == null || university == null ||
                theme == null || photoRef == null || ref == null || date == null) throw new SQLException("");

        EventsTable.addEvent(name, description, university, theme, photoRef, ref, date, time, place);
    }

    private String name, description;
    private String university, theme;
    private String photoRef, ref;
    private String date, time, place;
}