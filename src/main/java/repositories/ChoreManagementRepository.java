package repositories;

import com.google.inject.Inject;
import config.ConfigRepository;
import models.SimpleChoreList;
import models.TicketList;
import models.WeeklyChores;
import models.WeeklyChoresList;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ChoreManagementRepository extends BaseRepository {
    @Inject
    public ChoreManagementRepository(ConfigRepository config) {
        super(config.getString("api.baseURL"), Map.of(), config);
    }

    public CompletableFuture<TicketList> getTickets() {
        return sendRequest("/v1/tickets", TicketList.class);
    }

    public CompletableFuture<WeeklyChoresList> getTasks() {
        return sendRequest("/v1/weekly-chores?missing_only=true", WeeklyChoresList.class);
    }

    public CompletableFuture<Void> completeTask(String weekId, Integer tenantId, String choreType) {
        return sendRequest("POST", "/v1/weekly-chores/complete/" + weekId + "/tenant/" +
                tenantId + "/choreType/" + choreType, null);
    }

    public CompletableFuture<SimpleChoreList> getSimpleTasks(Integer tenantId) {
        return sendRequest("/v1/simple-chores?missing_only=true&tenant_id=" + tenantId, SimpleChoreList.class);
    }

    public CompletableFuture<Void> skipWeek(Integer tenantId, String weekId) {
        return sendRequest("POST", "/v1/tenants/" + tenantId + "/skip/" + weekId, null);
    }

    public CompletableFuture<Void> unskipWeek(Integer tenantId, String weekId) {
        return sendRequest("POST", "/v1/tenants/" + tenantId + "/unskip/" + weekId, null);
    }

    public CompletableFuture<WeeklyChores> createWeeklyChores(String weekId) {
        return sendRequest("POST", "/v1/weekly-chores/week/" + weekId, WeeklyChores.class);
    }
}
