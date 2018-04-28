package bot.inputData;

import bot.database.EnumTable;
import bot.database.UsersTable;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Вспомогательный класс со статическими функциями - действиями при нажатии на кнопки
 */
class Functions
{
    // Названия/содержимое кнопок
    static final String B0 = "/start";
    static final String B00 = "\uD83D\uDD27 Настроить мероприятия";
    static final String B01 = "\uD83D\uDE4B Предложить мероприятие";

    static final String B000 = "\uD83C\uDFDB Ваш ВУЗ";
    static final String B001 = "\uD83E\uDD47 Приорететные мероприятия";
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

    // Готовые шаблоны "тяжелых" сообщений
    private static volatile SendMessage startM;

    private static volatile SendMessage tuneEventsM;
    private static volatile SendMessage setUniversityM;
    private static volatile SendMessage setEventsM;
    private static volatile SendMessage setTimeM;

    private static volatile SendMessage offerEventM;
    private static volatile SendMessage setUniversityEM;
    private static volatile SendMessage setEventsEM;


    static SendMessage start(String... mes)
    {
        // При нажатии на кнопку /start, данные о пользователе заносятся в базу со стандартными настройками
        UsersTable.addDefaultUser(Integer.parseInt(mes[1]));

        if (startM != null)
            return startM;

        synchronized (Function.class)
        {
            if (startM != null)
                return startM;

            startM = new SendMessage();
            startM.enableMarkdown(true);
            startM.setText("Вы можете персонально настроить мероприятия, которые хотите получать.\n\nТакже вы можете предложить мероприятие для публикации");

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

    static SendMessage tuneEvents(String... mes)
    {
        if (tuneEventsM != null)
            return tuneEventsM;

        synchronized (Function.class)
        {
            if (tuneEventsM != null)
                return tuneEventsM;

            tuneEventsM = new SendMessage();
            tuneEventsM.setParseMode("markdown");
            tuneEventsM.enableMarkdown(true);
            tuneEventsM.setText("Теперь настройте информацию под себя!\nУкажите предпочтительные *типы мероприятий*, *места*, которые вам будет удобно посещать, и *время* для получения подборок");

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

    static SendMessage setUniversity(String... mes)
    {
        if (setUniversityM != null && !EnumTable.isChanged("university_list"))
            return setUniversityM;

        synchronized (Function.class)
        {
            if (setUniversityM != null)
                return setUniversityM;

            setUniversityM = new SendMessage();
            setUniversityM.setText("Выберите ваш университет:\n");

            addUniversities(setUniversityM);   // добавление в сообщение списка университетов
        }

        return setUniversityM;
    }

    static SendMessage inputUniversity(String... mes)
    {
        SendMessage message = new SendMessage();
        inputReply(message, mes[0]);

        // Обновление данных пользователя
        Users.setUniversity(mes[0], Integer.parseInt(mes[1]));

        return message;
    }

    static SendMessage setEvents(String... mes)
    {
        if (setEventsM != null && !EnumTable.isChanged("event_type_list"))
            return setEventsM;

        synchronized (Function.class)
        {
            if (setEventsM != null)
                return setEventsM;

            setEventsM = new SendMessage();
            setEventsM.setText("Выберите темы мероприятий, которые будут показываться вам в начале подборки:");

            addEvents(setEventsM);     // добавление в сообщение списка тем мероприятий
        }

        return setEventsM;
    }

    static SendMessage inputEvents(String... mes)
    {
        SendMessage message = new SendMessage();
        message.setParseMode("markdown");
        message.enableMarkdown(true);

        // Обновление данных пользователя
        String s = Users.setEvents(mes[0], Integer.parseInt(mes[1]));

        message.setText("Вы выбрали: *" + s + "*" +
                "\n\nДля *отмены* еще раз нажмите на мероприятие" +
                "\nВы можете указать *несколько* мероприятий");

        addBackButton(message);

        return message;
    }

    static SendMessage setTime(String... mes)
    {
        if (setTimeM != null && !EnumTable.isChanged("time_list"))
            return setTimeM;

        synchronized (Function.class)
        {
            if (setTimeM != null)
                return setTimeM;

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

    static SendMessage inputTime(String... mes)
    {
        SendMessage message = new SendMessage();
        message.setParseMode("markdown");
        message.enableMarkdown(true);

        // Обновление данных пользователя
        Users.setTime(mes[0], Integer.parseInt(mes[1]));

        message.setText("Вы выбрали: *" + mes[0] + "*" +
                "\n\nВы можете изменить свой выбор");

        addBackButton(message);

        return message;
    }

    static SendMessage discardChanges(String... mes)
    {
        SendMessage message = new SendMessage();
        message.setText("Вы отменили все изменения!\n\n" +
                "Ваш профиль: "); // TODO просмотр профиля из базы данных

        Users.deleteUser(Integer.parseInt(mes[1])); // удаление пользователя из временного хранилища

        return message;
    }

    static SendMessage saveToDatabase(String... mes)
    {
        SendMessage message = new SendMessage();
        message.setParseMode("markdown");
        message.enableMarkdown(true);

        message.setText("Ваши данные обновлены!\n\n" +
                Users.getUserInfo(Integer.parseInt(mes[1])));

        Users.downloadUserToDatabase(Integer.parseInt(mes[1]));

        return message;
    }

    ////////////////////////////////////////////////////
    // Добавление мероприятия
    ////////////////////////////////////////////////////

    static SendMessage offerEvent(String... mes)
    {
        if (offerEventM != null)
            return offerEventM;

        synchronized (Function.class)
        {
            if (offerEventM != null)
                return offerEventM;

            offerEventM = new SendMessage();
            offerEventM.setText("Здесь вы можете предложить свое мероприятие.\nПосле проверки модераторами оно будет доступно читателям");

            // Inline keyboard
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            // Добавление строк
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(new InlineKeyboardButton().setText(B010).setCallbackData(B010));

            List<InlineKeyboardButton> row2 = new ArrayList<>();
            row2.add(new InlineKeyboardButton().setText(B011).setCallbackData(B011));

            List<InlineKeyboardButton> row3 = new ArrayList<>();
            row3.add(new InlineKeyboardButton().setText(B012).setCallbackData(B012));
            row3.add(new InlineKeyboardButton().setText(B013).setCallbackData(B013));

            List<InlineKeyboardButton> row4 = new ArrayList<>();
            row4.add(new InlineKeyboardButton().setText(B014).setCallbackData(B014));
            row4.add(new InlineKeyboardButton().setText(B015).setCallbackData(B015));

            List<InlineKeyboardButton> row5 = new ArrayList<>();
            row5.add(new InlineKeyboardButton().setText(B016).setCallbackData(B016));
            row5.add(new InlineKeyboardButton().setText(B017).setCallbackData(B017));
            row5.add(new InlineKeyboardButton().setText(B018).setCallbackData(B018));

            List<InlineKeyboardButton> row6 = new ArrayList<>();
            row6.add(new InlineKeyboardButton().setText(B003).setCallbackData(B003));
            row6.add(new InlineKeyboardButton().setText(B004).setCallbackData(B004));

            // Добавление строк в клавиатуру
            rowsInline.add(row1);
            rowsInline.add(row2);
            rowsInline.add(row3);
            rowsInline.add(row4);
            rowsInline.add(row5);
            rowsInline.add(row6);

            // Установка клавиатуры
            markupInline.setKeyboard(rowsInline);
            offerEventM.setReplyMarkup(markupInline);
        }

        return offerEventM;
    }

    static SendMessage setTitle(String... mes)
    {
        SendMessage message = new SendMessage();
        message.setText("Введите название мероприятия");

        return message;
    }

    static SendMessage inputTitle(String... mes)
    {
        SendMessage message = new SendMessage();
        textInputReply(message, mes[0]);

        Events.setEvent(B010, mes[0], Integer.parseInt(mes[1]));

        return message;
    }

    static SendMessage setDescription(String... mes)
    {
        SendMessage message = new SendMessage();
        message.setText("Введите краткое описание мероприятия");

        return message;
    }

    static SendMessage inputDescription(String... mes)
    {
        SendMessage message = new SendMessage();
        textInputReply(message, mes[0]);

        Events.setEvent(B011, mes[0], Integer.parseInt(mes[1]));

        return message;
    }

    static SendMessage setUniversityE(String... mes)
    {
        if (setUniversityEM != null && !EnumTable.isChanged("university_list"))
            return setUniversityEM;

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

    static SendMessage inputUniversityE(String... mes)
    {
        SendMessage message = new SendMessage();
        inputReply(message, mes[0]);

        Events.setEvent(B012, mes[0], Integer.parseInt(mes[1]));

        return message;
    }

    static SendMessage setEventsE(String... mes)
    {
        if (setEventsEM != null && !EnumTable.isChanged("event_type_list"))
            return setEventsEM;

        synchronized (Function.class)
        {
            if (setEventsEM != null)
                return setEventsEM;

            setEventsEM = new SendMessage();
            setEventsEM.setText("Введите тематику вашего мероприятия");

            addEvents(setEventsEM);
        }

        return setEventsEM;
    }

    static SendMessage inputEvent(String... mes)
    {
        SendMessage message = new SendMessage();
        inputReply(message, mes[0]);

        Events.setEvent(B013, mes[0], Integer.parseInt(mes[1]));

        return message;
    }

    static SendMessage setPhoto(String... mes)
    {
        SendMessage message = new SendMessage();
        message.setText("Добавьте URL ссылку на фотографию");

        return message;
    }

    static SendMessage inputPhoto(String... mes)
    {
        SendMessage message = new SendMessage();
        textInputReply(message, mes[0]);

        message.setText(message.getText() +
                "\n\n❗Проверьте, что ваша картика отображается, в противном случае мы не сможем сформировать ваш пост❗");

        Events.setEvent(B014, mes[0], Integer.parseInt(mes[1]));

        return message;
    }

    static SendMessage setLink(String... mes)
    {
        SendMessage message = new SendMessage();
        message.setText("Добавьте ссылку на пост в социальной сети");

        return message;
    }

    static SendMessage inputLink(String... mes)
    {
        SendMessage message = new SendMessage();
        textInputReply(message, mes[0]);

        Events.setEvent(B015, mes[0], Integer.parseInt(mes[1]));

        return message;
    }

    static SendMessage setData(String... mes)
    {
        SendMessage message = new SendMessage();
        message.setParseMode("markdown");
        message.enableMarkdown(true);

        message.setText("Введите дату мероприятия в данном формате: *ГГГГ-ММ-ДД*");

        return message;
    }

    static SendMessage inputData(String... mes)
    {
        SendMessage message = new SendMessage();

        // Проверка формата ввода (ГГГГ-ММ-ДД)
        boolean flag = true;
        if (mes[0].length() == 10)
        {
            for (int i = 0; i < mes[0].length() && flag; i++)
            {
                char ch = mes[0].charAt(i);
                if (!((ch >= '0' && ch <= '9') || ((i == 4 || i == 7) && ch == '-')))
                {
                    flag = false;
                }
            }
        }
        else
        {
            flag = false;
        }

        if (flag)
        {
            textInputReply(message, mes[0]);
            Events.setEvent(Functions.B016, mes[0], Integer.parseInt(mes[1]));
        }
        else
        {
            message.setParseMode("markdown");
            message.enableMarkdown(true);
            message.setText("Вы ввели некорректные данные! Повторите ввод: *ГГГГ-ММ-ДД*");
        }

        return message;
    }

    static SendMessage setTimeE(String... mes)
    {
        SendMessage message = new SendMessage();
        message.setParseMode("markdown");
        message.enableMarkdown(true);

        message.setText("Введите время начала мероприятия в данном формате: *ЧЧ:ММ*");

        return message;
    }

    static SendMessage inputTimeE(String... mes)
    {
        SendMessage message = new SendMessage();

        // Проверка формата ввода (ЧЧ:ММ)
        boolean flag = true;
        if (mes[0].length() == 5)
        {
            for (int i = 0; i < mes[0].length() && flag; i++)
            {
                char ch = mes[0].charAt(i);
                if (!((ch >= '0' && ch <= '9') || (i == 2 && ch == ':')))
                {
                    flag = false;
                }
            }
        }

        if (flag)
        {
            textInputReply(message, mes[0]);
            Events.setEvent(Functions.B017, mes[0], Integer.parseInt(mes[1]));
        }
        else
        {
            message.setParseMode("markdown");
            message.enableMarkdown(true);
            message.setText("Вы ввели некорректные данные! Повторите ввод: *ММ:ЧЧ*");
        }

        return message;
    }

    static SendMessage setPlace(String... mes)
    {
        SendMessage message = new SendMessage();

        message.setText("Введите адрес мероприятия");

        return message;
    }

    static SendMessage inputPlace(String... mes)
    {
        SendMessage message = new SendMessage();

        textInputReply(message, mes[0]);

        Events.setEvent(Functions.B018, mes[0], Integer.parseInt(mes[1]));

        return message;
    }

    static SendMessage discardChangesE(String... mes)
    {
        SendMessage message = new SendMessage();
        message.setText("Вы отменили добавление нового мероприятия!");

        Events.deleteEvent(Integer.parseInt(mes[1])); // удаление мероприятия из временного хранилища

        return message;
    }

    static SendMessage saveToDatabaseE(String... mes)
    {
        SendMessage message = new SendMessage();
        message.setParseMode("markdown");
        message.enableMarkdown(true);

        try
        {
            String info = Events.getEventInfo(Integer.parseInt(mes[1]));
            Events.downloadEventToDatabase(Integer.parseInt(mes[1]));
            message.setText("Вы добавили новое мероприятие!\n\n" + info);
        }
        catch (SQLException e)
        {
            addBackButton(message);
            message.setText("Вы ввели неполные данные! *Обязательные* для заполнения поля:\nНазвание\nОписание" +
                    "\nУниверситет\nТематика\nСсылка на фотографию\nСсылка на пост\nДата");
        }

        return message;
    }

    ////////////////////////////////////////////////////

    private static void textInputReply(SendMessage message, String text)
    {
        message.setParseMode("markdown");
        message.enableMarkdown(true);

        message.setText("Вы ввели: *" + text + "*" +
                "\n\nВы можете изменить данные путем повторного ввода");

        addBackButton(message);
    }

    private static void inputReply(SendMessage message, String text)
    {
        message.setParseMode("markdown");
        message.enableMarkdown(true);

        // Обновление данных мероприятий

        message.setText("Вы выбрали: *" + text + "*" +
                "\n\nВы можете изменить свой выбор");

        addBackButton(message);
    }

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