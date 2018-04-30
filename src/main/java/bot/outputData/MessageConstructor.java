package bot.outputData;

import bot.Bot;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

public class MessageConstructor
{
    private static Account account;
    private static Bot bot;

    public static void initialize(Bot bot) throws TelegraphException
    {
        MessageConstructor.bot = bot;

        TelegraphLogger.setLevel(Level.ALL);
        TelegraphLogger.registerLogger(new ConsoleHandler());
        TelegraphContextInitializer.init();
        TelegraphContext.registerInstance(ExecutorOptions.class, new ExecutorOptions());

        account = new CreateAccount("hse.life")
                .setAuthorName("HSE.Life")
                .setAuthorUrl("https://t.me/hse_life")
                .execute();
    }

    static String generate(LinkedList<Event> priorityEvents, LinkedList<Event> otherEvents)
    {
        try
        {
            String messageText = "";
            int i = 1;
            List<Node> allContent = new LinkedList<>();

            // Приоритетные мероприятия
            if (priorityEvents != null && priorityEvents.size() != 0)
            {
                allContent.add(addNodeContent("Приоритетные мероприятия", "h3"));
                messageText += "Приоритетные мероприятия:\n";

                for (Event event : priorityEvents)
                {
                    addEvent(event, allContent);
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
                messageText += "Мероприятия:\n";

                for (Event event : otherEvents)
                {
                    addEvent(event, allContent);
                    messageText += i++ + ". " + event.getName() + '\n';
                }

            }

            // Формирование страницы
            Page page = new CreatePage(account.getAccessToken(), "Подборка", allContent)
                    .setAuthorName("HSE.Life")
                    .setAuthorUrl("https://t.me/hse_life")
                    .execute();

            messageText += '\n' + page.getUrl();

            System.out.println(messageText);
            return messageText;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    static void sendMessages(String messageText, LinkedList<Long> users)
    {
        SendMessage message = new SendMessage();
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

    private static void addEvent(Event event, List<Node> allContent) throws Exception
    {
        String[] params = event.getEventDescription();

        // Название
        allContent.add(addNodeContent(params[0], "h4"));

        // Описание
        allContent.add(addNodeContent(params[1], "p"));

        // Ссылка
        allContent.add(addUrl(params[5]));

        // Разделитель
        allContent.add(addNodeContent(null, "br"));

        // Дата
        Date date = new SimpleDateFormat("yyyy-mm-dd").parse(params[6]);
        allContent.add(addNodeContent("📆 " + dateFormat.format(date), "p"));

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

    private static Node addUrl(String url)
    {
        List<Node> tagContent = new ArrayList<>();
        Node textNode = new NodeText("Подробнее");
        tagContent.add(textNode);

        HashMap<String, String> map = new HashMap<>(1);
        map.put("href", url);

        return new NodeElement("a", map, tagContent);
    }

    private static Node addImage(String url)
    {
        HashMap<String, String> map = new HashMap<>(1);
        map.put("src", url);
        return new NodeElement("img", map, null);
    }

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM", new DateFormatSymbols()
    {
        @Override
        public String[] getMonths()
        {
            return new String[] {"января", "февраля", "марта", "апреля", "мая", "июня",
                    "июля", "августа", "сентября", "октября", "ноября", "декабря"};
        }
    });
}