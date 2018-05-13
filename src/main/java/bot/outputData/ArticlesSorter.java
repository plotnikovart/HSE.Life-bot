package bot.outputData;

import bot.Bot;
import bot.inputData.Event;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Класс-сортировщик мероприятий
 */
class ArticlesSorter
{
    /**
     * Сортирует мероприятия университета по группам, для каждой группы определяет приоритетные и остальные мероприятия.
     * Передает готовые наборы в конструктор сообщений и отправляет пользователям результат
     * @param actualUniversityEvents Список актуальных мероприятий
     * @param userGroups             Группы пользователей
     */
    static void set(ActualUniversityEvents actualUniversityEvents, UserGroups userGroups)
    {
        LinkedList<Event> priorityEvents = new LinkedList<>();  // приоритетные мероприятия
        LinkedList<Event> otherEvents = new LinkedList<>();     // остальные мероприятия

        Iterator<UserGroup> groupIterator = userGroups.iterator();
        while (groupIterator.hasNext())
        {
            UserGroup userGroup = groupIterator.next();

            if (userGroup.isZeroGroup())
            {
                otherEvents = actualUniversityEvents.getEvents();
            }
            else
            {
                Iterator<Event> eventsIterator = actualUniversityEvents.iterator();
                while (eventsIterator.hasNext())
                {
                    Event event = eventsIterator.next();
                    if (userGroup.isContained(event.getType()))
                    {
                        priorityEvents.add(event);
                    }
                    else
                    {
                        otherEvents.add(event);
                    }
                }
            }

            // Формирование текста сообщения
            String messageText = MessageConstructor.generateMessage(priorityEvents, otherEvents);

            // Отправка сообщения пользователям
            for (Long userId : userGroup.getUsersList())
            {
                Bot.sendM(userId, messageText);
            }

            // Очистка мероприятий для данной группы пользователей
            priorityEvents = new LinkedList<>();
            otherEvents = new LinkedList<>();
        }
    }
}