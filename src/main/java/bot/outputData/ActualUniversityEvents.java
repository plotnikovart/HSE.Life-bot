package bot.outputData;

import bot.database.EventsTable;
import bot.inputData.Event;

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
     * Инициализаця списка с меоприятиями
     * @param universityIndex Индекс университета
     */
    ActualUniversityEvents(int universityIndex)
    {
        // Получение мероприятий
        System.out.println(Thread.currentThread().getId());
        ResultSet resultSet = EventsTable.getEvents(universityIndex);
        events = new LinkedList<>();

        String answer = "" + universityIndex + '\n';
        //System.out.println(universityIndex);

        try
        {
            while (resultSet.next())
            {
                answer += resultSet.getString(1) + " ";
                String[] params = new String[Event.PARAM_NUMBER];
                for (int i = 0; i < Event.PARAM_NUMBER; i++)
                {
                    params[i] = resultSet.getString(i + 1);
                }
                events.add(new Event(params));
            }

            //System.out.println(answer + '\n' + Thread.currentThread());
        }
        catch (SQLException e)
        {
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

    // Список с актуальными мероприятиями
    private LinkedList<Event> events;

    @Override
    public Iterator<Event> iterator()
    {
        return events.iterator();
    }
}