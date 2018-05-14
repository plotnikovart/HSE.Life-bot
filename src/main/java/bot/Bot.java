package bot;

import bot.inputData.TaskSpreader;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;


/**
 * Класс, представляющий собой бота. Служит для принятия и отправки сообщений
 */
public class Bot extends TelegramLongPollingBot
{
    /**
     * Инициализация бота, создание распределителя задач
     */
    Bot()
    {
        super();
        taskSpreader = new TaskSpreader();  // инициализация распределителя задач
        Bot.bot = this;
    }

    /**
     * Медод для получения сообщений
     * @param update Полученное обновление от пользователя
     */
    @Override
    public void onUpdateReceived(Update update)
    {
        // Проверка, отправлено ли сообщение, содержит ли оно текст или нажатие на кнопку
        if (update.hasMessage() && update.getMessage().hasText() || update.hasCallbackQuery())
        {
            String messageText;
            long chatId;

            // Получение текста сообщения
            if (update.hasMessage())
            {
                messageText = update.getMessage().getText();
                chatId = update.getMessage().getChatId();
            }
            else
            {
                messageText = update.getCallbackQuery().getData();
                chatId = update.getCallbackQuery().getFrom().getId();
            }

            taskSpreader.setTask(messageText, chatId);
        }
    }

    /**
     * Геттер
     * @return Имя бота в сети
     */
    @Override
    public String getBotUsername()
    {
        return botName;
    }

    /**
     * Геттер
     * @return Токен бота
     */
    @Override
    public String getBotToken()
    {
        return botToken;
    }


    /**
     * Отправка сообщения
     * @param chatId  id пользователя
     * @param message Текст сообщения
     */
    public static void sendM(long chatId, String message)
    {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(message);
        sendMessage.enableMarkdown(true);
        sendMessage.setParseMode("markdown");
        sendMessage.setChatId(chatId);

        sendM(sendMessage);
    }

    /**
     * Отправка сообщения
     * @param message Готовое сообщение
     */
    public static void sendM(SendMessage message)
    {
        try
        {
            synchronized (Bot.bot)
            {
                bot.execute(message);
            }
        }
        catch (TelegramApiException e)
        {
            e.printStackTrace();
        }
    }

    static private volatile Bot bot;                // ссылка на бота
    private TaskSpreader taskSpreader;              // распределитель задач
    private final String botName = "HSE.Life bot";
    private final String botToken = "456367182:AAEUOD9XomIQhwHz7zT4cQ--2uBp7ts6Wxo";
}