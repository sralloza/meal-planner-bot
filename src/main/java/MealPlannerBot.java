import com.google.inject.Inject;
import config.Config;
import lombok.SneakyThrows;
import models.Meal;
import models.MealList;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;
import services.MealPlannerService;
import utils.annotations.AbilityMark;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import static contants.Messages.CURRENT_WEEK_MSG;
import static contants.Messages.DEFAULT_CALLBACK;
import static contants.Messages.LAST_WEEK_MSG;
import static contants.Messages.NEXT_WEEK_MSG;
import static contants.Messages.TODAY_MSG;
import static contants.Messages.TOMORROW_MSG;
import static contants.Messages.YESTERDAY_MSG;
import static org.telegram.abilitybots.api.db.MapDBContext.offlineInstance;
import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.CREATOR;

public class MealPlannerBot extends AbilityBot {
    private final Long creatorId;
    private final MealPlannerService service;
    private final Queue<Integer> botMessages;
    private Integer menuMessage;

    @Inject
    public MealPlannerBot(MealPlannerService service, Config config) {
        super(config.getenv("TELEGRAM_BOT_TOKEN"),
                config.getenv("TELEGRAM_BOT_USERNAME"),
                offlineInstance("db"));

        String telegramTokenBot = config.getenv("TELEGRAM_BOT_TOKEN");
        if (Optional.ofNullable(telegramTokenBot).orElse("").isEmpty()) {
            throw new IllegalArgumentException("Token is empty");
        }
        creatorId = Optional.ofNullable(config.getenv("TELEGRAM_CREATOR_ID"))
                .map(Long::parseLong)
                .orElseThrow(() -> new IllegalArgumentException("Creator id is empty"));

        this.service = service;
        this.botMessages = new ArrayDeque<>();
    }

    @Override
    public long creatorId() {
        return creatorId;
    }

    @AbilityMark
    public Ability processMsg() {
        return Ability.builder()
                .name(DEFAULT)
                .action(this::processMsg)
                .enableStats()
                .locality(USER)
                .privacy(CREATOR)
                .build();
    }

    @AbilityMark
    public Ability start() {
        return Ability.builder()
                .name("start")
                .info("Starts the bot")
                .locality(USER)
                .privacy(CREATOR)
                .action(this::sendMenuAsync)
                .build();
    }

    public void sendAndRegister(String msgStr, Long chatId) {
        SendMessage msg = new SendMessage();
        msg.setText(msgStr);
        msg.setChatId(chatId.toString());
        msg.setReplyMarkup(getMainMenuKeyboard());

        var r = silent.execute(msg);
        if (r.isEmpty()) {
            System.out.println("Error sending message");
        } else {
            botMessages.add(r.get().getMessageId());
        }
    }

    private void deleteMessageAsyncById(Long chatId, Integer messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId.toString());
        deleteMessage.setMessageId(messageId);

        silent.executeAsync(deleteMessage, DEFAULT_CALLBACK);
    }

    private void deleteUserMessage(MessageContext ctx) {
        deleteMessageAsyncById(ctx.chatId(), ctx.update().getMessage().getMessageId());
    }

    private void deleteBotMessages(Long chatId) {
        while (!botMessages.isEmpty()) {
            Integer messageId = botMessages.poll();
            deleteMessageAsyncById(chatId, messageId);
        }
    }

    private void recreateMenuMessage(MessageContext ctx) {
        Optional.ofNullable(menuMessage)
                .ifPresent(integer -> deleteMessageAsyncById(ctx.chatId(), integer));
        sendMenuAsync(ctx);
    }

    private String mealsToString(List<Meal> meals) {
        return String.join("\n", meals.stream()
                .filter(Objects::nonNull)
                .map(this::mealToString)
                .toArray(String[]::new));
    }

    private String mealToString(Meal meal) {
        if (meal == null) {
            return "Could not find meal";
        }
        return "- " + meal.getWeekday() + ": " + meal.toReadableString();
    }

    private void sendSingleMeal(Meal meal, String msg, Long chatId) {
        if (meal == null) {
            sendAndRegister("Could not find " + msg.toLowerCase(), chatId);
            return;
        }
        sendAndRegister(msg + ":\n" + mealToString(meal), chatId);
    }

    private void sendMealList(MealList meals, String msg, Long chatId) {
        if (meals.isEmpty()) {
            sendAndRegister("Could not find " + msg.toLowerCase(), chatId);
            return;
        }
        sendAndRegister(msg + ":\n" + mealsToString(meals), chatId);
    }

    @SneakyThrows
    private void processMsg(MessageContext ctx) {
        // 1. Get data as a completable future
        String userMessage = ctx.update().getMessage().getText();
        CompletableFuture<?> future = service.getCompletableFutureByUserMessage(userMessage);

        // 2. Recreate menu message, remove bot and user messages
        recreateMenuMessage(ctx);
        deleteBotMessages(ctx.chatId());
        deleteUserMessage(ctx);

        //   3. Send data to user
        switch (userMessage) {
            case YESTERDAY_MSG:
                sendSingleMeal((Meal) future.get(), "Yesterday's meal", ctx.chatId());
                break;
            case TODAY_MSG:
                sendSingleMeal((Meal) future.get(), "Today's meal", ctx.chatId());
                break;
            case TOMORROW_MSG:
                sendSingleMeal((Meal) future.get(), "Tomorrow's meal", ctx.chatId());
                break;
            case LAST_WEEK_MSG:
                sendMealList((MealList) future.get(), "Last week's meals", ctx.chatId());
                break;
            case CURRENT_WEEK_MSG:
                sendMealList((MealList) future.get(), "Current week's meals", ctx.chatId());
                break;
            case NEXT_WEEK_MSG:
                sendMealList((MealList) future.get(), "Next week's meals", ctx.chatId());
                break;
            default:
                sendMenuAsync(ctx);
                break;
        }
    }

    private void sendMenuAsync(MessageContext ctx) {
        SendMessage msg = new SendMessage();
        msg.setText("Select the command:");
        msg.setChatId(Long.toString(ctx.chatId()));
        msg.setReplyMarkup(getMainMenuKeyboard());

        SentCallback<Message> callback = new SentCallback<>() {
            @Override
            public void onResult(BotApiMethod method, Message response) {
                menuMessage = response.getMessageId();
            }

            @Override
            public void onError(BotApiMethod method, TelegramApiRequestException apiException) {
            }

            @Override
            public void onException(BotApiMethod method, Exception exception) {
            }
        };
        silent.executeAsync(msg, callback);
        deleteUserMessage(ctx);
    }

    private ReplyKeyboardMarkup getMainMenuKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(YESTERDAY_MSG);
        row.add(TODAY_MSG);
        row.add(TOMORROW_MSG);
        keyboard.add(row);
        row = new KeyboardRow();
        row.add(LAST_WEEK_MSG);
        row.add(CURRENT_WEEK_MSG);
        row.add(NEXT_WEEK_MSG);
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setInputFieldPlaceholder("Type your message");
        keyboardMarkup.setResizeKeyboard(true);

        return keyboardMarkup;
    }
}