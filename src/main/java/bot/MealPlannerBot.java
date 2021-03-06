package bot;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import constants.Messages;
import lombok.Getter;
import lombok.SneakyThrows;
import models.Meal;
import models.MealList;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;
import repositories.messages.MessagesRepository;
import services.MealPlannerService;
import utils.MealUtils;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static constants.Messages.CURRENT_WEEK_MSG;
import static constants.Messages.LAST_WEEK_MSG;
import static constants.Messages.NEXT_WEEK_MSG;
import static constants.Messages.TITLE_MAP;
import static constants.Messages.TODAY_MSG;
import static constants.Messages.TOMORROW_MSG;
import static constants.Messages.TOW_DAYS_AGO_MSG;
import static constants.Messages.TWO_DAYS_AHEAD_MSG;
import static constants.Messages.TWO_WEEKS_AGO_MSG;
import static constants.Messages.TWO_WEEKS_AHEAD_MSG;
import static constants.Messages.YESTERDAY_MSG;
import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.CREATOR;

public class MealPlannerBot extends BaseMealPlannerBot {
    private final Long creatorId;
    private final MealUtils mealUtils;
    private final Keyboards keyboards;

    @Getter
    private Integer menuMessage;

    @Inject
    public MealPlannerBot(MessagesRepository messagesRepository,
                          Config config,
                          MealUtils mealUtils,
                          Keyboards keyboards,
                          MealPlannerService mealPlannerService) {
        super(config.getString("telegram.bot.token"),
                config.getString("telegram.bot.username"),
                messagesRepository,
                keyboards,
                mealPlannerService);

        creatorId = config.getLong("telegram.creatorID");
        this.mealUtils = mealUtils;
        this.keyboards = keyboards;
    }

    @Override
    public long creatorId() {
        return creatorId;
    }

    public Ability processMsg() {
        return Ability.builder()
                .name(DEFAULT)
                .action(this::processMsg)
                .enableStats()
                .locality(USER)
                .privacy(CREATOR)
                .build();
    }

    public Ability start() {
        return Ability.builder()
                .name("start")
                .info("Starts the bot")
                .locality(USER)
                .privacy(CREATOR)
                .action(this::sendMenuAndClearMessages)
                .build();
    }

    private void sendMenuAndClearMessages(MessageContext ctx) {
        deleteUserMessage(ctx);
        deleteBotMessages(ctx.chatId());
        recreateMenuMessage(ctx);
    }

    private void recreateMenuMessage(MessageContext ctx) {
        Optional.ofNullable(menuMessage)
                .ifPresent(n -> deleteMessageAsyncById(ctx.chatId(), n));
        sendMenuAsync(ctx);
    }

    private void sendSingleMeal(Meal meal, String msg, Long chatId) {
        var message = Optional.ofNullable(meal)
                .map(m -> msg + ":\n" + mealUtils.toString(m))
                .orElse("Could not find " + msg.toLowerCase());

        sendMessageMarkdown(message, chatId);
    }

    private void sendMealList(MealList meals, String msg, Long chatId) {
        var message = Optional.ofNullable(meals)
                .map(m -> msg + ":\n" + mealUtils.toString(m))
                .orElse("Could not find " + msg.toLowerCase());

        sendMessageMarkdown(message, chatId);
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
        String title = TITLE_MAP.get(userMessage);
        switch (userMessage) {
            case TOW_DAYS_AGO_MSG:
            case YESTERDAY_MSG:
            case TODAY_MSG:
            case TOMORROW_MSG:
            case TWO_DAYS_AHEAD_MSG:
                sendSingleMeal((Meal) future.get(), title, ctx.chatId());
                break;
            case TWO_WEEKS_AGO_MSG:
            case LAST_WEEK_MSG:
            case CURRENT_WEEK_MSG:
            case NEXT_WEEK_MSG:
            case TWO_WEEKS_AHEAD_MSG:
                sendMealList((MealList) future.get(), title, ctx.chatId());
                break;
            default:
        }
    }

    private void sendMenuAsync(MessageContext ctx) {
        SendMessage msg = new SendMessage();
        msg.setText(Messages.START_MSG);
        msg.disableNotification();
        msg.enableMarkdownV2(true);
        msg.setChatId(Long.toString(ctx.chatId()));
        msg.setReplyMarkup(keyboards.getMainMenuKeyboard());

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
}
