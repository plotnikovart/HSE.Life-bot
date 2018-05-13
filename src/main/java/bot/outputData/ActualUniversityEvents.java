package bot.outputData;

import bot.database.DBWorker;
import bot.database.EventsTable;
import bot.inputData.Event;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Класс, содержащий список актуальных мероприятий для конкретного университета
 */
class ActualUniversityEvents implements Iterable<Event>
{
    /**
     * Инициализация списка с актуальными мероприятиями
     * @param universityIndex Индекс университета
     */
    ActualUniversityEvents(int universityIndex)
    {
        try (Connection connection = DBWorker.getConnection())
        {
            // Получение актуальных мероприятий
            ResultSet resultSet = EventsTable.getEvents(universityIndex, connection);
            events = new LinkedList<>();

            if (resultSet == null)
            {
                return;
            }

            while (resultSet.next())
            {
                String[] params = new String[Event.PARAM_NUMBER];
                for (int i = 0; i < Event.PARAM_NUMBER; i++)
                {
                    params[i] = resultSet.getString(i + 1);
                }

                events.add(new Event(params));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Геттер
     * @return Возвращает список с актуальными мероприятиями
     */
    LinkedList<Event> getEvents()
    {
        return events;
    }

    /**
     * Проверка, если ли мероприятия
     * @return Есть или нет
     */
    boolean isEmpty()
    {
        return events.isEmpty();
    }


    private LinkedList<Event> events;   // список с актуальными мероприятиями

    /**
     * Получение итератора для данной коллекции
     * @return Итератор
     */
    @Override
    public Iterator<Event> iterator()
    {
        return events.iterator();
    }
}