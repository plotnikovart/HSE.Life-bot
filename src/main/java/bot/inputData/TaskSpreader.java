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
     * Инициализация меню, пула потоков
     */
    public TaskSpreader()
    {
        threadPool = Executors.newFixedThreadPool(5);
        menu = new Menu();
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

            // Отправка сообщения
            Bot.sendM(message);
        });
    }

    private ExecutorService threadPool;     // пул потоков, которые обрабатывают входные сообщения
    private Menu menu;                      // древовидное меню
}