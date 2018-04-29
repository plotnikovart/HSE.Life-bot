package bot.outputData;


import bot.Bot;
import bot.database.EnumTable;
import bot.database.UsersTable;


import java.time.LocalTime;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventCollector implements Runnable
{
    public EventCollector(/*Bot bot*/)
    {
        threadPool = Executors.newFixedThreadPool(2);

        Thread thread = new Thread(this, "EventCollector thread");
        thread.start();
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                Vector<Callable<Void>> tasks = new Vector<>();
                tasks.setSize(5);
                for (int i = 0; i < 5; i++)
                {
                    tasks.set(i, () ->
                    {
                        System.out.println(Thread.currentThread());
                        for (int j = 0; j < 10000; j++)
                        {
                            double a = Math.cos(j);
                        }

                        return null;
                    });
                }

                threadPool.invokeAll(tasks);

                int timeIndex = EnumTable.getNextTimeIndex();
                System.out.println(Thread.currentThread());

                UserGroups ug = new UserGroups(UsersTable.getUsersEventsType(1, 2));
                ActualUniversityEvents ae = new ActualUniversityEvents(1);

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
    private Bot bot;
}