package bot.inputData;

import org.telegram.telegrambots.api.methods.send.SendMessage;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс, который организовывает древовидную структуру меню
 */
class Menu
{
    /**
     * Инициализация структуры меню
     */
    Menu()
    {
        // todo пункт меню для администратора

        // Основные пункты меню
        MenuItem b0 = new MenuItem(Functions.B0, null, 1, Functions::start, false);
        MenuItem b00 = new MenuItem(Functions.B00, b0, 5, Functions::tuneEvents, false);
        MenuItem b01 = new MenuItem(Functions.B01, b0, 10, Functions::offerEvent, false);

        // Пункт меню для ввода информации, нужен лишь для того, чтобы привязать к нему действие
        MenuItem inputMenuItem;


        // Пункты, связанные с добавлением данных пользователя

        // Выбор университета
        MenuItem b000 = new MenuItem(Functions.B000, b00, 1, Functions::setUniversity, true);
        inputMenuItem = new MenuItem(Functions.INPUT, b000, 0, Functions::inputUniversity, true);

        // Выбор мероприятий
        MenuItem b001 = new MenuItem(Functions.B001, b00, 1, Functions::setEvents, true);
        inputMenuItem = new MenuItem(Functions.INPUT, b001, 0, Functions::inputEvents, true);

        // Выбор времени
        MenuItem b002 = new MenuItem(Functions.B002, b00, 1, Functions::setTime, true);
        inputMenuItem = new MenuItem(Functions.INPUT, b002, 0, Functions::inputTime, true);

        // Отмена введенных данных и сохранение пользователя в базу данных
        MenuItem b003 = new MenuItem(Functions.B003, b00, 0, Functions::discardChanges, false);
        MenuItem b004 = new MenuItem(Functions.B004, b00, 0, Functions::saveToDatabase, false);


        // Пункты, связанные с добавлением мероприятий

        // Название мероприятия
        MenuItem b010 = new MenuItem(Functions.B010, b01, 1, Functions::setTitle, true);
        inputMenuItem = new MenuItem(Functions.INPUT, b010, 0, Functions::inputTitle, true);

        // Краткое описание мероприятия
        MenuItem b011 = new MenuItem(Functions.B011, b01, 1, Functions::setDescription, true);
        inputMenuItem = new MenuItem(Functions.INPUT, b011, 0, Functions::inputDescription, true);

        // Выбор университета
        MenuItem b012 = new MenuItem(Functions.B012, b01, 1, Functions::setUniversityE, true);
        inputMenuItem = new MenuItem(Functions.INPUT, b012, 0, Functions::inputUniversityE, true);

        // Выбор тематики мероприятия
        MenuItem b013 = new MenuItem(Functions.B013, b01, 1, Functions::setEventE, true);
        inputMenuItem = new MenuItem(Functions.INPUT, b013, 0, Functions::inputEvent, true);

        // Ссылка на фотографию
        MenuItem b014 = new MenuItem(Functions.B014, b01, 1, Functions::setPhoto, true);
        inputMenuItem = new MenuItem(Functions.INPUT, b014, 0, Functions::inputPhoto, true);

        // Ссылка на пост
        MenuItem b015 = new MenuItem(Functions.B015, b01, 1, Functions::setReference, true);
        inputMenuItem = new MenuItem(Functions.INPUT, b015, 0, Functions::inputReference, true);

        // Дата
        MenuItem b016 = new MenuItem(Functions.B016, b01, 1, Functions::setData, true);
        inputMenuItem = new MenuItem(Functions.INPUT, b016, 0, Functions::inputData, true);

        // Время
        MenuItem b017 = new MenuItem(Functions.B017, b01, 1, Functions::setTimeE, true);
        inputMenuItem = new MenuItem(Functions.INPUT, b017, 0, Functions::inputTimeE, true);

        // Место
        MenuItem b018 = new MenuItem(Functions.B018, b01, 1, Functions::setPlace, true);
        inputMenuItem = new MenuItem(Functions.INPUT, b018, 0, Functions::inputPlace, true);

        // Просмотр введенных данных
        MenuItem b019 = new MenuItem(Functions.B019, b01, 0, Functions::viewData, false);

        // Отмена введенных данных и сохранение пользователя в базу данных
        MenuItem b0190 = new MenuItem(Functions.B003, b01, 0, Functions::discardChangesE, false);
        MenuItem b0110 = new MenuItem(Functions.B004, b01, 0, Functions::saveToDatabaseE, false);

        // Пункты управления
        keyboardMenuItems = new HashMap<>(3);
        keyboardMenuItems.put(Functions.B0, b0);
        keyboardMenuItems.put(Functions.B00, b00);
        keyboardMenuItems.put(Functions.B01, b01);

        // Инициализация полей
        users = new ConcurrentHashMap<>();
        root = b0;
    }

    /**
     * Обращение к меню
     * @param message Текст сообщения
     * @param userId  Идентификатор пользователя
     * @return Ответное сообщение
     */
    SendMessage call(String message, long userId)
    {
        // Если пользователь впервые зашел в к нам, то добавляем его
        if (!users.containsKey(userId))
        {
            users.put(userId, root);
        }

        // Получение кнопки, на которой находится пользователь в данный момент
        MenuItem current = users.get(userId);

        // Сообщение 1-го приоретета
        if (message.equals("Назад"))
        {
            current = current.back();
        }
        else
        {
            // 2-го приоритета
            if (keyboardMenuItems.containsKey(message))
            {
                current = keyboardMenuItems.get(message);
            }
            else
            {
                // Пользователь находится в состоянии ввода информации
                if (current.isInputItem())
                {
                    // Не переходим на следующий пункт, но передаем в input функцию сообщение
                    return current.next(message).action(userId, message);
                }
                else
                {
                    // Переход к следующему пункту меню
                    current = current.next(message);
                }
            }
        }

        // Обновление положения пользователя
        users.put(userId, current);
        return current.action(userId);
    }

    private MenuItem root;                                // ссылка на корневой пункт меню
    private HashMap<String, MenuItem> keyboardMenuItems;  // кнопки "снизу"
    private ConcurrentHashMap<Long, MenuItem> users;      // таблица с пользователями и их состояниями
}


/**
 * Класс, описывающий поведение пункта меню при при переходе в него,
 * структуру этой пункта: название, ссылки на родителя и потомков, и различные флаги и процедуры
 */
class MenuItem
{
    /**
     * Инициализация пункта меню
     * @param name      Имя кнопки
     * @param father    Ссылка на родителя
     * @param childrenN Количество детей
     * @param func      Ссылка на функцию, вызывающиюся при открытии пункта меню
     * @param input     Флаг, пункт для ввода или нет
     */
    MenuItem(String name, MenuItem father, int childrenN, Function func, boolean input)
    {
        this.name = name;
        this.input = input;
        this.father = father;
        if (father != null)
        {
            father.addChild(this);
        }
        children = new HashMap<>(childrenN);

        this.func = func;
    }

    /**
     * Вызов функции
     * @param userId      Идентификатор пользователя
     * @param messageText Текст сообщения
     * @return Ответное сообщение
     */
    SendMessage action(long userId, String... messageText)
    {
        return func.execute(userId, messageText);
    }

    /**
     * Переход на шаг назад
     * @return Предущий пункт меню
     */
    MenuItem back()
    {
        return father != null ? father : this;
    }

    /**
     * Переход на следующий пункт меню
     * @param name Имя пункта
     * @return Следующий пункт меню
     */
    MenuItem next(String name)
    {
        // Если пункт для ввода информации, то возвращаем его ребенка для ввода
        if (input)
        {
            return children.get(Functions.INPUT);
        }

        MenuItem b = children.get(name);
        return b != null ? b : this;
    }

    /**
     * Получение типа пункта
     * @return Для ввода или нет
     */
    boolean isInputItem()
    {
        return input;
    }

    /**
     * Добавление ребенка
     * @param child Дочерний пункт меню
     */
    private void addChild(MenuItem child)
    {
        children.put(child.name, child);
    }

    private String name;                        // имя пункта меню
    private boolean input;                      // флаг, показывающий это меню для ввода информации или нет

    private Function func;                      // действие при переходе в этот пункт
    private MenuItem father;                    // ссылка на родителя
    private HashMap<String, MenuItem> children; // дети
}

/**
 * Функциональный интерфес. Используется для обработки события перехода в пункт меню
 */
interface Function
{
    SendMessage execute(long userId, String... messageText);
}