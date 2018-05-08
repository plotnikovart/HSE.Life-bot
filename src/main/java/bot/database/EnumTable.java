package bot.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;

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
     * Иницализация коннектора к базе и предзагрузка университетов
     * @param dataSource Пул коннекторов к БД
     */
    static void initialize(ComboPooledDataSource dataSource)
    {
        EnumTable.dataSource = dataSource;

//        // Инициализация шаблонов запросов
//        getUniversityListPS = connection.prepareStatement("SELECT name FROM university_list ORDER BY name");
//        getEventTypeListPS = connection.prepareStatement("SELECT name FROM event_type_list ORDER BY name");
//        getTimeListPS = connection.prepareStatement("SELECT time FROM time_list ORDER BY time");
//
//        checkPS = connection.prepareStatement("SELECT status FROM change_list WHERE table_name = ? AND status = TRUE");
//        updateStatusPS = connection.prepareStatement("UPDATE change_list SET status = FALSE WHERE table_name = ?");
//
//        getNextTimeIndexPS = connection.prepareStatement("(SELECT *" +
//                " FROM time_list" +
//                " WHERE time > NOW()" +
//                " ORDER BY time)" +
//                "UNION" +
//                "(SELECT *" +
//                " FROM time_list" +
//                " ORDER BY time)");
//
//        getTimePS = connection.prepareStatement("SELECT time FROM time_list WHERE id = ?");
//
//        getUniversityIndexesPS = connection.prepareStatement("SELECT id FROM university_list");
//
//        getEverydayImagePS = connection.prepareStatement("SELECT refference FROM everyday_images WHERE day_of_week = WEEKDAY(NOW())");
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
        // Если значения списка изменялись, то обновляем список
        synchronized (EnumTable.class)
        {
            // Не пропустит второй поток для повторного изменения
            if (isChanged("university_list"))
            {
                loadUniversity();
            }
        }

        return universityList;
    }

    /**
     * Получение индексов университетов
     * @return Индексы университетов, пустой список, если ошибка
     */
    public static LinkedList<Integer> getUniversityIndexes()
    {
        try (Connection connection = dataSource.getConnection())
        {
            LinkedList<Integer> list = new LinkedList<>();
            ResultSet resultSet = connection.createStatement().executeQuery(GET_UNIVERSITY_INDEXES);

            while (resultSet.next())
            {
                list.add(resultSet.getInt(1));
            }

            return list;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return new LinkedList<>();
        }
    }

    /**
     * Получение списка с типами мероприятий
     * @return Список с типами мероприятий
     */
    public static ArrayList<String> getEventTypeList()
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

        return eventsTypeList;
    }

    /**
     * Получение списка со временем
     * @return Список со временем
     */
    public static ArrayList<String> getTimeList()
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

        return timeList;
    }

    /**
     * Получение времени по индексу времени
     * @param timeIndex Индекс времени
     * @return Время, null если индекс указан неверно
     */
    public static LocalTime getTime(int timeIndex)
    {
        try (Connection connection = dataSource.getConnection())
        {
            PreparedStatement getTimePS = connection.prepareStatement(GET_TIME);
            getTimePS.setInt(1, timeIndex);
            ResultSet resultSet = getTimePS.executeQuery();

            resultSet.next();
            Time time = resultSet.getTime(1);
            return LocalTime.of(time.getHours(), time.getMinutes());
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Получение индекса следующего времени отправки
     * @return Индекс времени, 0, если ошибка
     */
    public static int getNextTimeIndex()
    {
        try (Connection connection = dataSource.getConnection())
        {
            ResultSet resultSet = connection.createStatement().executeQuery(GET_NEXT_TIME_INDEX);

            resultSet.next();
            return resultSet.getInt(1);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Получение ссылки на ежедневную картинку
     */
    public static String getEverydayImage()
    {
        try (Connection connection = dataSource.getConnection())
        {
            ResultSet resultSet = connection.createStatement().executeQuery(GET_EVERYDAY_IMAGE);

            resultSet.next();
            return resultSet.getString(1);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Обновление списка университетов
     * @throws SQLException Ошибка при обновлении
     */
    private static void loadUniversity()
    {
        try (Connection connection = dataSource.getConnection())
        {
            ResultSet resultSet = connection.createStatement().executeQuery(GET_UNIVERSITY_LIST);
            universityList = new ArrayList<>();

            while (resultSet.next())
            {
                universityList.add(resultSet.getString(1));
            }

            // Обновление статуса
            PreparedStatement updateStatusPS = connection.prepareStatement(UPDATE_STATUS);
            updateStatusPS.setString(1, "university_list");
            updateStatusPS.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Обновление списка типов мероприятий
     * @throws SQLException Ошибка при обновлении
     */
    private static void loadEventType()
    {
        try (Connection connection = dataSource.getConnection())
        {
            ResultSet resultSet = connection.createStatement().executeQuery(GET_EVENTS_TYPE_LIST);
            eventsTypeList = new ArrayList<>();

            while (resultSet.next())
            {
                eventsTypeList.add(resultSet.getString(1));
            }

            // Обновление статуса
            PreparedStatement updateStatusPS = connection.prepareStatement(UPDATE_STATUS);
            updateStatusPS.setString(1, "event_type_list");
            updateStatusPS.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Обновление списка времен
     * @throws SQLException Ошибка при обновлении
     */
    private static void loadTime()
    {
        try (Connection connection = dataSource.getConnection())
        {
            ResultSet resultSet = connection.createStatement().executeQuery(GET_TIME_LIST);
            timeList = new ArrayList<>();

            while (resultSet.next())
            {
                String time = resultSet.getString(1);
                timeList.add(time.substring(0, time.length() - 3));
            }

            // Обновление статуса
            PreparedStatement updateStatusPS = connection.prepareStatement(UPDATE_STATUS);
            updateStatusPS.setString(1, "time_list");
            updateStatusPS.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Проверка, изменилась ли таблица
     * @param tableName Имя таблицы
     * @return Изменилась или нет
     */
    public static boolean isChanged(String tableName)
    {
        try (Connection connection = dataSource.getConnection())
        {
            boolean changed = false;

            PreparedStatement check = connection.prepareStatement(CHECK);
            check.setString(1, tableName);

            ResultSet resultSet = check.executeQuery();
            if (resultSet.next())
            {
                changed = true;
            }

            return changed;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private static volatile ArrayList<String> universityList;       // университеты
    private static volatile ArrayList<String> eventsTypeList;       // типы мероприятий
    private static volatile ArrayList<String> timeList;             // времена

    private static ComboPooledDataSource dataSource;

    // Шаблоны запросов

    private static final String GET_UNIVERSITY_LIST =               // получение университетов
            "SELECT name FROM university_list ORDER BY name";
    private static final String GET_EVENTS_TYPE_LIST =              // получение типов мероприятий
            "SELECT name FROM event_type_list ORDER BY name";
    private static final String GET_TIME_LIST =                     // получение времен
            "SELECT time FROM time_list ORDER BY time";

    private static final String GET_UNIVERSITY_INDEXES =            // получение индексов университетов
            "SELECT id FROM university_list";

    private static final String GET_NEXT_TIME_INDEX =               // получение индекса следующего времени
            "(SELECT *" +
            " FROM time_list" +
            " WHERE time > NOW()" +
            " ORDER BY time)" +
            "UNION" +
            "(SELECT *" +
            " FROM time_list" +
            " ORDER BY time)";
    private static final String GET_TIME =                          // получение времени по индексу
            "SELECT time FROM time_list WHERE id = ?";

    private static final String GET_EVERYDAY_IMAGE =                // получение картинки на каждый день
            "SELECT refference FROM everyday_images WHERE day_of_week = WEEKDAY(NOW())";

    private static final String CHECK =                             // проверка на изменение университетов/типов
            // мероприятий/времени
            "SELECT status FROM change_list WHERE table_name = ? AND status = TRUE";
    private static final String UPDATE_STATUS =                     // обновление университетов/типов
            // мероприятий/времени
            "UPDATE change_list SET status = FALSE WHERE table_name = ?";
//
//    private static PreparedStatement getUniversityListPS;           // получение университетов
//    private static PreparedStatement getEventTypeListPS;            // получение типов мероприятий
//    private static PreparedStatement getTimeListPS;                 // получение времен
//
//    private static PreparedStatement getUniversityIndexesPS;        // получение индексов университетов
//
//    private static PreparedStatement getNextTimeIndexPS;            // получение индекса следующего времени
//    private static PreparedStatement getTimePS;                     // получение времени по индексу
//
//    private static PreparedStatement getEverydayImagePS;            // получение картинки на каждый день
//
//    private static PreparedStatement checkPS;                       // проверка на изменение
//    private static PreparedStatement updateStatusPS;                // обновление университетов/типов мероприятий/времени
}