package bot.outputData.events;

import bot.database.EnumTable;
import bot.database.EventsTable;

import java.time.LocalTime;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Класс-сборщик мероприятий
 */
public class EventCollector implements Runnable
{
    /**
     * Инициализирует пул потоков, запускает сборщик в отдельном потоке
     */
    public EventCollector()
    {
        threadPool = Executors.newFixedThreadPool(10);

        Thread thread = new Thread(this, "EventCollector thread");
        thread.start();
    }

    /**
     * Запуск сборщика мероприятий в отдельном потоке
     */
    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                // Получение индекса для следующего времени для отправки и списка уиверситетов
                int timeIndex = EnumTable.getNextTimeIndex();
                LinkedList<Integer> universityIndexes = EnumTable.getUniversityIndexes();

                // Ожидание следующего времени для отправки подборки
                Thread.sleep(countTimeForNextMessage(timeIndex));

                Vector<Callable<Void>> tasks = new Vector<>();
                // Добавление задач для генерирования подборок
                for (Integer universityIndex : universityIndexes)
                {
                    tasks.add(() ->
                    {
                        UserGroups ug = new UserGroups(universityIndex, timeIndex);
                        ActualUniversityEvents aue = new ActualUniversityEvents(universityIndex);

                        // Если группа не пустая и есть мероприятия, то передаем в сортировщик
                        if (!aue.isEmpty() && !ug.isEmpty())
                        {
                            EventSorter.set(aue, ug);
                        }

                        return null;
                    });
                }

                // Выполнение и ожидание завершения задач
                threadPool.invokeAll(tasks);
                //Thread.sleep(30000000);

                // Удаление неактуальных мероприятий
                EventsTable.deleteOldEvents();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Подсчет времени до следующего сообщения
     * @param timeIndex Индекс времени следующего сообщения
     * @return Время в миллисекундах
     */
    private int countTimeForNextMessage(int timeIndex)
    {
        LocalTime messageTime = EnumTable.getTime(timeIndex);
        LocalTime currentTime = LocalTime.now();

        if (messageTime == null)
        {
            return Integer.MAX_VALUE;
        }

        int result;
        if (currentTime.compareTo(messageTime) > 0)
        {
            // Подборка только в следующем дне
            int a = messageTime.toSecondOfDay();
            int b = 3600 * 24 - currentTime.toSecondOfDay();

            result = a + b;
        }
        else
        {
            result = messageTime.toSecondOfDay() - currentTime.toSecondOfDay();
        }

        return result * 1000;
    }

    private ExecutorService threadPool;     // пул потоков
}