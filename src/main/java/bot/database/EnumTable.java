package bot.database;

import java.sql.*;
import java.util.ArrayList;

/**
 * Класс для взаимодействия с таблицами - university_list, event_type_list, time_list
 */
public class EnumTable
{
    static void initialize(Connection connection) throws SQLException
    {
        // Инициализация шаблонов запросов
        getUniversities = connection.prepareStatement("SELECT name FROM university_list ORDER BY name");
        getEventType = connection.prepareStatement("SELECT name FROM event_type_list ORDER BY name");
        getTime = connection.prepareStatement("SELECT time FROM time_list ORDER BY time");

        check = connection.prepareStatement("SELECT status FROM change_list WHERE table_name = ? AND status = TRUE");
        update = connection.prepareStatement("UPDATE change_list SET status = FALSE WHERE table_name = ?");

        // Загрузка данных
        loadUniversity();
        loadEventType();
        loadTime();
    }

    public static ArrayList<String> getUniversityList()
    {
        try
        {
            if (isChanged("university_list"))
            {
                loadUniversity();
            }
        }
        catch (SQLException e)
        {
        }

        return universityList;
    }

    public static ArrayList<String> getEventTypeList()
    {
        try
        {
            if (isChanged("events"))
            {
                loadEventType();
            }
        }
        catch (SQLException e)
        {
        }

        return eventsTypeList;
    }

    public static ArrayList<String> getTimeList()
    {
        try
        {
            if (isChanged("time"))
            {
                loadTime();
            }
        }
        catch (SQLException e)
        {
        }

        return timeList;
    }


    private static void loadUniversity() throws SQLException
    {
        universityList = new ArrayList<>();

        ResultSet resultSet = getUniversities.executeQuery();

        while (resultSet.next())
        {
            universityList.add(resultSet.getString(1));
        }

        // Обновление статуса
        update.setString(1, "university_list");
        update.executeUpdate();
    }

    private static void loadEventType() throws SQLException
    {
        eventsTypeList = new ArrayList<>();

        ResultSet resultSet = getEventType.executeQuery();

        while (resultSet.next())
        {
            eventsTypeList.add(resultSet.getString(1));
        }

        // Обновление статуса
        update.setString(1, "event_type_list");
        update.executeUpdate();
    }

    private static void loadTime() throws SQLException
    {
        timeList = new ArrayList<>();

        ResultSet resultSet = getTime.executeQuery();

        while (resultSet.next())
        {
            String time = resultSet.getString(1);
            timeList.add(time.substring(0, time.length() - 3));
        }

        // Обновление статуса
        update.setString(1, "time_list");
        update.executeUpdate();
    }

    public static boolean isChanged(String tableName)
    {
        boolean changed = false;
        try
        {
            check.setString(1, tableName);

            ResultSet resultSet = check.executeQuery();
            if (resultSet.next())
            {
                changed = true;
            }
        }
        catch (SQLException e)
        {
        }

        return changed;
    }


    private static ArrayList<String> universityList;
    private static ArrayList<String> eventsTypeList;
    private static ArrayList<String> timeList;

    private static PreparedStatement getUniversities;
    private static PreparedStatement getEventType;
    private static PreparedStatement getTime;

    private static PreparedStatement check;
    private static PreparedStatement update;
}
