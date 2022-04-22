package repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class BaseRepository {
    private final String baseURL;
    private final Map<String, String> headers;

    public BaseRepository(String baseURL, Map<String, String> headers) {
        this.baseURL = baseURL;
        this.headers = headers;
    }

    protected <T> CompletableFuture<T> sendRequest(String path, Class<T> clazz) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + path))
                .header("Content-Type", "application/json")
                .header("User-Agent", "MealPlanner/1.0")
                .GET();

        for (Map.Entry<String, String> header : headers.entrySet()) {
            requestBuilder = requestBuilder.header(header.getKey(), header.getValue());
        }

        HttpRequest request = requestBuilder.build();
        log.debug("Request URL: " + request.uri());
        log.debug("Request headers: " + request.headers());

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(this::getBodyOpt)
                .thenApplyAsync(bodyOpt -> bodyOpt.map(body -> processBody(body, clazz)).orElse(null));
    }

    private <T> T processBody(String body, Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        try {
            return mapper.readValue(body, clazz);
        } catch (JsonProcessingException e) {
            System.err.println("Error parsing response: " + body);
            log.error("Error parsing response: " + body, e);
            e.printStackTrace();
            return null;
        }
    }

    @NotNull
    private Optional<String> getBodyOpt(HttpResponse<String> response) {
        log.debug("Response code: " + response.statusCode());
        log.debug("Response headers: " + response.headers());
        log.debug("Response body: " + response.body());

        if (response.statusCode() == 404) {
            return Optional.empty();
        }
        return Optional.of(response.body());
    }
}
