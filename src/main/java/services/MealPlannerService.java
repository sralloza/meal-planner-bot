package services;

import com.google.inject.Inject;
import repositories.MealPlannerRepository;

import java.util.concurrent.CompletableFuture;

import static contants.Messages.CURRENT_WEEK_MSG;
import static contants.Messages.LAST_WEEK_MSG;
import static contants.Messages.NEXT_WEEK_MSG;
import static contants.Messages.TODAY_MSG;
import static contants.Messages.TOMORROW_MSG;
import static contants.Messages.YESTERDAY_MSG;

public class MealPlannerService {
    private final MealPlannerRepository repository;

    @Inject
    public MealPlannerService(MealPlannerRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<?> getCompletableFutureByUserMessage(String userMessage) {
        switch (userMessage) {
            case YESTERDAY_MSG:
                return repository.getYesterdayMeal();
            case TODAY_MSG:
                return repository.getTodayMeal();
            case TOMORROW_MSG:
                return repository.getTomorrowMeal();
            case LAST_WEEK_MSG:
                return repository.getLastWeekMeals();
            case CURRENT_WEEK_MSG:
                return repository.getCurrentWeekMeals();
            case NEXT_WEEK_MSG:
                return repository.getNextWeekMeals();
            default:
                return CompletableFuture.completedFuture(null);
        }
    }
}
