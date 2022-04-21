package repositories.messages;

import java.util.List;

public interface MessagesRepository {
    void registerMessage(int messageID);

    List<Integer> getMessages();

    void removeMessage(int messageID);

    void empty();
}
