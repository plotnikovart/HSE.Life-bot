package bot.outputData;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Класс для разбиения пользователей на группы
 */
public class Groups
{
    Groups(ResultSet resultSet, int eventsNumber)
    {
        try
        {
            long currentUser = 0;
            BigInteger currentGroupId = BigInteger.valueOf(0);

            long user;
            while (resultSet.next())
            {
                user = resultSet.getLong(1);

                if (user != currentUser)
                {
                    // Добавляем в пользователя в список групп
                    addUserToGroup(currentUser, currentGroupId);

                    currentUser = user;
                    currentGroupId = BigInteger.valueOf(0);
                }

                currentGroupId = currentGroupId.or(BigInteger.valueOf(2).pow(resultSet.getShort(2)));
            }
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