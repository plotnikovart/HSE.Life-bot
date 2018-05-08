package bot;

import bot.inputData.TaskSpreader;

import bot.outputData.MessageConstructor;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegraph.exceptions.TelegraphException;


/**
 * Класс, представляющий собой бота
 */
public class Bot extends TelegramLongPollingBot
{
    Bot()
    {
        super();
        taskSpreader = new TaskSpreader();  // инициализация распределителя задач
        MessageSender.initialize(this);
    }

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

    @Override
    public String getBotUsername()
    {
        return botName;
    }

    @Override
    public String getBotToken()
    {
        return botToken;
    }

    /**
     * Класс для отправки сообщений
     */
    public static class MessageSender
    {
        static private Bot bot;

        static void initialize(Bot bot)
        {
            MessageSender.bot = bot;
        }

        public static void send(long chatId, String message)
        {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setText(message);
            sendMessage.enableMarkdown(true);
            sendMessage.setParseMode("markdown");
            sendMessage.setChatId(chatId);

            send(sendMessage);
        }

        public static void send(SendMessage message)
        {
            try
            {
                bot.execute(message);
            }
            catch (TelegramApiException e)
            {
                e.printStackTrace();
            }
        }
    }

    private TaskSpreader taskSpreader;
    private final String botName = "HSE.Life bot";
    private final String botToken = "456367182:AAEUOD9XomIQhwHz7zT4cQ--2uBp7ts6Wxo";
}