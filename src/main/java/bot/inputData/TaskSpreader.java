package bot.inputData;

import bot.Bot;
import org.telegram.telegrambots.api.methods.send.SendMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Класс-распределитель
 * Распределяет задачи (входные сообщения) между потоками
 */
public class TaskSpreader
{
    /**
     * Распределитель задач.
     * Инициализация меню, пула потоков, ссылки на бота
     * @param bot Ссылка на бота (для отправки сообщений)
     */
    public TaskSpreader(Bot bot)
    {
        threadPool = Executors.newFixedThreadPool(5);
        menu = new Menu();
        this.bot = bot;
    }

    /**
     * Получает задачу от бота, дает ее на выполнение одному из потоков
     * @param messageText Текст сообщения
     * @param chatId Идентификатор чата (id пользователя)
     */
    public void setTask(String messageText, long chatId)
    {
        threadPool.submit(() ->
        {
            // Обращение к меню, получение ответа
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

    private ExecutorService threadPool;     // пул потоков, которые обрабатывают входные сообщения
    private Menu menu;                      // древовидное меню
    private Bot bot;                        // ссылка на бота (для отправки сообщений)
}