package bot;

import bot.database.DBWorker;
import bot.outputData.EventCollector;
import bot.outputData.MessageConstructor;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegraph.exceptions.TelegraphException;

import java.sql.SQLException;


public class Initializer
{
    public static void main(String[] args)
    {
        try
        {
            DBWorker.initialize();

            //MessageConstructor.initialize();

            // Инициализация Api контекста
            ApiContextInitializer.init();

            // Регистрация бота
            Bot myBot = new Bot();
            new TelegramBotsApi().registerBot(myBot);

            MessageConstructor.initialize(myBot);
            EventCollector eventCollector = new EventCollector();
        }
        catch (SQLException e)
        {
            System.out.println("Ошибка подключения к базе данных");
        }
        catch (TelegramApiException e)
        {
            System.out.println("Ошибка регистрации бота");
        }
        catch (TelegraphException e)
        {
            System.out.println("Ошибка регистрации в Telegraph");
        }
    }
}