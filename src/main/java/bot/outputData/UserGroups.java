package bot.outputData;

import bot.database.UsersTable;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


/**
 * Класс для разбиения пользователей на группы
 */
class UserGroups implements Iterable<UserGroup>
{
    UserGroups(int universityIndex, int timeIndex)
    {
        ResultSet usersWithEvents = UsersTable.getUsersEventsType(universityIndex, timeIndex);

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

                short currentEventIndex = usersWithEvents.getShort(2);

                if (currentEventIndex != 0)
                {
                    currentGroupId = currentGroupId.or(BigInteger.ZERO.flipBit(currentEventIndex - 1));
                }
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
        UserGroup userGroup = groups.get(currentGroupId);
        if (userGroup == null)
        {
            // Добавляем новую группу пользователей
            UserGroup newUserGroup = new UserGroup(currentGroupId);
            newUserGroup.addUserToGroup(currentUser);

            groups.put(currentGroupId, newUserGroup);
        }
        else
        {
            // Добавляем в уже существующую группу
            userGroup.addUserToGroup(currentUser);
        }
    }

    private TreeMap<BigInteger, UserGroup> groups;

    @Override
    public Iterator<UserGroup> iterator()
    {
        return groups.values().iterator();
    }
}

class UserGroup
{
    private BigInteger groupId;
    private LinkedList<Long> users;

    UserGroup(BigInteger groupId)
    {
        this.groupId = groupId;
        users = new LinkedList<>();
    }

    void addUserToGroup(long user)
    {
        users.add(user);
    }

    boolean isContained(int eventIndex)
    {
        // Если данный бит содержится, то число уменьшится
        return groupId.compareTo(groupId.flipBit(eventIndex - 1)) > 0;
    }

    boolean isZeroGroup()
    {
        return groupId.equals(BigInteger.ZERO);
    }


    LinkedList<Long> getUsersList()
    {
        return users;
    }
}