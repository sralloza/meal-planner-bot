package bot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static constants.Messages.CURRENT_WEEK_MSG;
import static constants.Messages.TWO_DAYS_AHEAD_MSG;
import static constants.Messages.TOW_DAYS_AGO_MSG;
import static constants.Messages.LAST_WEEK_MSG;
import static constants.Messages.NEXT_WEEK_MSG;
import static constants.Messages.TODAY_MSG;
import static constants.Messages.TOMORROW_MSG;
import static constants.Messages.TWO_WEEKS_AGO_MSG;
import static constants.Messages.TWO_WEEKS_AHEAD_MSG;
import static constants.Messages.YESTERDAY_MSG;

public class Keyboards {
    public ReplyKeyboardMarkup getMainMenuKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(TOW_DAYS_AGO_MSG);
        row.add(YESTERDAY_MSG);
        row.add(TODAY_MSG);
        row.add(TOMORROW_MSG);
        row.add(TWO_DAYS_AHEAD_MSG);

        keyboard.add(row);
        row = new KeyboardRow();
        row.add(TWO_WEEKS_AGO_MSG);
        row.add(LAST_WEEK_MSG);
        row.add(CURRENT_WEEK_MSG);
        row.add(NEXT_WEEK_MSG);
        row.add(TWO_WEEKS_AHEAD_MSG);
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setInputFieldPlaceholder("Type your message");
        keyboardMarkup.setResizeKeyboard(true);

        return keyboardMarkup;
    }
}
