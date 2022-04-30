package base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;

import java.io.File;
import java.net.URL;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class BaseTest {
    protected void assertMessageDeleted(Long chatId, Integer messageId, SilentSender silent) {
        var deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId.toString());
        deleteMessage.setMessageId(messageId);
        Mockito.verify(silent).executeAsync(eq(deleteMessage), any());
    }

    protected Object invokeSentCallback(InvocationOnMock invocation, Message msg) {
        var arguments = invocation.getArguments();
        if (arguments.length == 2 && arguments[0] instanceof SendMessage) {
            var callback = (SentCallback<Message>) arguments[1];
            callback.onResult(null, msg);
        }
        return null;
    }

    protected <T> T readJson(Class<T> clazz, String filename) {
        URL resource = getClass().getClassLoader().getResource(filename);
        if (resource == null) {
            throw new IllegalArgumentException("File not found: " + filename);
        }
        try {
            ObjectMapper mapper = JsonMapper.builder()
                    .addModule(new JavaTimeModule())
                    .build();
            return mapper.readValue(new File(resource.toURI()), clazz);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        throw new RuntimeException("Failed to read file: " + filename);
    }
}
