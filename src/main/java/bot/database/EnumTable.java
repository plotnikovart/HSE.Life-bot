package bot.database;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Класс для взаимодействия с таблицами - university_list, event_type_list, time_list.
 * А также с change_list, everyday_images
 */
public class EnumTable
{
    /**
     * Иницализация коннектора к базе и шаблонов запросов
     * @param connection Коннектор
     * @throws SQLException Произошла ошибка при формировании запросов
     */
    static void initialize(Connection connection) throws SQLException
    {
        // Инициализация шаблонов запросов
        getUniversityListPS = connection.prepareStatement("SELECT name FROM university_list ORDER BY name");
        getEventTypeListPS = connection.prepareStatement("SELECT name FROM event_type_list ORDER BY name");
        getTimeListPS = connection.prepareStatement("SELECT time FROM time_list ORDER BY time");

        checkPS = connection.prepareStatement("SELECT status FROM change_list WHERE table_name = ? AND status = TRUE");
        updateStatusPS = connection.prepareStatement("UPDATE change_list SET status = FALSE WHERE table_name = ?");

        getNextTimeIndexPS = connection.prepareStatement("(SELECT *" +
                " FROM time_list" +
                " WHERE time > NOW()" +
                " ORDER BY time)" +
                "UNION" +
                "(SELECT *" +
                " FROM time_list" +
                " ORDER BY time)");

        getTimePS = connection.prepareStatement("SELECT time FROM time_list WHERE id = ?");

        getUniversityIndexesPS = connection.prepareStatement("SELECT id FROM university_list");

        getEverydayImagePS = connection.prepareStatement("SELECT refference FROM everyday_images WHERE day_of_week = WEEKDAY(NOW())");

        // Загрузка данных
        loadUniversity();
        loadEventType();
        loadTime();
    }

    /**
     * Получение списка университетов
     * @return Список университетов
     */
    public static ArrayList<String> getUniversityList()
    {
        try
        {
            // Если значения списка изменялись, то обновляем список
            synchronized (EnumTable.class)
            {
                // Не пропустит второй поток для повторного изменения
                if (isChanged("university_list"))
                {
                    loadUniversity();
                }
            }

        }
        catch (SQLException e)
        {
        }

        return universityList;
    }

    /**
     * Получение индексов университетов
     * @return Индексы университетов, пустой список, если ошибка
     */
    public static LinkedList<Integer> getUniversityIndexes()
    {
        try
        {
            ResultSet resultSet = getUniversityIndexesPS.executeQuery();
            LinkedList<Integer> list = new LinkedList<>();

            while (resultSet.next())
            {
                list.add(resultSet.getInt(1));
            }

            return list;
        }
        catch (SQLException e)
        {
            return new LinkedList<>();
        }
    }

    /**
     * Получение списка с типами мероприятий
     * @return Список с типами мероприятий
     */
    public static ArrayList<String> getEventTypeList()
    {
        try
        {
            // Если значения изменялись, то обновляем их
            synchronized (EnumTable.class)
            {
                // Не пропустит второй поток для повторного изменения
                if (isChanged("events"))
                {
                    loadEventType();
                }
            }
        }
        catch (SQLException e)
        {
        }

        return eventsTypeList;
    }

    /**
     * Получение списка со временем
     * @return Список со временем
     */
    public static ArrayList<String> getTimeList()
    {
        try
        {
            // Если значения изменялись, то обновляем их
            synchronized (EnumTable.class)
            {
                // Не пропустит второй поток для повторного изменения
                if (isChanged("time"))
                {
                    loadTime();
                }
            }

        }
        catch (SQLException e)
        {
        }

        return timeList;
    }

    /**
     * Получение времени по индексу времени
     * @param timeIndex Индекс времени
     * @return Время, null если индекс указан неверно
     */
    public static LocalTime getTime(int timeIndex)
    {
        try
        {
            getTimePS.setInt(1, timeIndex);
            ResultSet resultSet = getTimePS.executeQuery();

            resultSet.next();
            Time time = resultSet.getTime(1);
            return LocalTime.of(time.getHours(), time.getMinutes());
        }
        catch (SQLException e)
        {
            return null;
        }
    }

    /**
     * Получение индекса следующего времени отправки
     * @return Индекс времени, 0, если ошибка
     */
    public static int getNextTimeIndex()
    {
        try
        {
            ResultSet resultSet = getNextTimeIndexPS.executeQuery();

            resultSet.next();
            return resultSet.getInt(1);
        }
        catch (SQLException e)
        {
            return 0;
        }
    }

    /**
     * Получение ссылки на ежедневную картинку
     * @return
     */
    synchronized public static String getEverydayImage()
    {
        try
        {
            ResultSet resultSet = getEverydayImagePS.executeQuery();

            resultSet.next();
            return resultSet.getString(1);
        }
        catch (SQLException e)
        {
            return "";
        }
    }

    /**
     * Обновление списка университетов
     * @throws SQLException Ошибка при обновлении
     */
    private static void loadUniversity() throws SQLException
    {
        universityList = new ArrayList<>();

        ResultSet resultSet = getUniversityListPS.executeQuery();

        while (resultSet.next())
        {
            universityList.add(resultSet.getString(1));
        }

        // Обновление статуса
        updateStatusPS.setString(1, "university_list");
        updateStatusPS.executeUpdate();
    }

    /**
     * Обновление списка типов мероприятий
     * @throws SQLException Ошибка при обновлении
     */
    private static void loadEventType() throws SQLException
    {
        eventsTypeList = new ArrayList<>();

        ResultSet resultSet = getEventTypeListPS.executeQuery();

        while (resultSet.next())
        {
            eventsTypeList.add(resultSet.getString(1));
        }

        // Обновление статуса
        updateStatusPS.setString(1, "event_type_list");
        updateStatusPS.executeUpdate();
    }

    /**
     * Обновление списка времен
     * @throws SQLException Ошибка при обновлении
     */
    private static void loadTime() throws SQLException
    {
        timeList = new ArrayList<>();

        ResultSet resultSet = getTimeListPS.executeQuery();

        while (resultSet.next())
        {
            String time = resultSet.getString(1);
            timeList.add(time.substring(0, time.length() - 3));
        }

        // Обновление статуса
        updateStatusPS.setString(1, "time_list");
        updateStatusPS.executeUpdate();
    }

    /**
     * Проверка, изменилась ли таблица
     * @param tableName Имя таблицы
     * @return Изменилась или нет
     */
    public static boolean isChanged(String tableName)
    {
        boolean changed = false;
        try
        {
            checkPS.setString(1, tableName);

            ResultSet resultSet = checkPS.executeQuery();
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


    private static volatile ArrayList<String> universityList;       // университеты
    private static volatile ArrayList<String> eventsTypeList;       // типы мероприятий
    private static volatile ArrayList<String> timeList;             // времена

    private static PreparedStatement getUniversityListPS;           // получение университетов
    private static PreparedStatement getEventTypeListPS;            // получение типов мероприятий
    private static PreparedStatement getTimeListPS;                 // получение времен

    private static PreparedStatement getUniversityIndexesPS;        // получение индексов университетов

    private static PreparedStatement getNextTimeIndexPS;            // получение индекса следующего времени
    private static PreparedStatement getTimePS;                     // получение времени по индексу

    private static PreparedStatement getEverydayImagePS;            // получение картинки на каждый день

    private static PreparedStatement checkPS;                       // проверка на изменение
    private static PreparedStatement updateStatusPS;                // обновление университетов/типов мероприятий/времени
}