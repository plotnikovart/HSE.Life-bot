package bot.inputData;

import bot.database.UsersTable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class Users
{
    static void setUniversity(String university, int userId)
    {
        findUser(userId).setUniversity(university);
    }

    static String setEvents(String event, int userId)
    {
        User user = findUser(userId);
        user.setEvents(event);

        return user.getEventsS();
    }

    static void setTime(String time, int userId)
    {
        findUser(userId).setTime(time);
    }

    static void downloadUserToDatabase(int userId)
    {
        User user = users.get(userId);
        user.downloadToDatabase();

        users.remove(userId);
    }

    static void deleteUser(int userId)
    {
        users.remove(userId);
    }

    static String getUserInfo(int userId)
    {
        User user = findUser(userId);

        return user.getInfo();
    }

    private static User findUser(int userId)
    {
        User user = users.get(userId);

        if (user == null)
        {
            user = new User(userId);
            users.put(userId, user);
        }

        return user;
    }

    private static ConcurrentHashMap<Integer, User> users = new ConcurrentHashMap<>();
}


class User
{
    User(int id)
    {
        this.id = id;
        university = "НИУ ВШЭ (Москва)";
        events = new LinkedList<>();
        time = "10:00";
    }

    void setUniversity(String university)
    {
        this.university = university;
    }

    void setEvents(String event)
    {
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

    void setTime(String time)
    {
        this.time = time;
    }

    String getEventsS()
    {
        String eventsS = "";
        for (Iterator<String> i = events.iterator(); i.hasNext(); eventsS += i.next() + ", ") ;

        return eventsS.length() == 0 ? eventsS : eventsS.substring(0, eventsS.length() - 2);
    }

    String getInfo()
    {
        return "*Университет:* " + university +
                "\n*Приоритетные темы мероприятий:* " + getEventsS() +
                "\n*Время для получения подборки:* " + time;
    }

    void downloadToDatabase()
    {
        try
        {
            UsersTable.addUser(id, university, time, events);
        }
        catch (Exception e)
        {
        }


    }

    private int id;
    private String university;
    private List<String> events;
    private String time;
}