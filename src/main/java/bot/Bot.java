package bot;

import bot.inputData.TaskSpreader;

import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;


public class Bot extends TelegramLongPollingBot
{
    Bot()
    {
        super();
        taskSpreader = new TaskSpreader(this);
    }

    @Override
    public void onUpdateReceived(Update update)
    {
        // Проверка, отправлено ли сообщение, содержит ли оно текст или нажатие на кнопку
        if (update.hasMessage() && update.getMessage().hasText() || update.hasCallbackQuery())
        {
            System.out.println(Thread.currentThread());

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


    private TaskSpreader taskSpreader;
    private final String botName = "HSE.Life bot";
    private final String botToken = "456367182:AAEUOD9XomIQhwHz7zT4cQ--2uBp7ts6Wxo";
}