package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Locale;

public class DateUtils {
    public LocalDate getYesterdayDate() {
        return LocalDate.now().minusDays(1);
    }

    public String getWeekdayName(LocalDate localDate) {
        Locale spanishLocale = new Locale("es", "ES");
        String dayName = localDate.format(DateTimeFormatter.ofPattern("EEEE", spanishLocale));
        return dayName.substring(0, 1).toUpperCase() + dayName.substring(1);
    }

    public int getLastWeekNumber() {
        return LocalDate.now().minusDays(7).get(ChronoField.ALIGNED_WEEK_OF_YEAR);
    }
}
