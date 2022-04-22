package utils;

import models.Meal;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MealUtils {
    public String toString(List<Meal> meals) {
        return String.join("\n", meals.stream()
                .filter(Objects::nonNull)
                .map(this::toString)
                .toArray(String[]::new));
    }

    public String toString(Meal meal) {
        return Optional.ofNullable(meal)
                .map(m->"- " + m.getWeekday() + ": " + m.toReadableString())
                .orElse("Could not find meal");
    }
}
