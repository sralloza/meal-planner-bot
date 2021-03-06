package services;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import repositories.MealPlannerRepository;

import java.util.concurrent.CompletableFuture;

import static constants.Messages.CURRENT_WEEK_MSG;
import static constants.Messages.TWO_DAYS_AHEAD_MSG;
import static constants.Messages.TOW_DAYS_AGO_MSG;
import static constants.Messages.LAST_WEEK_MSG;
import static constants.Messages.NEXT_WEEK_MSG;
import static constants.Messages.TODAY_MSG;
import static constants.Messages.TOMORROW_MSG;
import static constants.Messages.TWO_WEEKS_AGO_MSG;
import static constants.Messages.TWO_WEEKS_AHEAD_MSG;
import static constants.Messages.YESTERDAY_MSG;

@Slf4j
public class MealPlannerService {
    private final MealPlannerRepository repository;

    @Inject
    public MealPlannerService(MealPlannerRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<?> getCompletableFutureByUserMessage(String userMessage) {
        log.debug("Received user message: {}", userMessage);
        switch (userMessage) {
            case TOW_DAYS_AGO_MSG:
                return repository.getTwoDaysAgoMeal();
            case YESTERDAY_MSG:
                return repository.getYesterdayMeal();
            case TODAY_MSG:
                return repository.getTodayMeal();
            case TOMORROW_MSG:
                return repository.getTomorrowMeal();
            case TWO_DAYS_AHEAD_MSG:
                return repository.getTwoDaysAheadMeal();

            case TWO_WEEKS_AGO_MSG:
                return repository.getTwoWeeksAgoMealList();
            case LAST_WEEK_MSG:
                return repository.getLastWeekMeals();
            case CURRENT_WEEK_MSG:
                return repository.getCurrentWeekMeals();
            case NEXT_WEEK_MSG:
                return repository.getNextWeekMeals();
            case TWO_WEEKS_AHEAD_MSG:
                return repository.getTwoWeeksAheadMealList();
            default:
                return CompletableFuture.completedFuture(null);
        }
    }
}
