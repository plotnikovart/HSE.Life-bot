package bot.outputData;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * Класс для разбиения пользователей на группы
 */
class UserGroups
{
    UserGroups(ResultSet usersWithEvents)
    {
        groups = new TreeMap<>();
        try
        {
            long currentUser;
            BigInteger currentGroupId = BigInteger.valueOf(0);

            usersWithEvents.next();
            currentUser = usersWithEvents.getLong(1);

            long user;
            do
            {
                user = usersWithEvents.getLong(1);

                if (user != currentUser)
                {
                    // Добавляем в пользователя в список групп
                    addUserToGroup(currentUser, currentGroupId);

                    currentUser = user;
                    currentGroupId = BigInteger.ZERO;
                }

                currentGroupId = currentGroupId.or(BigInteger.ZERO.flipBit(usersWithEvents.getShort(2) - 1));
            }
            while (usersWithEvents.next());

            // Добавление последнего пользователя
            addUserToGroup(currentUser, currentGroupId);
        }
        catch (SQLException e)
        {
        }
    }

    private void addUserToGroup(long currentUser, BigInteger currentGroupId)
    {
        LinkedList<Long> group = groups.get(currentGroupId);
        if (group == null)
        {
            // Добавляем новую группу пользователей
            LinkedList<Long> newGroup = new LinkedList<>();
            newGroup.add(currentUser);

            groups.put(currentGroupId, newGroup);
        }
        else
        {
            group.add(currentUser);
        }

        // todo getLowestSetBit(), flipBit(int n)
    }

    TreeMap<BigInteger, LinkedList<Long>> getGroups()
    {
        return groups;
    }

    private TreeMap<BigInteger, LinkedList<Long>> groups;
}

// todo Проверить, для хранения использовать List<Group>
//class Group
//{
//    ArrayList<Short> eventsSet;
//
//    void addEvent(Short event)
//    {
//        LinkedList<Short> ar = new ArrayList<>();
//        ar.get
//        eventsSet.add(event);
//    }
//
//    @Override
//    public boolean equals(Object other)
//    {
//        if (!(other instanceof Group))
//        {
//            return false;
//        }
//
//        Group group = (Group)other;
//
//        if (group.eventsSet.size() != eventsSet.size())
//        {
//            return false;
//        }
//
//        for (int i = 0; i < eventsSet.size(); i++)
//        {
//            if (!eventsSet.get(i).equals(group.eventsSet.get(i)))
//            {
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//    @Override
//    public int hashCode()
//    {
//        return 0;
//    }
//
//    //    @Override
//    //    public int compareTo(Object other)
//    //    {
//    //        Group group = (Group)other;
//    //
//    //        for(int i = 0; i< arr)
//    //    }
//}