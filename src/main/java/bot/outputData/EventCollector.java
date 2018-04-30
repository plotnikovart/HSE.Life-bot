package bot.outputData;


import bot.database.EnumTable;

import java.time.LocalTime;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.Callable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventCollector implements Runnable
{
    public EventCollector()
    {
        threadPool = Executors.newFixedThreadPool(10);

        Thread thread = new Thread(this, "EventCollector thread");
        thread.start();
    }

    @Override
    public void run()
    {
        while (true)
        {
            UserGroups ug1 = new UserGroups(1, 2);
            ActualUniversityEvents aue1 = new ActualUniversityEvents(1);

            ArticlesSorter.set(aue1, ug1);

            try
            {
                // Получение индекса для следующего времени для отправки и списка уиверситетов
                int timeIndex = EnumTable.getNextTimeIndex();
                LinkedList<Integer> universityIndexes = EnumTable.getUniversityIndexes();

                // Добавление задач для генерирования подборок
                Vector<Callable<Void>> tasks = new Vector<>();
                for (Integer universityIndex : universityIndexes)
                {
                    tasks.add(() ->
                    {
                        UserGroups ug = new UserGroups(universityIndex, timeIndex);
                        ActualUniversityEvents aue = new ActualUniversityEvents(universityIndex);

                        ArticlesSorter.set(aue, ug);

                        return null;
                    });
                }

                // Выполнение и ожидание завершения задач
                threadPool.invokeAll(tasks);
                threadPool.shutdown();

                // Ожидание следующего времени для отправки подборки
                Thread.sleep(countTimeForNextMessage(timeIndex));
            }
            catch (InterruptedException e)
            {
                int a = 8;
            }
        }
    }

    private int countTimeForNextMessage(int timeIndex)
    {
        LocalTime messageTime = EnumTable.getTime(timeIndex);
        LocalTime currentTime = LocalTime.now();

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


    private ExecutorService threadPool;
}