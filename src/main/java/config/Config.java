package config;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.Optional;

public class Config {
    private static boolean dotenvLoaded = false;

    public String getenv(String key) {
        if (!dotenvLoaded) {
            Dotenv.configure()
                    .ignoreIfMissing()
                    .systemProperties()
                    .load();
            dotenvLoaded = true;
        }
        return Optional.ofNullable(System.getenv(key)).orElse(System.getProperty(key));
    }
}
