package services;

import bot.Keyboards;
import com.google.inject.Inject;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import repositories.messages.MessagesRepository;

import static constants.Messages.DEFAULT_CALLBACK;

public class MessagesSenderService {
    private SilentSender sender;
    private final MessagesRepository messagesRepository;
    private final Keyboards keyboards;

    @Inject
    public MessagesSenderService(MessagesRepository messagesRepository, Keyboards keyboards) {
        this.messagesRepository = messagesRepository;
        this.keyboards = keyboards;
    }

    public void setSender(SilentSender sender) {
        this.sender = sender;
    }

    private void checkSender() {
        if (sender == null) {
            throw new IllegalStateException("Sender is not set");
        }
    }

    public void sendMessage(String msgStr, Long chatId) {
        checkSender();
        SendMessage msg = new SendMessage();
        msg.setText(msgStr);
        msg.setChatId(chatId.toString());
        msg.setReplyMarkup(keyboards.getMainMenuKeyboard());

        var r = sender.execute(msg);
        if (r.isEmpty()) {
            System.out.println("Error sending message");
        } else {
            messagesRepository.registerMessage(r.get().getMessageId());
        }
    }

    public void deleteBotMessages(Long chatId) {
        checkSender();
        var messageIDList = messagesRepository.getMessages();
        for (var messageID : messageIDList) {
            deleteMessageAsyncById(chatId, messageID);
        }
        messagesRepository.empty();
    }

    public void deleteMessageAsyncById(Long chatId, Integer messageId) {
        checkSender();
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId.toString());
        deleteMessage.setMessageId(messageId);

        sender.executeAsync(deleteMessage, DEFAULT_CALLBACK);
    }

    public void deleteUserMessage(MessageContext ctx) {
        deleteMessageAsyncById(ctx.chatId(), ctx.update().getMessage().getMessageId());
    }
}
