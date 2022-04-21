package repositories.messages;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocalMessagesRepository implements MessagesRepository {
    private final Set<Integer> memory;

    public LocalMessagesRepository() {
        this.memory = new HashSet<>();
    }

    @Override
    public void registerMessage(int messageID) {
        memory.add(messageID);
    }

    @Override
    public List<Integer> getMessages() {
        return new ArrayList<>(memory);
    }

    @Override
    public void removeMessage(int messageID) {
        memory.remove(messageID);
    }

    @Override
    public void empty() {
        memory.clear();
    }
}
