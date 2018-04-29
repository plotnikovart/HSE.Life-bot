package bot.inputData;

import bot.Bot;
import org.telegram.telegrambots.api.methods.send.SendMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Класс-распределитель
 * Распределяет задачи между потоками
 */
public class TaskSpreader
{
    public TaskSpreader(Bot bot)
    {
        menu = new Menu();
        this.bot = bot;
        threadPool = Executors.newFixedThreadPool(5);
    }

    public void setTask(String messageText, long chatId)
    {
        threadPool.submit(() ->
        {
            System.out.println(Thread.currentThread());
            SendMessage message = menu.call(messageText, chatId);
            message.setChatId(chatId);
            try
            {
                bot.execute(message);
            }
            catch (Exception e)
            {
            }
        });
    }

    private ExecutorService threadPool;
    private Menu menu;
    private Bot bot;
}