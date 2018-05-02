package bot.outputData;

import bot.Bot;
import bot.database.EnumTable;
import bot.inputData.Event;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegraph.ExecutorOptions;
import org.telegram.telegraph.TelegraphContext;
import org.telegram.telegraph.TelegraphContextInitializer;
import org.telegram.telegraph.TelegraphLogger;
import org.telegram.telegraph.api.methods.*;
import org.telegram.telegraph.api.objects.*;
import org.telegram.telegraph.exceptions.TelegraphException;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

/**
 * Конструктор сообщений
 */
public class MessageConstructor
{
    private static Account account;     // зарегистрированый аккаунт в Telegraph
    private static Bot bot;             // ссылка на бота

    /**
     * Инициализирует Telegraph.Api и регистрирует аккаунт в Telegraph
     * @param bot Ссылка на бота
     * @throws TelegraphException Если не удалось зарегистрировать аккаунт
     */
    public static void initialize(Bot bot) throws TelegraphException
    {
        MessageConstructor.bot = bot;

        // Инициализация контекста
        TelegraphLogger.setLevel(Level.ALL);
        TelegraphLogger.registerLogger(new ConsoleHandler());
        TelegraphContextInitializer.init();
        TelegraphContext.registerInstance(ExecutorOptions.class, new ExecutorOptions());

        // Регистрация аккаунта
        account = new CreateAccount("hse.life")
                .setAuthorName("HSE.Life")
                .setAuthorUrl("https://t.me/hse_life")
                .execute();
    }

    /**
     * Генератор подборок. Создает статью в Telegraph и оборачивает ее в сообщение
     * @param priorityEvents Приоритетные мероприятия
     * @param otherEvents    Остальные мероприятия
     * @return Сообщение с ссылкой на telegraph статью
     */
    static String generateArticle(LinkedList<Event> priorityEvents, LinkedList<Event> otherEvents)
    {
        try
        {
            System.out.println(Thread.currentThread());

            String messageText = "*[HSEvents]*\n\n";        // итоговое сообщение
            int i = 1;                                      // счетчик мероприятий
            List<Node> allContent = new LinkedList<>();     // контент страницы в Telegraph

            // Добавление картинки в начало подборки
            String everydayImage = EnumTable.getEverydayImage();
            allContent.add(addImage(everydayImage));

            // Приоритетные мероприятия
            if (priorityEvents != null && priorityEvents.size() != 0)
            {
                allContent.add(addNodeContent("Приоритетные мероприятия", "h3"));
                messageText += "*Приоритетные мероприятия:*\n";

                for (Event event : priorityEvents)
                {
                    addEvent(i, event, allContent);
                    messageText += i++ + ". " + event.getName() + '\n';
                }

                allContent.add(addNodeContent(null, "hr"));
                messageText += '\n';
            }

            // Остальные мероприятия
            if (otherEvents != null && otherEvents.size() != 0)
            {
                i = 1;
                allContent.add(addNodeContent("Мероприятия", "h3"));
                messageText += "*Мероприятия:*\n";

                for (Event event : otherEvents)
                {
                    addEvent(i, event, allContent);
                    messageText += i++ + ". " + event.getName() + '\n';
                }

            }

            // Получение дня недели
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            int day = c.get(Calendar.DAY_OF_WEEK);

            // Регистрация страницы в Telegraph
            Page page = new CreatePage(account.getAccessToken(), dayOfWeek[day], allContent)
                    .setAuthorName("HSE.Life")
                    .setAuthorUrl("https://t.me/hse_life")
                    .execute();

            messageText += '\n' + page.getUrl();

            //System.out.println(messageText);
            return messageText;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Отправляет сообщение пользователям
     * @param messageText Текст сообщения
     * @param users       Список пользователй
     */
    static void sendMessages(String messageText, LinkedList<Long> users)
    {
        SendMessage message = new SendMessage();
        message.enableMarkdown(true);
        message.setParseMode("markdown");
        message.setText(messageText);

        for (Long userId : users)
        {
            try
            {
                message.setChatId(userId);
                bot.execute(message);
            }
            catch (TelegramApiException e)
            {
                System.out.println("Пользователь не существует");
            }
        }
    }

    /**
     * Добавляет мероприятие в подборку
     * @param eventNumber Номер мероприятия
     * @param event       Мероприятие
     * @param allContent  Весь контент подборки
     */
    private static void addEvent(int eventNumber, Event event, List<Node> allContent)
    {
        String[] params = event.getEventParams();

        // Название
        allContent.add(addNodeContent(eventNumber + ". " + params[0], "h4"));

        // Описание
        allContent.add(addNodeContent(params[1], "p"));

        // Ссылка
        allContent.add(addUrl(params[5]));

        // Разделитель
        allContent.add(addNodeContent(null, "br"));

        // Дата
        try
        {
            Date date = new SimpleDateFormat("yyyy-mm-dd").parse(params[6]);
            allContent.add(addNodeContent("📆 " + dateFormat.format(date), "p"));
        }
        catch (ParseException e)
        {
        }

        // Время
        if (params[7] != null)
        {
            allContent.add(addNodeContent("\uD83D\uDD54 " + params[7].substring(0, 5), "p"));
        }

        // Место
        if (params[8] != null)
        {
            allContent.add(addNodeContent("\uD83D\uDCCD " + params[8], "p"));
        }

        // Фото
        allContent.add(addImage(params[4]));
    }

    /**
     * Создает узел статьи с определенным тегом
     * @param text Текст узла
     * @param tag  Тег
     * @return Узел
     */
    private static Node addNodeContent(String text, String tag)
    {
        List<Node> tagContent = null;
        if (text != null)
        {
            tagContent = new ArrayList<>();
            Node textNode = new NodeText(text);
            tagContent.add(textNode);
        }

        return new NodeElement(tag, null, tagContent);
    }

    /**
     * Создает узел статьи, прикрепляет к нему ссылку
     * @param url Ссылка
     * @return Узел
     */
    private static Node addUrl(String url)
    {
        List<Node> tagContent = new ArrayList<>();
        Node textNode = new NodeText("Подробнее");
        tagContent.add(textNode);

        HashMap<String, String> map = new HashMap<>(1);
        map.put("href", url);

        return new NodeElement("a", map, tagContent);
    }

    /**
     * Создает узел статьи с картинкой
     * @param url Ссылка на картинку
     * @return Узел
     */
    private static Node addImage(String url)
    {
        HashMap<String, String> map = new HashMap<>(1);
        map.put("src", url);
        return new NodeElement("img", map, null);
    }

    // Формат месяцев
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM", new DateFormatSymbols()
    {
        @Override
        public String[] getMonths()
        {
            return new String[] {"января", "февраля", "марта", "апреля", "мая", "июня",
                    "июля", "августа", "сентября", "октября", "ноября", "декабря"};
        }
    });

    // Дни недели
    private static String[] dayOfWeek = {"", "Воскресенье", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"};
}