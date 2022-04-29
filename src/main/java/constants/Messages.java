package constants;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;

import java.util.Map;

public class Messages {
    public static final String START_MSG = "*API connected*";

    public static final String TODAY_MSG = "\uD83D\uDCC5 Today";
    public static final String TOMORROW_MSG = "\uD83C\uDF81 Tomorrow";
    public static final String YESTERDAY_MSG = "\uD83D\uDCC6 Yesterday";

    public static final String CURRENT_WEEK_MSG = "\uD83D\uDCF0 This Week";
    public static final String NEXT_WEEK_MSG = "\uD83D\uDCE6 Next Week";
    public static final String LAST_WEEK_MSG = "\uD83C\uDF39 Last Week";

    public static final Callback DEFAULT_CALLBACK = new Callback();

    public static final Map<String, String> TITLE_MAP = Map.of(
            YESTERDAY_MSG, "Yesterday's meal",
            TODAY_MSG, "Today's meal",
            TOMORROW_MSG, "Tomorrow's meal",
            LAST_WEEK_MSG, "Last week's meals",
            CURRENT_WEEK_MSG, "Current week's meals",
            NEXT_WEEK_MSG, "Next week's meals");

    private static class Callback implements SentCallback<Boolean> {
        @Override
        public void onResult(BotApiMethod method, Boolean response) {
        }

        @Override
        public void onError(BotApiMethod method, TelegramApiRequestException apiException) {
        }

        @Override
        public void onException(BotApiMethod method, Exception exception) {
        }
    }
}
