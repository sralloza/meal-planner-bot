package bot;

import com.google.inject.Inject;
import config.ConfigRepository;
import lombok.SneakyThrows;
import models.Meal;
import models.MealList;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;
import services.MealPlannerService;
import services.MessagesSenderService;
import utils.MealUtils;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static constants.Messages.CURRENT_WEEK_MSG;
import static constants.Messages.LAST_WEEK_MSG;
import static constants.Messages.NEXT_WEEK_MSG;
import static constants.Messages.TITLE_MAP;
import static constants.Messages.TODAY_MSG;
import static constants.Messages.TOMORROW_MSG;
import static constants.Messages.YESTERDAY_MSG;
import static org.telegram.abilitybots.api.db.MapDBContext.offlineInstance;
import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.CREATOR;

public class MealPlannerBot extends AbilityBot {
    private final Long creatorId;
    private final MealPlannerService service;
    private final MealUtils mealUtils;
    private final MessagesSenderService sender;
    private final Keyboards keyboards;
    private Integer menuMessage;

    @Inject
    public MealPlannerBot(MealPlannerService service,
                          ConfigRepository config,
                          MealUtils mealUtils,
                          MessagesSenderService sender,
                          Keyboards keyboards) {
        super(config.getString("telegram.bot.token"),
                config.getString("telegram.bot.username"),
                offlineInstance("db"));

        creatorId = Optional.ofNullable(config.getString("telegram.creatorID"))
                .map(Long::parseLong)
                .orElseThrow(() -> new IllegalArgumentException("Creator id is empty"));

        this.service = service;
        this.mealUtils = mealUtils;
        this.keyboards = keyboards;
        this.sender = sender;
        sender.setSender(silent);
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
                .action(this::sendMenuAsync)
                .build();
    }

    private void recreateMenuMessage(MessageContext ctx) {
        Optional.ofNullable(menuMessage)
                .ifPresent(n -> sender.deleteMessageAsyncById(ctx.chatId(), n));
        sendMenuAsync(ctx);
    }

    private void sendSingleMeal(Meal meal, String msg, Long chatId) {
        var message = Optional.ofNullable(meal)
                .map(m -> msg + ":\n" + mealUtils.toString(m))
                .orElse("Could not find " + msg.toLowerCase());

        sender.sendMessage(message, chatId);
    }

    private void sendMealList(MealList meals, String msg, Long chatId) {
        var message = Optional.ofNullable(meals)
                .map(m -> msg + ":\n" + mealUtils.toString(m))
                .orElse("Could not find " + msg.toLowerCase());

        sender.sendMessage(message, chatId);
    }

    @SneakyThrows
    private void processMsg(MessageContext ctx) {
        // 1. Get data as a completable future
        String userMessage = ctx.update().getMessage().getText();
        CompletableFuture<?> future = service.getCompletableFutureByUserMessage(userMessage);

        // 2. Recreate menu message, remove bot and user messages
        recreateMenuMessage(ctx);
        sender.deleteBotMessages(ctx.chatId());
        sender.deleteUserMessage(ctx);

        //   3. Send data to user
        String title = TITLE_MAP.get(userMessage);
        switch (userMessage) {
            case YESTERDAY_MSG:
            case TODAY_MSG:
            case TOMORROW_MSG:
                sendSingleMeal((Meal) future.get(), title, ctx.chatId());
                break;
            case LAST_WEEK_MSG:
            case CURRENT_WEEK_MSG:
            case NEXT_WEEK_MSG:
                sendMealList((MealList) future.get(), title, ctx.chatId());
                break;
            default:
        }
    }

    private void sendMenuAsync(MessageContext ctx) {
        SendMessage msg = new SendMessage();
        msg.setText("*API connected*");
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
        sender.deleteUserMessage(ctx);
    }
}