package repositories;

import com.google.inject.Inject;
import config.ConfigRepository;
import models.Meal;
import models.MealList;
import utils.DateUtils;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MealPlannerRepository extends BaseRepository {
    private final DateUtils dateUtils;

    @Inject
    public MealPlannerRepository(ConfigRepository config, DateUtils dateUtils) {
        super(config.getString("api.baseURL"), Map.of("X-Token", config.getString("api.token")));
        this.dateUtils = dateUtils;
    }

    private CompletableFuture<Meal> getMealByDate(LocalDate date) {
        return sendRequest("/meals/" + date.toString(), Meal.class);
    }

    private CompletableFuture<MealList> getMealListByWeekNumber(int weekNumber) {
        return sendRequest("/meals/week/" + weekNumber, MealList.class);
    }

    private CompletableFuture<MealList> getMealListByWeekDelta(int weekDelta) {
        return getMealListByWeekNumber(dateUtils.getWeekNumberByDeltaDays(weekDelta * 7));
    }

    public CompletableFuture<Meal> getTwoDaysAgoMeal() {
        return getMealByDate(dateUtils.getTwoDaysAgoDate());
    }

    public CompletableFuture<Meal> getYesterdayMeal() {
        return getMealByDate(dateUtils.getYesterdayDate());
    }

    public CompletableFuture<Meal> getTodayMeal() {
        return getMealByDate(dateUtils.getTodayDate());
    }

    public CompletableFuture<Meal> getTomorrowMeal() {
        return getMealByDate(dateUtils.getTomorrowDate());
    }

    public CompletableFuture<Meal> getTwoDaysAheadMeal() {
        return getMealByDate(dateUtils.getTwoDaysAheadDate());
    }

    public CompletableFuture<MealList> getTwoWeeksAgoMealList() {
        return getMealListByWeekDelta(-2);
    }

    public CompletableFuture<MealList> getLastWeekMeals() {
        return getMealListByWeekDelta(-1);
    }

    public CompletableFuture<MealList> getCurrentWeekMeals() {
        return getMealListByWeekDelta(0);
    }

    public CompletableFuture<MealList> getNextWeekMeals() {
        return getMealListByWeekDelta(1);
    }

    public CompletableFuture<MealList> getTwoWeeksAheadMealList() {
        return getMealListByWeekDelta(2);
    }
}
