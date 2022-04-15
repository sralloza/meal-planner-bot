package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Meal {
    @JsonProperty
    private LocalDate date;

    public String getWeekday() {
        Date date = Date.from(getDate().atStartOfDay(ZoneId.of("Europe/Madrid")).toInstant());
        String dayName = new SimpleDateFormat("EEEE").format(date);
        return dayName.substring(0, 1).toUpperCase() + dayName.substring(1);
    }

    @JsonProperty
    private String lunch1;

    @JsonProperty
    private String lunch2;

    @JsonProperty
    private String dinner;

    @JsonProperty("lunch1_frozen")
    private boolean lunch1Frozen;

    @JsonProperty("lunch2_frozen")
    private boolean lunch2Frozen;

    @JsonProperty("dinner_frozen")
    private boolean dinnerFrozen;

    public String toReadableString() {
        if (Optional.ofNullable(lunch2).orElse("").isEmpty()) {
            return lunch1.toLowerCase() + " | " + dinner.toLowerCase();
        }
        return lunch1.toLowerCase() + " & " + lunch2.toLowerCase() + " | " + dinner.toLowerCase();
    }
}
