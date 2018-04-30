package bot.outputData;


import bot.inputData.Event;

import java.util.Iterator;
import java.util.LinkedList;

class ArticlesSorter
{
    static void set(ActualUniversityEvents actualUniversityEvents, UserGroups userGroups)
    {
        LinkedList<Event> priorityEvents = new LinkedList<>();
        LinkedList<Event> otherEvents = new LinkedList<>();

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

            String messageText = MessageConstructor.generate(priorityEvents, otherEvents);
            MessageConstructor.sendMessages(messageText, userGroup.getUsersList());

            priorityEvents = new LinkedList<>();
            otherEvents = new LinkedList<>();
        }
    }
}