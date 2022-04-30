package utils;

import models.Meal;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MealUtils {
    private final DateUtils dateUtils;

    @Inject
    public MealUtils(DateUtils dateUtils) {
        this.dateUtils = dateUtils;
    }

    public String toString(List<Meal> meals) {
        return String.join("\n", meals.stream()
                .filter(Objects::nonNull)
                .map(this::toString)
                .toArray(String[]::new));
    }

    public String toString(Meal meal) {
        return Optional.ofNullable(meal)
                .map(m->"- " + dateUtils.getWeekdayName(m.getDate()) + ": " + m.toReadableString())
                .orElse("Could not find meal");
    }
}
