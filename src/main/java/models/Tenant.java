package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Tenant {
    private String username;
    @JsonProperty("tenant_id")
    private Integer tenantId;
    @JsonProperty("api_token")
    private String apiToken;
}
