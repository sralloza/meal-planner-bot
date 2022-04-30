package constants;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;

import java.util.Map;

public class Messages {
    public static final String START_MSG = "*API connected*";

    public static final String TOW_DAYS_AGO_MSG = "⏮️";
    public static final String YESTERDAY_MSG = "⏪";
    public static final String TODAY_MSG = "▶️";
    public static final String TOMORROW_MSG = "⏩";
    public static final String TWO_DAYS_AHEAD_MSG = "⏭️";

    public static final String TWO_WEEKS_AGO_MSG = "🏳️";
    public static final String LAST_WEEK_MSG = "🏴‍☠️️";
    public static final String CURRENT_WEEK_MSG = "🚩";
    public static final String NEXT_WEEK_MSG = "🏁";
    public static final String TWO_WEEKS_AHEAD_MSG = "🏴";

    public static final Callback DEFAULT_CALLBACK = new Callback();

    public static final Map<String, String> TITLE_MAP = Map.of(
            TOW_DAYS_AGO_MSG, "Two days ago meal",
            YESTERDAY_MSG, "Yesterday meal",
            TODAY_MSG, "Today meal",
            TOMORROW_MSG, "Tomorrow meal",
            TWO_DAYS_AHEAD_MSG, "Two days ahead meal",

            TWO_WEEKS_AGO_MSG, "Two weeks ago meals",
            LAST_WEEK_MSG, "Last week meals",
            CURRENT_WEEK_MSG, "Current week meals",
            NEXT_WEEK_MSG, "Next week meals",
            TWO_WEEKS_AHEAD_MSG, "Two weeks ahead meals");

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
