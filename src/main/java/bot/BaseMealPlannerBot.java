package bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import repositories.messages.MessagesRepository;
import services.MealPlannerService;

import static constants.Messages.DEFAULT_CALLBACK;
import static org.telegram.abilitybots.api.db.MapDBContext.offlineInstance;

@Slf4j
public abstract class BaseMealPlannerBot extends AbilityBot {
    private final MessagesRepository messagesRepository;
    private final Keyboards keyboards;
    protected final MealPlannerService service;

    protected BaseMealPlannerBot(String botToken,
                                 String botUsername,
                                 MessagesRepository messagesRepository,
                                 Keyboards keyboards,
                                 MealPlannerService service) {
        super(botToken, botUsername, offlineInstance("db"));
        this.messagesRepository = messagesRepository;
        this.keyboards = keyboards;
        this.service = service;
    }

    public void setSilent(SilentSender silentSender) {
        this.silent = silentSender;
    }

    protected void sendMessageMarkdown(String msgStr, Long chatId) {
        msgStr = msgStr.replace("-", "\\-")
                .replace("|", "\\|");
        log.debug("Sending message with markdownV2 enabled: {}", msgStr);
        SendMessage msg = new SendMessage();
        msg.setText(msgStr);
        msg.enableMarkdownV2(true);
        msg.setChatId(chatId.toString());
        msg.setReplyMarkup(keyboards.getMainMenuKeyboard());

        var r = silent.execute(msg);
        if (r.isEmpty()) {
            System.out.println("Error sending message");
        } else {
            messagesRepository.registerMessage(r.get().getMessageId());
        }
    }

    protected void deleteBotMessages(Long chatId) {
        var messageIDList = messagesRepository.getMessages();
        for (var messageID : messageIDList) {
            deleteMessageAsyncById(chatId, messageID);
        }
        messagesRepository.empty();
    }

    public void deleteMessageAsyncById(Long chatId, Integer messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId.toString());
        deleteMessage.setMessageId(messageId);

        silent.executeAsync(deleteMessage, DEFAULT_CALLBACK);
    }

    public void deleteUserMessage(MessageContext ctx) {
        deleteMessageAsyncById(ctx.chatId(), ctx.update().getMessage().getMessageId());
    }
}
