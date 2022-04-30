package controllers;

import base.BaseTest;
import bot.Keyboards;
import bot.MealPlannerBot;
import config.ConfigRepository;
import constants.Messages;
import models.Meal;
import models.MealList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import repositories.MealPlannerRepository;
import repositories.messages.MessagesRepository;
import services.MealPlannerService;
import utils.MealUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class MealPlannerBotTest extends BaseTest {
    @Mock
    private MessagesRepository messagesRepository;
    @Mock
    private ConfigRepository configRepository;
    @Mock
    private Keyboards keyboards;
    @Mock
    private MealPlannerRepository mealPlannerRepository;
    @Mock
    private SilentSender silent;

    private MealPlannerBot bot;

    private static final String MESSAGES_JSON = "messages.json";
    private static final String TG_BOT_TOKEN = "telegramBotToken";
    private static final String TG_BOT_USERNAME = "telegramBotToken";

    private static final Long TG_USER_ID = 111111L;
    private static final Long TG_CHAT_ID = 222222L;
    private static final Integer TG_MSG_USER_ID = 333333;
    private static final Integer TG_MSG_BOT_ID = 444444;

    private final Meal YESTERDAY_MEAL = createMeal("YL1", "YL2", "YD1", 2022, 2, 14);
    private final Meal TODAY_MEAL = createMeal("TL1", null, "TD1", 2022, 2, 15);
    private final Meal TOMORROW_MEAL = createMeal("ML1", "ML2", "MD1", 2022, 2, 16);

    private final MealList LAST_WEEK_MEALS = createMeals(7, 2022, 2, 7, "L");
    private final MealList CURRENT_WEEK_MEALS = createMeals(6, 2022, 2, 14, "C");
    private final MealList NEXT_WEEK_MEALS = createMeals(5, 2022, 2, 21, "N");

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        var mealPlannerService = new MealPlannerService(mealPlannerRepository);
        bot = new MealPlannerBot(messagesRepository, configRepository, new MealUtils(), keyboards, mealPlannerService);
        bot.setSilent(silent);

        when(keyboards.getMainMenuKeyboard()).thenReturn(null);

        var msg = new Message();
        msg.setMessageId(TG_MSG_BOT_ID);
        when(silent.execute(any())).thenReturn(Optional.of(msg));
        doAnswer(invocation -> invokeSentCallback(invocation, msg))
                .when(silent).executeAsync(any(), any());

        when(configRepository.getString("telegram.bot.token")).thenReturn(TG_BOT_TOKEN);
        when(configRepository.getString("telegram.bot.username")).thenReturn(TG_BOT_USERNAME);
        when(configRepository.getLong("telegram.creatorID")).thenReturn(TG_USER_ID);

        when(messagesRepository.getMessages()).thenReturn(Collections.emptyList());

        when(mealPlannerRepository.getYesterdayMeal()).thenReturn(CompletableFuture.completedFuture(YESTERDAY_MEAL));
        when(mealPlannerRepository.getTodayMeal()).thenReturn(CompletableFuture.completedFuture(TODAY_MEAL));
        when(mealPlannerRepository.getTomorrowMeal()).thenReturn(CompletableFuture.completedFuture(TOMORROW_MEAL));

        when(mealPlannerRepository.getLastWeekMeals()).thenReturn(CompletableFuture.completedFuture(LAST_WEEK_MEALS));
        when(mealPlannerRepository.getCurrentWeekMeals()).thenReturn(CompletableFuture.completedFuture(CURRENT_WEEK_MEALS));
        when(mealPlannerRepository.getNextWeekMeals()).thenReturn(CompletableFuture.completedFuture(NEXT_WEEK_MEALS));
    }

    @AfterEach
    public void tearDown() throws IOException {
        bot.db().close();
    }

    // START ability

    @Test
    public void shouldStartBot() {
        assertNull(bot.getMenuMessage());

        MessageContext context = getContext("/start");
        bot.start().action().accept(context);

        assertEquals(TG_MSG_BOT_ID, bot.getMenuMessage());
        SendMessage message = new SendMessage(TG_CHAT_ID.toString(), Messages.START_MSG);
        message.enableMarkdownV2(true);
        message.disableNotification();
        Mockito.verify(silent).executeAsync(eq(message), any());

//        assertMessageDeleted(TG_CHAT_ID, TG_MSG_USER_ID, silent);
    }

    // DEFAULT ability

    @Test
    public void shouldGetYesterdayMeal() {
        MessageContext context = getContext(Messages.YESTERDAY_MSG);
        bot.processMsg().action().accept(context);

        var expected = getMessageFromJson("yesterday");
        Mockito.verify(silent).execute(new SendMessage(TG_CHAT_ID.toString(), expected));
    }

    @Test
    public void shouldGetTodayMeal() {
        MessageContext context = getContext(Messages.TODAY_MSG);
        bot.processMsg().action().accept(context);

        var expected = getMessageFromJson("today");
        Mockito.verify(silent).execute(new SendMessage(TG_CHAT_ID.toString(), expected));
    }

    @Test
    public void shouldGetTomorrowMeal() {
        MessageContext context = getContext(Messages.TOMORROW_MSG);
        bot.processMsg().action().accept(context);

        var expected = getMessageFromJson("tomorrow");
        Mockito.verify(silent).execute(new SendMessage(TG_CHAT_ID.toString(), expected));
    }

    @Test
    public void shouldGetLastWeekMeals() {
        MessageContext context = getContext(Messages.LAST_WEEK_MSG);
        bot.processMsg().action().accept(context);

        var expected = getMessageFromJson("lastWeek");
        Mockito.verify(silent).execute(new SendMessage(TG_CHAT_ID.toString(), expected));
    }

    @Test
    public void shouldGetCurrentWeekMeals() {
        MessageContext context = getContext(Messages.CURRENT_WEEK_MSG);
        bot.processMsg().action().accept(context);

        var expected = getMessageFromJson("currentWeek");
        Mockito.verify(silent).execute(new SendMessage(TG_CHAT_ID.toString(), expected));
    }

    @Test
    public void shouldGetNextWeekMeals() {
        MessageContext context = getContext(Messages.NEXT_WEEK_MSG);
        bot.processMsg().action().accept(context);

        var expected = getMessageFromJson("nextWeek");
        Mockito.verify(silent).execute(new SendMessage(TG_CHAT_ID.toString(), expected));
    }

    private Message getMessage(String text) {
        Message msg = new Message();
        msg.setText(text);
        msg.setChat(new Chat());
        msg.setMessageId(TG_MSG_USER_ID);
        return msg;
    }

    private MessageContext getContext(String text) {
        Message msg = getMessage(text);
        Update upd = new Update();
        upd.setMessage(msg);
        User user = new User(TG_USER_ID, "firstName", false, "lastName", "username", "en", true, true, false);
        return MessageContext.newContext(upd, user, TG_CHAT_ID, bot);
    }


    private MealList createMeals(int count, int year, int month, int day, String prefix) {
        var date = LocalDate.of(year, month, day);
        MealList meals = new MealList();
        for (int i = 0; i < count; i++) {
            meals.add(new Meal(date,
                    prefix + "L1." + i,
                    prefix + "L2." + i,
                    prefix + "D." + i,
                    false,
                    false,
                    false));
            date = date.plusDays(1);
        }
        return meals;
    }

    private Meal createMeal(String lunch1, String lunch2, String dinner, int year, int month, int day) {
        var date = LocalDate.of(year, month, day);
        return new Meal(date, lunch1, lunch2, dinner, false, false, false);
    }

    private static class TestMessages extends HashMap<String, String> {
    }

    private String getMessageFromJson(String key) {
        TestMessages messages = readJson(TestMessages.class, MESSAGES_JSON);
        return Optional.ofNullable(messages).map(m -> m.get(key)).orElseThrow(
                () -> new IllegalArgumentException("No message found for key: " + key));
    }
}
