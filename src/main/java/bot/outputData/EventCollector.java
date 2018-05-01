package bot.outputData;


import bot.database.EnumTable;

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
     * Инициализирует необходимые поля
     */
    public EventCollector()
    {
        threadPool = Executors.newFixedThreadPool(5);

        Thread thread = new Thread(this, "EventCollector thread");
        thread.start();
    }

    @Override
    public void run()
    {
//        try
//        {
////            int j = 9;
//            Vector<Callable<Void>> tasks = new Vector<>(5);
//            for (int i = 0; i < 5; i++)
//            {
//                threadPool.submit(() ->
//                {
//                    System.out.println(i);
//
//                    return null;
//                });
//            }
//            //threadPool.invokeAll(tasks);
//        }
//        catch (Exception e){}


        //while (true)
        {
            try
            {
                // Получение индекса для следующего времени для отправки и списка уиверситетов
                int timeIndex = EnumTable.getNextTimeIndex();
                LinkedList<Integer> universityIndexes = EnumTable.getUniversityIndexes();

                // Ожидание следующего времени для отправки подборки
                //Thread.sleep(countTimeForNextMessage(timeIndex));

                // Добавление задач для генерирования подборок
                for (Integer universityIndex : universityIndexes)
                {
                    threadPool.submit(() ->
                    {
                    UserGroups ug = new UserGroups(universityIndex, 8);
                    ActualUniversityEvents aue = new ActualUniversityEvents(universityIndex);

                    // Если группа не пустая и есть мероприятия, то запускаем сортировщик
                    if (!aue.isEmpty() && !ug.isEmpty())
                    {
                        System.out.println(Thread.currentThread());
                        ArticlesSorter.set(aue, ug);
                    }

                        return null;
                    });

                }

                // Выполнение и ожидание завершения задач
                //threadPool.invokeAll(tasks);
                //threadPool.shutdown();
            }
            catch (Exception e)
            {
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

    // Пул потоков
    private static ExecutorService threadPool;
}