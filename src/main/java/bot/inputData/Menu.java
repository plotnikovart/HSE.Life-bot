package bot.inputData;

import org.telegram.telegrambots.api.methods.send.SendMessage;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Класс, который организовывает древовидную структуру меню
 */
class Menu
{
    // Инициализация кнопок меню
    Menu()
    {
        // todo кнопка с просмотром введенной информации

        // Основные кнопки
        Button b0 = new Button(Functions.B0, null, 1, Functions::start, false);
        Button b00 = new Button(Functions.B00, b0, 5, Functions::tuneEvents, false);
        Button b01 = new Button(Functions.B01, b0, 10, Functions::offerEvent, false);

        // Кнопка для ввода информации, нужна лишь для того, чтобы привязать к ней действие
        Button inputButton;


        // Кнопки, связанные с добавлением данных пользователя

        // Выбор университета
        Button b000 = new Button(Functions.B000, b00, 1, Functions::setUniversity, true);
        inputButton = new Button("inputButton", b000, 0, Functions::inputUniversity, true);

        // Выбор мероприятий
        Button b001 = new Button(Functions.B001, b00, 1, Functions::setEvents, true);
        inputButton = new Button("inputButton", b001, 0, Functions::inputEvents, true);

        // Выбор времени
        Button b002 = new Button(Functions.B002, b00, 1, Functions::setTime, true);
        inputButton = new Button("inputButton", b002, 0, Functions::inputTime, true);

        // Отмена введенных данных и сохранение пользователя в базу данных
        Button b003 = new Button(Functions.B003, b00, 0, Functions::discardChanges, false);
        Button b004 = new Button(Functions.B004, b00, 0, Functions::saveToDatabase, false);


        // Кнопки, связанные с добавлением мероприятий

        // Название мероприятия
        Button b010 = new Button(Functions.B010, b01, 1, Functions::setTitle, true);
        inputButton = new Button("inputButton", b010, 0, Functions::inputTitle, true);

        // Краткое описание мероприятия
        Button b011 = new Button(Functions.B011, b01, 1, Functions::setDescription, true);
        inputButton = new Button("inputButton", b011, 0, Functions::inputDescription, true);

        // Выбор университета
        Button b012 = new Button(Functions.B012, b01, 1, Functions::setUniversityE, true);
        inputButton = new Button("inputButton", b012, 0, Functions::inputUniversityE, true);

        // Выбор тематики мероприятия
        Button b013 = new Button(Functions.B013, b01, 1, Functions::setEventsE, true);
        inputButton = new Button("inputButton", b013, 0, Functions::inputEvent, true);

        // Ссылка на фотографию
        Button b014 = new Button(Functions.B014, b01, 1, Functions::setPhoto, true);
        inputButton = new Button("inputButton", b014, 0, Functions::inputPhoto, true);

        // Ссылка на пост
        Button b015 = new Button(Functions.B015, b01, 1, Functions::setLink, true);
        inputButton = new Button("inputButton", b015, 0, Functions::inputLink, true);

        // Дата
        Button b016 = new Button(Functions.B016, b01, 1, Functions::setData, true);
        inputButton = new Button("inputButton", b016, 0, Functions::inputData, true);

        // Время
        Button b017 = new Button(Functions.B017, b01, 1, Functions::setTimeE, true);
        inputButton = new Button("inputButton", b017, 0, Functions::inputTimeE, true);

        // Место
        Button b018 = new Button(Functions.B018, b01, 1, Functions::setPlace, true);
        inputButton = new Button("inputButton", b018, 0, Functions::inputPlace, true);

        // Отмена введенных данных и сохранение пользователя в базу данных
        Button b019 = new Button(Functions.B003, b01, 0, Functions::discardChangesE, false);
        Button b0110 = new Button(Functions.B004, b01, 0, Functions::saveToDatabaseE, false);

        // Кнопки управления
        keyboardButtons = new HashMap<>(3);
        keyboardButtons.put(Functions.B0, b0);
        keyboardButtons.put(Functions.B00, b00);
        keyboardButtons.put(Functions.B01, b01);

        // Инициализация полей
        users = new ConcurrentHashMap<>();
        root = b0;
    }

    SendMessage call(String message, long userId)
    {
        if (!users.containsKey(userId))
        {
            users.put(userId, root);
        }

        Button current = users.get(userId);

        if (message.equals("Назад"))
        {
            current = current.back();
        }
        else
        {
            if (keyboardButtons.containsKey(message))
            {
                current = keyboardButtons.get(message);
            }
            else
            {
                // Пользователь находится в состоянии ввода информации
                if (current.getInput())
                {
                    return current.next(message).action(message, String.valueOf(userId));
                }
                else
                {
                    current = current.next(message);
                }
            }
        }

        users.put(userId, current);
        return current.action(message, String.valueOf(userId));
    }

    private Button root;
    private HashMap<String, Button> keyboardButtons;
    private ConcurrentHashMap<Long, Button> users;
}


/**
 * Класс, описывающий поведение кнопки при нажатии,
 * структуру этой кнопки: название этой кнопки, ссылки на ее родителей и потомков и различные флаги и процедуры
 */
class Button
{
    Button(String buttonName, Button father, int childrenN, Function func, boolean input)
    {
        this.name = buttonName;
        this.input = input;
        this.father = father;
        if (father != null) father.addChild(this);
        children = new HashMap<>(childrenN);

        this.func = func;
    }

    SendMessage action(String... message)
    {
        return func.execute(message);
    }

    Button back()
    {
        return father != null ? father : this;
    }

    Button next(String name)
    {
        Button b = children.get(name);
        if (b == null)
        {
            b = children.get("inputButton");
        }

        return b != null ? b : this;
    }

    boolean getInput()
    {
        return input;
    }

    private void addChild(Button child)
    {
        children.put(child.name, child);
    }

    private String name;
    private boolean input;

    private Function func;
    private Button father;
    private HashMap<String, Button> children;
}

/**
 * Функциональный интерфес. Используется для обработки события нажатия на кнопку
 */
interface Function
{
    SendMessage execute(String... message);
}