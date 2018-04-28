package bot;

import bot.database.DBWorker;
import bot.outputData.EventCollector;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.ArrayList;

public class Initializer
{
    public static void main(String[] args)
    {
        try
        {
            EventCollector ev = new EventCollector();

            DBWorker.initialize();

            // Инициализация Api контекста
            ApiContextInitializer.init();

            // Инициализация Telegram Bots API
            TelegramBotsApi botsApi = new TelegramBotsApi();

            // Регистрация бота
            Bot myBot = new Bot();
            botsApi.registerBot(myBot);
        }
        catch (SQLException e)
        {
            System.out.println("Ошибка подключения к базе данных");
        }
        catch (TelegramApiException e)
        {
            System.out.println("Ошибка регистрации бота");
        }
    }
}
