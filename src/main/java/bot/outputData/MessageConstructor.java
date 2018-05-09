package bot.outputData;

import bot.database.EnumTable;
import bot.inputData.Event;
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
    /**
     * Инициализирует Telegraph.Api и регистрирует аккаунт в Telegraph
     * @throws TelegraphException Если не удалось зарегистрировать аккаунт
     */
    public static void initialize() throws TelegraphException
    {
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
     * Оборачивает статью Telegraph в сообщение
     * @param priorityEvents   Приоритетные мероприятия
     * @param otherEvents      Остальные мероприятия
     * @return Готовое для отправки сообщение
     */
    static String generateMessage(LinkedList<Event> priorityEvents, LinkedList<Event> otherEvents)
    {
        StringBuilder messageText = new StringBuilder("*[HSEvents]*\n\n");

        // Приоритетные мероприятия
        if (priorityEvents != null && priorityEvents.size() != 0)
        {
            int i = 1;
            messageText.append("*Приоритетные мероприятия:*\n");

            for (Event event : priorityEvents)
            {
                messageText.append(i).append(". ").append(event.getName()).append('\n');
                i++;
            }

            messageText.append('\n');
        }

        // Остальные мероприятия
        if (otherEvents != null && otherEvents.size() != 0)
        {
            int i = 1;
            messageText.append("*Мероприятия:*\n");

            for (Event event : otherEvents)
            {
                messageText.append(i).append(". ").append(event.getName()).append('\n');
                i++;
            }
        }

        // Получение ссылки на статью
        String articleReference = generateArticle(priorityEvents, otherEvents);
        messageText.append("\n").append(articleReference);

        return messageText.toString();
    }

    /**
     * Генератор подборок. Создает статью в Telegraph
     * @param priorityEvents Приоритетные мероприятия
     * @param otherEvents    Остальные мероприятия
     * @return Ссылка на Telegraph статью
     */
    private static String generateArticle(LinkedList<Event> priorityEvents, LinkedList<Event> otherEvents)
    {
        try
        {
            List<Node> allContent = new LinkedList<>();     // контент страницы в Telegraph

            // Добавление картинки в начало подборки
            String everydayImage = EnumTable.getEverydayImage();
            allContent.add(addImage(everydayImage));

            // Приоритетные мероприятия
            if (priorityEvents != null && priorityEvents.size() != 0)
            {
                int i = 1;
                allContent.add(addNodeContent("Приоритетные мероприятия", "h3"));

                for (Event event : priorityEvents)
                {
                    addEvent(i, event, allContent);
                    i++;
                }

                allContent.add(addNodeContent(null, "hr"));
            }

            // Остальные мероприятия
            if (otherEvents != null && otherEvents.size() != 0)
            {
                int i = 1;
                allContent.add(addNodeContent("Мероприятия", "h3"));

                for (Event event : otherEvents)
                {
                    addEvent(i, event, allContent);
                    i++;
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

            return page.getUrl();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
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
            e.printStackTrace();
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


    private static Account account;     // зарегистрированый аккаунт в Telegraph

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
    private static String[] dayOfWeek = {"", "Воскресенье", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница",
                                         "Суббота"};
}