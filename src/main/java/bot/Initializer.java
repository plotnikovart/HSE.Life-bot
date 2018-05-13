package bot;

import bot.database.DBWorker;
import bot.outputData.EventCollector;
import bot.outputData.MessageConstructor;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegraph.exceptions.TelegraphException;

import java.beans.PropertyVetoException;


/**
 * Класс для инициализации ресурсов программы
 */
public class Initializer
{
    /**
     * Точка входа в программу
     * @param args Программные аргументы
     */
    public static void main(String[] args)
    {
        try
        {
            // Подключение к базе данных
            DBWorker.initialize();

            // Инициализация Api контекста
            ApiContextInitializer.init();

            // Регистрация бота
            Bot myBot = new Bot();
            new TelegramBotsApi().registerBot(myBot);

            // Инициализация конструктора сообщений
            MessageConstructor.initialize();

            // Запуск сборщика мероприятий
            new EventCollector();
        }
        catch (PropertyVetoException e)
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