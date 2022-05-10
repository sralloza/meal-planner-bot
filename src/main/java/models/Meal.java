package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Meal {
    @JsonProperty
    private LocalDate date;

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

    public String getLunch1Markdown() {
        return format(lunch1, lunch1Frozen);
    }

    public String getLunch2Markdown() {
        return format(lunch2, lunch2Frozen);
    }

    public String getDinnerMarkdown() {
        return format(dinner, dinnerFrozen);
    }

    public String toReadableString() {
        if (Optional.ofNullable(lunch2).orElse("").isEmpty()) {
            return getLunch1Markdown() + " | " + getLunch2Markdown();
        }
        return getLunch1Markdown() + " & " + getLunch2Markdown() + " | " + getDinnerMarkdown();
    }

    private String format(String meal, boolean frozen) {
        meal = meal.toLowerCase();
        if (frozen) {
            return "__*_" + meal + "_*__";
        }
        return meal;
    }
}
