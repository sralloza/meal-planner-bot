package utils;

import com.typesafe.config.Config;
import models.Meal;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MealUtils {
    private final DateUtils dateUtils;
    private final String leftToken;
    private final String rightToken;

    @Inject
    public MealUtils(DateUtils dateUtils, Config config) {
        this.dateUtils = dateUtils;
        this.leftToken = config.getString("telegram.style.frozenTokens.left");
        this.rightToken = config.getString("telegram.style.frozenTokens.right");
    }

    public String toString(List<Meal> meals) {
        return String.join("\n", meals.stream()
                .filter(Objects::nonNull)
                .map(this::toString)
                .toArray(String[]::new));
    }

    public String toString(Meal meal) {
        return Optional.ofNullable(meal)
                .map(m->"- " + dateUtils.getWeekdayName(m.getDate()) + ": " + toReadableString(m))
                .orElse("Could not find meal");
    }

    public String toReadableString(Meal meal) {
        if (Optional.ofNullable(meal.getLunch2()).orElse("").isEmpty()) {
            return getLunch1Markdown(meal) + " | " + getDinnerMarkdown(meal);
        }
        return getLunch1Markdown(meal) + " & " + getLunch2Markdown(meal) + " | " + getDinnerMarkdown(meal);
    }

    private String getLunch1Markdown(Meal meal) {
        return format(meal.getLunch1(), meal.isLunch1Frozen());
    }

    private String getLunch2Markdown(Meal meal) {
        return format(meal.getLunch2(), meal.isLunch2Frozen());
    }

    private String getDinnerMarkdown(Meal meal) {
        return format(meal.getDinner(), meal.isDinnerFrozen());
    }

    private String format(String meal, boolean frozen) {
        if (meal == null) {
            return leftToken + "NULL" + rightToken;
        }
        meal = meal.toLowerCase();
        if (frozen) {
            return leftToken + meal + rightToken;
        }
        return meal;
    }
}
