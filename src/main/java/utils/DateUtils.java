package utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class DateUtils {
    public LocalDate getYesterdayDate() {
        return LocalDate.now().minusDays(1);
    }

    public int getLastWeekNumber() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(7);
        TemporalField woy = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
        return localDateTime.get(woy);
    }
}
