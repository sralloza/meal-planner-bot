package repositories;

import com.google.inject.Inject;
import config.ConfigRepository;
import models.Meal;
import models.MealList;
import utils.DateUtils;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MealPlannerRepository extends BaseRepository {
    private final DateUtils dateUtils;

    @Inject
    public MealPlannerRepository(ConfigRepository config, DateUtils dateUtils) {
        super(config.getString("api.baseURL"), Map.of("X-Token", config.getString("api.token")));
        this.dateUtils = dateUtils;
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
