package bot.outputData;

import bot.database.UsersTable;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


/**
 * Класс для разбиения пользователей одного университета на группы
 */
class UserGroups implements Iterable<UserGroup>
{
    /**
     * Инициализирует группы пользователей
     * @param universityIndex Индекс университета
     * @param timeIndex       Временной индекс
     */
    UserGroups(int universityIndex, int timeIndex)
    {
        // Получение подходящих пользователей
        ResultSet usersWithEvents = UsersTable.getUsersEventsType(universityIndex, timeIndex);
        groups = new TreeMap<>();

        try
        {
            long currentUser;
            BigInteger currentGroupId = BigInteger.valueOf(0);

            // Пустая подборка
            if (!usersWithEvents.next())
            {
                return;
            }
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

    /**
     * Добавление пользователя в группу
     * @param currentUser    Идентификатор пользователя
     * @param currentGroupId Идентификатор группы пользователя
     */
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

    /**
     * Проверка, есть ли группы пользователей
     * @return Есть или нет
     */
    boolean isEmpty()
    {
        return groups.isEmpty();
    }

    // Контейнер. Содержит значения групп пользователей и сами группы
    private TreeMap<BigInteger, UserGroup> groups;

    @Override
    public Iterator<UserGroup> iterator()
    {
        return groups.values().iterator();
    }
}


/**
 * Класс, представляет собой группу пользователй
 */
class UserGroup
{
    private BigInteger groupId;     // идентификатор группы
    private LinkedList<Long> users; // список пользователей

    /**
     * Инициилизирует группу
     * @param groupId Идентификатор группы
     */
    UserGroup(BigInteger groupId)
    {
        this.groupId = groupId;
        users = new LinkedList<>();
    }

    /**
     * Добавление пользователя к группе
     * @param user Идентификатор пользователя
     */
    void addUserToGroup(long user)
    {
        users.add(user);
    }

    /**
     * Проверка, содержится ли индекс типа мероприятия в данной группе
     * @param eventIndex Индекс мероприятия
     * @return Содержится или нет
     */
    boolean isContained(int eventIndex)
    {
        // Если данный бит содержится, то число уменьшится
        return groupId.compareTo(groupId.flipBit(eventIndex - 1)) > 0;
    }

    /**
     * Проверка, равен ли идентификатор группы нулю
     * @return Равен или нет
     */
    boolean isZeroGroup()
    {
        return groupId.equals(BigInteger.ZERO);
    }


    /**
     * Получение списка пользователей
     */
    LinkedList<Long> getUsersList()
    {
        return users;
    }
}