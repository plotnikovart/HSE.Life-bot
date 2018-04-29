package bot.database;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Класс для взаимодействия с таблицами - university_list, event_type_list, time_list
 */
public class EnumTable
{
    static void initialize(Connection connection) throws SQLException
    {
        // Инициализация шаблонов запросов
        getUniversitiesList = connection.prepareStatement("SELECT name FROM university_list ORDER BY name");
        getEventTypeList = connection.prepareStatement("SELECT name FROM event_type_list ORDER BY name");
        getTimeList = connection.prepareStatement("SELECT time FROM time_list ORDER BY time");

        check = connection.prepareStatement("SELECT status FROM change_list WHERE table_name = ? AND status = TRUE");
        update = connection.prepareStatement("UPDATE change_list SET status = FALSE WHERE table_name = ?");

        getNextTimeIndex = connection.prepareStatement("(SELECT *" +
                " FROM time_list" +
                " WHERE time > NOW()" +
                " ORDER BY time)" +
                "UNION" +
                "(SELECT *" +
                " FROM time_list" +
                " ORDER BY time)");

        getTimeOnIndex = connection.prepareStatement("SELECT time FROM time_list WHERE id = ?");

        getUniversityIndexes = connection.prepareStatement("SELECT id FROM university_list");

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

    public static LinkedList<Integer> getUniversityIndexes()
    {
        try
        {
            ResultSet resultSet = getUniversityIndexes.executeQuery();
            LinkedList<Integer> list = new LinkedList<>();

            while (resultSet.next())
            {
                list.add(resultSet.getInt(1));
            }

            return list;
        }
        catch (SQLException e)
        {
            return null;
        }
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

    public static LocalTime getTime(Integer timeIndex)
    {
        LocalTime nextTime = null;
        try
        {
            getTimeOnIndex.setInt(1, timeIndex);
            ResultSet resultSet = getTimeOnIndex.executeQuery();

            resultSet.next();
            Time time = resultSet.getTime(1);
            nextTime = LocalTime.of(time.getHours(), time.getMinutes());
        }
        catch (SQLException e)
        {
        }

        return nextTime;
    }

    public static int getNextTimeIndex()
    {
        int index = 0;
        try
        {
            ResultSet resultSet = getNextTimeIndex.executeQuery();

            resultSet.next();
            index = resultSet.getInt(1);
        }
        catch (SQLException e)
        {
        }

        return index;
    }

    private static void loadUniversity() throws SQLException
    {
        universityList = new ArrayList<>();

        ResultSet resultSet = getUniversitiesList.executeQuery();

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

        ResultSet resultSet = getEventTypeList.executeQuery();

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

        ResultSet resultSet = getTimeList.executeQuery();

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

    private static PreparedStatement getUniversitiesList;
    private static PreparedStatement getEventTypeList;
    private static PreparedStatement getTimeList;

    private static PreparedStatement getUniversityIndexes;

    private static PreparedStatement getNextTimeIndex;
    private static PreparedStatement getTimeOnIndex;

    private static PreparedStatement check;
    private static PreparedStatement update;
}
