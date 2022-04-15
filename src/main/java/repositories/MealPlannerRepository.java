package repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Inject;
import config.Config;
import models.Meal;
import models.MealList;
import utils.DateUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class MealPlannerRepository {
    private final String token;
    private final String baseURL;
    private final DateUtils dateUtils;

    @Inject
    public MealPlannerRepository(Config config, DateUtils dateUtils) {
        token = config.getenv("API_TOKEN");
        baseURL = config.getenv("API_BASE_URL");
        this.dateUtils = dateUtils;
    }

    private <T> CompletableFuture<T> sendRequest(String path, Class<T> clazz) {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + path))
                .header("Content-Type", "application/json")
                .header("X-Token", token)
                .header("User-Agent", "MealPlanner/1.0")
                .GET()
                .build();
        return client.sendAsync(request, BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    try {
                        return mapper.readValue(body, clazz);
                    } catch (JsonProcessingException e) {
                        System.err.println("Error parsing response: " + body);
                        e.printStackTrace();
                        return null;
                    }
                });

    }

    public CompletableFuture<Meal> getYesterdayMeal() {
        String yesterday = dateUtils.getYesterdayDate().toString();
        return sendRequest("/meals/" + yesterday, Meal.class);
    }

    public CompletableFuture<Meal> getTodayMeal() {
        return sendRequest("/meals/today", Meal.class);
    }

    public CompletableFuture<Meal> getTomorrowMeal() {
        return sendRequest("/meals/tomorrow", Meal.class);
    }

    public CompletableFuture<MealList> getLastWeekMeals() {
        int weekNumber = dateUtils.getLastWeekNumber();
        return sendRequest("/meals/week/" + weekNumber, MealList.class);
    }

    public CompletableFuture<MealList> getCurrentWeekMeals() {
        return sendRequest("/meals/week/current", MealList.class);
    }

    public CompletableFuture<MealList> getNextWeekMeals() {
        return sendRequest("/meals/week/next", MealList.class);
    }
}
