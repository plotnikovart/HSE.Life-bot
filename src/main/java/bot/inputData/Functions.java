package bot.inputData;

import bot.database.EnumTable;
import bot.database.UsersTable;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.util.*;

/**
 * Вспомогательный класс со статическими функциями - действиями при переходе в пункт меню
 */
class Functions
{
    // Названия пунктов
    static final String B0 = "/start";
    static final String B00 = "\uD83D\uDD27 Настроить мероприятия";
    static final String B01 = "\uD83D\uDE4B Предложить мероприятие";

    static final String INPUT = "input";

    static final String B000 = "\uD83C\uDFDB Ваш ВУЗ";
    static final String B001 = "\uD83E\uDD47 Приоритетные мероприятия";
    static final String B002 = "⏰ Время для получения подборки";

    static final String B003 = "❎ Отменить";
    static final String B004 = "✅ Сохранить";

    static final String B010 = "\uD83C\uDFA4 Название мероприятия";
    static final String B011 = "\uD83D\uDC68\u200D\uD83D\uDCBB️ Краткое описание";
    static final String B012 = "\uD83C\uDFDB Университет";
    static final String B013 = "\uD83C\uDFAD Тематика";
    static final String B014 = "\uD83D\uDCF8 Фотография";
    static final String B015 = "\uD83C\uDF10 Ссылка на пост";
    static final String B016 = "\uD83D\uDCC6 Дата";
    static final String B017 = "\uD83D\uDD54 Время";
    static final String B018 = "\uD83D\uDCCD Место";
    static final String B019 = "\uD83D\uDD0E Посмотреть введенные данные \uD83D\uDD0D";

    // Готовые шаблоны "тяжелых" сообщений
    private static volatile SendMessage startM;

    private static volatile SendMessage tuneEventsM;
    private static volatile SendMessage setUniversityM;
    private static volatile SendMessage setEventsM;
    private static volatile SendMessage setTimeM;

    private static volatile SendMessage offerEventM;
    private static volatile SendMessage setUniversityEM;
    private static volatile SendMessage setEventsEM;


    /**
     * Пункт B0
     */
    static SendMessage start(long userId, String... messageText)
    {
        // При нажатии на кнопку /start, данные о пользователе заносятся в базу со стандартными настройками
        UsersTable.addDefaultUser(userId);

        if (startM != null)
        {
            return startM;
        }

        synchronized (Function.class)
        {
            if (startM != null)
            {
                return startM;
            }

            startM = getMarkdownMessage();
            startM.setText("Вы можете персонально *настроить* мероприятия, которые хотите получать.\n\nТакже вы можете *предложить* мероприятие для публикации");

            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            keyboardMarkup.setOneTimeKeyboard(true);
            keyboardMarkup.setResizeKeyboard(true);
            List<KeyboardRow> keyboard = new ArrayList<>();

            KeyboardRow row = new KeyboardRow();

            row.add(B00);
            keyboard.add(row);

            row = new KeyboardRow();
            row.add(B01);
            keyboard.add(row);

            keyboardMarkup.setKeyboard(keyboard);
            startM.setReplyMarkup(keyboardMarkup);
        }

        return startM;
    }

    ////////////////////////////////////////////////////
    // Добавление данных пользователя
    ////////////////////////////////////////////////////

    /**
     * Пункт B00. Настройка мероприятий
     */
    static SendMessage tuneEvents(long userId, String... messageText)
    {
        if (tuneEventsM != null)
        {
            return tuneEventsM;
        }

        synchronized (Function.class)
        {
            if (tuneEventsM != null)
            {
                return tuneEventsM;
            }

            tuneEventsM = getMarkdownMessage();
            tuneEventsM.setText("Теперь настройте информацию под себя!\n" +
                    "Укажите предпочтительную *тематику* мероприятий, свой *университет*, и *время* для получения подборок");

            // Inline keyboard
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            // Добавление строк
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(new InlineKeyboardButton().setText(B000).setCallbackData(B000));

            List<InlineKeyboardButton> row2 = new ArrayList<>();
            row2.add(new InlineKeyboardButton().setText(B001).setCallbackData(B001));

            List<InlineKeyboardButton> row3 = new ArrayList<>();
            row3.add(new InlineKeyboardButton().setText(B002).setCallbackData(B002));

            List<InlineKeyboardButton> row4 = new ArrayList<>();
            row4.add(new InlineKeyboardButton().setText(B003).setCallbackData(B003));
            row4.add(new InlineKeyboardButton().setText(B004).setCallbackData(B004));

            // Добавление строк в клавиатуру
            rowsInline.add(row1);
            rowsInline.add(row2);
            rowsInline.add(row3);
            rowsInline.add(row4);

            // Установка клавиатуры
            markupInline.setKeyboard(rowsInline);
            tuneEventsM.setReplyMarkup(markupInline);
        }

        return tuneEventsM;
    }

    /**
     * Пункт B000. Настройка университета
     */
    static SendMessage setUniversity(long userId, String... messageText)
    {
        if (setUniversityM != null && !EnumTable.isChanged("university_list"))
        {
            return setUniversityM;
        }

        synchronized (Function.class)
        {
            if (setUniversityM != null)
            {
                return setUniversityM;
            }

            setUniversityM = new SendMessage();
            setUniversityM.setText("Выберите ваш университет:\n");

            addUniversities(setUniversityM);   // добавление в сообщение списка университетов
        }

        return setUniversityM;
    }

    /**
     * Ввод университета
     */
    static SendMessage inputUniversity(long userId, String... messageText)
    {
        // Обновление данных пользователя
        Users.setUniversity(messageText[0], userId);

        return getInputReplyMessage(messageText[0]);
    }

    /**
     * Пункт B001. Настройка мероприятий
     */
    static SendMessage setEvents(long userId, String... messageText)
    {
        if (setEventsM != null && !EnumTable.isChanged("event_type_list"))
        {
            return setEventsM;
        }

        synchronized (Function.class)
        {
            if (setEventsM != null)
            {
                return setEventsM;
            }

            setEventsM = new SendMessage();
            setEventsM.setText("Выберите темы мероприятий, которые будут показываться вам в начале подборки:");

            addEvents(setEventsM);     // добавление в сообщение списка тем мероприятий
        }

        return setEventsM;
    }

    /**
     * Ввод мероприятий
     */
    static SendMessage inputEvents(long userId, String... messageText)
    {
        // Обновление данных пользователя
        String s = Users.setEvents(messageText[0], userId);

        SendMessage message = getMarkdownMessage();
        message.setText("Вы выбрали: *" + s + "*" +
                "\n\nВы можете указать *несколько* мероприятий" +
                "\nДля *отмены* еще раз нажмите на мероприятие");

        addBackButton(message);

        return message;
    }

    /**
     * Пункт B002. Настройка времени
     */
    static SendMessage setTime(long userId, String... messageText)
    {
        if (setTimeM != null && !EnumTable.isChanged("time_list"))
        {
            return setTimeM;
        }

        synchronized (Function.class)
        {
            if (setTimeM != null)
            {
                return setTimeM;
            }

            setTimeM = new SendMessage();
            setTimeM.setText("Выберите удобное время для получения подборки с мероприятиями:");

            // Inline keyboard
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            // Добавление строк
            ArrayList<String> mas = EnumTable.getTimeList();
            List<InlineKeyboardButton> row;
            for (int i = 0; i < mas.size(); i++)
            {
                row = new ArrayList<>();
                row.add(new InlineKeyboardButton().setText(mas.get(i)).setCallbackData(mas.get(i)));
                i++;
                row.add(new InlineKeyboardButton().setText(mas.get(i)).setCallbackData(mas.get(i)));
                rowsInline.add(row);
            }

            row = new ArrayList<>();
            row.add(new InlineKeyboardButton().setText("Назад").setCallbackData("Назад"));
            rowsInline.add(row);

            // Установка клавиатуры
            markupInline.setKeyboard(rowsInline);
            setTimeM.setReplyMarkup(markupInline);
        }

        return setTimeM;
    }

    /**
     * Ввод времени
     */
    static SendMessage inputTime(long userId, String... messageText)
    {
        // Обновление данных пользователя
        Users.setTime(messageText[0], userId);

        return getInputReplyMessage(messageText[0]);
    }

    /**
     * Пункт B003. Отмена мероприятий
     */
    static SendMessage discardChanges(long userId, String... messageText)
    {
        Users.deleteUser(userId); // удаление пользователя из временного хранилища

        SendMessage message = new SendMessage();
        message.setText("Вы отменили все изменения!");

        return message;
    }

    /**
     * Пункт B004. Сохранение в базу
     */
    static SendMessage saveToDatabase(long userId, String... messageText)
    {
        SendMessage message = getMarkdownMessage();

        try
        {
            String userInfo = Users.getUserInfo(userId);
            Users.downloadUserToDatabase(userId);
            message.setText("Ваши данные обновлены!\n\n" + userInfo);
        }
        catch (SQLException e)
        {
            message.setText("*Вы ввели некорректные данные*\nПожалуйста выбирайте информацию по кнопкам");
            addBackButton(message);
        }

        return message;
    }

    ////////////////////////////////////////////////////
    // Добавление мероприятия
    ////////////////////////////////////////////////////

    /**
     * Пункт B01. Добавление мероприятия
     */
    static SendMessage offerEvent(long userId, String... messageText)
    {
        if (offerEventM != null)
        {
            return offerEventM;
        }

        synchronized (Function.class)
        {
            if (offerEventM != null)
            {
                return offerEventM;
            }

            offerEventM = new SendMessage();
            offerEventM.setText("Здесь вы можете предложить свое мероприятие.\nПосле проверки модераторами оно будет доступно читателям");

            // Inline keyboard
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>(7);

            // Добавление строк
            List<InlineKeyboardButton> row1 = new ArrayList<>(2);
            row1.add(new InlineKeyboardButton().setText(B010).setCallbackData(B010));

            List<InlineKeyboardButton> row2 = new ArrayList<>(2);
            row2.add(new InlineKeyboardButton().setText(B011).setCallbackData(B011));

            List<InlineKeyboardButton> row3 = new ArrayList<>(2);
            row3.add(new InlineKeyboardButton().setText(B012).setCallbackData(B012));
            row3.add(new InlineKeyboardButton().setText(B013).setCallbackData(B013));

            List<InlineKeyboardButton> row4 = new ArrayList<>(2);
            row4.add(new InlineKeyboardButton().setText(B014).setCallbackData(B014));
            row4.add(new InlineKeyboardButton().setText(B015).setCallbackData(B015));

            List<InlineKeyboardButton> row5 = new ArrayList<>(3);
            row5.add(new InlineKeyboardButton().setText(B016).setCallbackData(B016));
            row5.add(new InlineKeyboardButton().setText(B017).setCallbackData(B017));
            row5.add(new InlineKeyboardButton().setText(B018).setCallbackData(B018));

            List<InlineKeyboardButton> row6 = new ArrayList<>(1);
            row6.add(new InlineKeyboardButton().setText(B019).setCallbackData(B019));

            List<InlineKeyboardButton> row7 = new ArrayList<>(2);
            row7.add(new InlineKeyboardButton().setText(B003).setCallbackData(B003));
            row7.add(new InlineKeyboardButton().setText(B004).setCallbackData(B004));

            // Добавление строк в клавиатуру
            rowsInline.add(row1);
            rowsInline.add(row2);
            rowsInline.add(row3);
            rowsInline.add(row4);
            rowsInline.add(row5);
            rowsInline.add(row6);
            rowsInline.add(row7);

            // Установка клавиатуры
            markupInline.setKeyboard(rowsInline);
            offerEventM.setReplyMarkup(markupInline);
        }

        return offerEventM;
    }

    /**
     * Пункт B010. Название мероприятия
     */
    static SendMessage setTitle(long userId, String... messageText)
    {
        SendMessage message = new SendMessage();
        message.setText("Введите название мероприятия");

        return message;
    }

    /**
     * Ввод названия
     */
    static SendMessage inputTitle(long userId, String... messageText)
    {
        Events.setEventParam(0, messageText[0], userId);

        return getInputReplyMessage(messageText[0]);
    }

    /**
     * Пункт B011. Добавление описания
     */
    static SendMessage setDescription(long userId, String... messageText)
    {
        SendMessage message = new SendMessage();
        message.setText("Введите краткое описание мероприятия");

        return message;
    }

    /**
     * Ввод описания
     */
    static SendMessage inputDescription(long userId, String... messageText)
    {
        Events.setEventParam(1, messageText[0], userId);

        return getInputReplyMessage(messageText[0]);
    }

    /**
     * Пункт B012. Выбор университета
     */
    static SendMessage setUniversityE(long userId, String... messageText)
    {
        if (setUniversityEM != null && !EnumTable.isChanged("university_list"))
        {
            return setUniversityEM;
        }

        synchronized (Function.class)
        {
            if (setUniversityEM != null)
            {
                return setUniversityEM;
            }

            setUniversityEM = new SendMessage();
            setUniversityEM.setText("Выберите университет вашей студенческой организации");

            addUniversities(setUniversityEM);   // добавление в сообщение списка университетов
        }

        return setUniversityEM;
    }

    /**
     * Ввод университета
     */
    static SendMessage inputUniversityE(long userId, String... messageText)
    {
        Events.setEventParam(2, messageText[0], userId);

        return getInputReplyMessage(messageText[0]);
    }

    /**
     * Пункт B013. Выбор типа мероприятия
     */
    static SendMessage setEventsE(long userId, String... messageText)
    {
        if (setEventsEM != null && !EnumTable.isChanged("event_type_list"))
        {
            return setEventsEM;
        }

        synchronized (Function.class)
        {
            if (setEventsEM != null)
            {
                return setEventsEM;
            }

            setEventsEM = new SendMessage();
            setEventsEM.setText("Введите тематику вашего мероприятия");

            addEvents(setEventsEM);
        }

        return setEventsEM;
    }

    /**
     * Ввод типа мероприятия
     */
    static SendMessage inputEvent(long userId, String... messageText)
    {
        Events.setEventParam(3, messageText[0], userId);

        return getInputReplyMessage(messageText[0]);
    }

    /**
     * Пункт B014. Установка ссылки на фотографию
     */
    static SendMessage setPhoto(long userId, String... messageText)
    {
        SendMessage message = new SendMessage();
        message.setText("Добавьте URL ссылку на фотографию");

        return message;
    }

    /**
     * Ввод ссылки на фотографию
     */
    static SendMessage inputPhoto(long userId, String... messageText)
    {
        Events.setEventParam(4, messageText[0], userId);

        SendMessage message = getInputReplyMessage(messageText[0]);
        message.setText(message.getText() +
                "\n\n❗Проверьте, что ваша картика отображается, в противном случае мы не сможем сформировать ваш пост❗");

        return message;
    }

    /**
     * Пункт B015. Добавление ссылки на пост
     */
    static SendMessage setLink(long userId, String... messageText)
    {
        SendMessage message = new SendMessage();
        message.setText("Добавьте ссылку на пост в социальной сети");

        return message;
    }

    /**
     * Ввод ссылки на пост
     */
    static SendMessage inputLink(long userId, String... messageText)
    {
        Events.setEventParam(5, messageText[0], userId);

        return getInputReplyMessage(messageText[0]);
    }

    /**
     * Пункт B016. Установка даты
     */
    static SendMessage setData(long userId, String... messageText)
    {
        SendMessage message = getMarkdownMessage();
        message.setText("Введите дату мероприятия в данном формате: *ГГГГ-ММ-ДД*");

        return message;
    }

    /**
     * Ввод даты
     */
    static SendMessage inputData(long userId, String... messageText)
    {
        SendMessage message = getInputReplyMessage(messageText[0]);

        // Проверка формата ввода (ГГГГ-ММ-ДД)
        try
        {
            Date.valueOf(messageText[0]);
            Events.setEventParam(6, messageText[0], userId);
        }
        catch (IllegalArgumentException e)
        {
            message.setText("Вы ввели некорректные данные! Повторите ввод: *ГГГГ-ММ-ДД*");
        }

        return message;
    }

    /**
     * Пункт B017. Добавление времени
     */
    static SendMessage setTimeE(long userId, String... messageText)
    {
        SendMessage message = getMarkdownMessage();
        message.setText("Введите время начала мероприятия в данном формате: *ЧЧ:ММ*");

        return message;
    }

    /**
     * Ввод времени
     */
    static SendMessage inputTimeE(long userId, String... messageText)
    {
        SendMessage message = getInputReplyMessage(messageText[0]);

        // Проверка формата ввода (ЧЧ:ММ)
        try
        {
            Time.valueOf(messageText[0] + ":00");
            Events.setEventParam(7, messageText[0], userId);
        }
        catch (IllegalArgumentException e)
        {
            message.setText("Вы ввели некорректные данные! Повторите ввод: *ЧЧ:ММ*");
        }

        return message;
    }

    /**
     * Пункт B018. Добавление места
     */
    static SendMessage setPlace(long userId, String... messageText)
    {
        SendMessage message = new SendMessage();
        message.setText("Введите адрес мероприятия");

        return message;
    }

    /**
     * Ввод места
     */
    static SendMessage inputPlace(long userId, String... messageText)
    {
        Events.setEventParam(8, messageText[0], userId);

        return getInputReplyMessage(messageText[0]);
    }

    /**
     * Просмотр информации, которую уже ввели
     */
    static SendMessage viewData(long userId, String... messageText)
    {
        SendMessage message = getMarkdownMessage();
        addBackButton(message);

        String info = Events.getEventInfo(userId);
        if (info.equals(""))
        {
            info += "Вы пока еще ничего не ввели";
        }
        message.setText(info);

        return message;
    }

    /**
     * Отмена добавления мероприятия
     */
    static SendMessage discardChangesE(long userId, String... messageText)
    {
        Events.deleteEvent(Integer.parseInt(messageText[1])); // удаление мероприятия из временного хранилища

        SendMessage message = new SendMessage();
        message.setText("Вы отменили добавление нового мероприятия!");

        return message;
    }

    /**
     * Сохранение мероприятия в БД
     */
    static SendMessage saveToDatabaseE(long userId, String... messageText)
    {
        SendMessage message = getMarkdownMessage();

        try
        {
            String info = Events.getEventInfo(userId);
            Events.downloadEventToDatabase(userId);
            message.setText("Вы добавили новое мероприятие!\n\n" + info);
        }
        catch (SQLException e)
        {
            message.setText(e.getMessage());
            addBackButton(message);
        }

        return message;
    }

    ////////////////////////////////////////////////////

    /**
     * Шаблон сообщения ответа ввод данных
     */
    private static SendMessage getInputReplyMessage(String text)
    {
        SendMessage message = getMarkdownMessage();

        message.setText("Вы выбрали: *" + text + "*" +
                "\n\nВы можете изменить данные путем повторного ввода");
        addBackButton(message);

        return message;
    }

    /**
     * Получение сообщения, с настроенной Markdown разметкой
     */
    private static SendMessage getMarkdownMessage()
    {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setParseMode("markdown");

        return sendMessage;
    }

    /**
     * Добавление в сообщение списка университетов
     */
    private static void addUniversities(SendMessage message)
    {
        // Inline keyboard
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Добавление строк
        ArrayList<String> mas = EnumTable.getUniversityList();
        List<InlineKeyboardButton> row;
        for (String university : mas)
        {
            row = new ArrayList<>();
            row.add(new InlineKeyboardButton().setText(university).setCallbackData(university));
            rowsInline.add(row);
        }

        row = new ArrayList<>();
        row.add(new InlineKeyboardButton().setText("Назад").setCallbackData("Назад"));
        rowsInline.add(row);

        // Установка клавиатуры
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
    }

    /**
     * Добавление в сообщение списка тем мероприятий
     */
    private static void addEvents(SendMessage message)
    {
        // Inline keyboard
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Добавление строк
        ArrayList<String> mas = EnumTable.getEventTypeList();
        List<InlineKeyboardButton> row;
        for (String event_type : mas)
        {
            row = new ArrayList<>();
            row.add(new InlineKeyboardButton().setText(event_type).setCallbackData(event_type));
            rowsInline.add(row);
        }

        row = new ArrayList<>();
        row.add(new InlineKeyboardButton().setText("Назад").setCallbackData("Назад"));
        rowsInline.add(row);

        // Установка клавиатуры
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
    }

    /**
     * Добавление в сообщение кнопки "Готово" ("Назад")
     */
    private static void addBackButton(SendMessage message)
    {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(new InlineKeyboardButton().setText("Готово").setCallbackData("Назад"));
        rowsInline.add(row);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
    }
}