import com.google.inject.AbstractModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import repositories.messages.LocalMessagesRepository;
import repositories.messages.MessagesRepository;

public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(MessagesRepository.class).to(LocalMessagesRepository.class);
        bind(Config.class).toInstance(ConfigFactory.load());
    }
}
