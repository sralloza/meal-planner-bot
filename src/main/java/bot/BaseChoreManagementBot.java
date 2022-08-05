package bot;

import constants.Messages;
import exceptions.APIException;
import lombok.extern.slf4j.Slf4j;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import services.ChoreManagementService;

import static org.telegram.abilitybots.api.db.MapDBContext.offlineInstance;

@Slf4j
public abstract class BaseChoreManagementBot extends AbilityBot {
    private final Keyboards keyboards;
    protected final ChoreManagementService service;

    protected BaseChoreManagementBot(String botToken,
                                     String botUsername,
                                     Keyboards keyboards,
                                     ChoreManagementService service) {
        super(botToken, botUsername, offlineInstance("db"));
        this.keyboards = keyboards;
        this.service = service;
    }

    protected void sendMessageMarkdown(String msgStr, Long chatId) {
        sendMessage(msgStr, chatId, true);
    }

    protected void sendMessage(String msgStr, Long chatId, boolean markdown) {
        SendMessage msg = new SendMessage();
        if (markdown) {
            msgStr = msgStr.replace("-", "\\-").replace("|", "\\|");
            msg.enableMarkdownV2(true);
            log.debug("Sending message with markdownV2 enabled: {}", msgStr);
        }

        msg.setText(msgStr);
        msg.setChatId(chatId.toString());
        msg.setReplyMarkup(keyboards.getMainMenuKeyboard());

        var r = silent.execute(msg);
        if (r.isEmpty()) {
            System.err.println("Error sending message");
        }
    }

    protected void handleException(Exception e, Long chatId) {
        log.error("Manually handling exception", e);
        if (e.getClass().equals(APIException.class)) {
            var exc = (APIException) e;
            sendMessage("Error: " + exc.getMsg(), chatId, false);
        } else if (e.getCause().getClass().equals(APIException.class)) {
            var exc = (APIException) e.getCause();
            sendMessage("Error: " + exc.getMsg(), chatId, false);
        } else {
            sendMessage(Messages.UNKNOWN_ERROR, chatId, true);
            String msg = "ERROR:\n" + e.getClass() + " - " + e.getMessage();
            sendMessage(msg, creatorId(), false);
        }
    }
}
