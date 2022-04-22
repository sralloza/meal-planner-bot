package utils;

import java.time.LocalDate;
import java.time.temporal.ChronoField;

public class DateUtils {
    public LocalDate getYesterdayDate() {
        return LocalDate.now().minusDays(1);
    }

    public int getLastWeekNumber() {
        return LocalDate.now().minusDays(7).get(ChronoField.ALIGNED_WEEK_OF_YEAR);
    }
}
