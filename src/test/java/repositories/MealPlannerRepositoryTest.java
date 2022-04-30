package repositories;

import base.BaseTest;
import com.github.tomakehurst.wiremock.WireMockServer;
import config.ConfigRepository;
import models.Meal;
import models.MealList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import utils.DateProvider;
import utils.DateUtils;

import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MealPlannerRepositoryTest extends BaseTest {
    @Mock
    private ConfigRepository config;

    @Mock
    private DateProvider dateProvider;

    private WireMockServer wireMockServer;
    private MealPlannerRepository repository;
    private final LocalDate today = LocalDate.of(2022, 2, 26);

    @BeforeEach
    void setUp() {
        long time = System.currentTimeMillis();
        int port = 8080 + (int) (time % 30000);
        System.out.println("Using port: " + port);

        MockitoAnnotations.openMocks(this);
        Mockito.when(config.getString("api.baseURL")).thenReturn("http://localhost:" + port);
        Mockito.when(config.getString("api.token")).thenReturn("uuid");
        Mockito.when(dateProvider.getCurrentDate()).thenReturn(today);

        wireMockServer = new WireMockServer(port);
        wireMockServer.start();

        configureFor("localhost", port);

        stubFor(get(urlEqualTo("/meals/" + today.minusDays(2)))
                .willReturn(aResponse().withBodyFile("singleMealTwoDaysAgo.json")));
        stubFor(get(urlEqualTo("/meals/" + today.minusDays(1)))
                .willReturn(aResponse().withBodyFile("singleMealYesterday.json")));
        stubFor(get(urlEqualTo("/meals/" + today))
                .willReturn(aResponse().withBodyFile("singleMealToday.json")));
        stubFor(get(urlEqualTo("/meals/" + today.plusDays(1)))
                .willReturn(aResponse().withBodyFile("singleMealTomorrow.json")));
        stubFor(get(urlEqualTo("/meals/" + today.plusDays(2)))
                .willReturn(aResponse().withBodyFile("singleMealTwoDaysAhead.json")));

        stubFor(get(urlEqualTo("/meals/week/6"))
                .willReturn(aResponse().withBodyFile("mealListTwoWeeksAgo.json")));
        stubFor(get(urlEqualTo("/meals/week/7"))
                .willReturn(aResponse().withBodyFile("mealListLastWeek.json")));
        stubFor(get(urlEqualTo("/meals/week/8"))
                .willReturn(aResponse().withBodyFile("mealListCurrentWeek.json")));
        stubFor(get(urlEqualTo("/meals/week/9"))
                .willReturn(aResponse().withBodyFile("mealListNextWeek.json")));
        stubFor(get(urlEqualTo("/meals/week/10"))
                .willReturn(aResponse().withBodyFile("mealListTwoWeeksAhead.json")));

        var dateUtils = new DateUtils(dateProvider);
        repository = new MealPlannerRepository(config, dateUtils);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void shouldGetTwoDaysAgoMeal() throws Exception {
        Meal result = repository.getTwoDaysAgoMeal().get();
        Meal expected = readJson(Meal.class, "__files/singleMealTwoDaysAgo.json");
        assertEquals(expected, result);
        assertEquals(result.getDate(), today.minusDays(2));
    }

    @Test
    void shouldGetYesterdayMeal() throws Exception {
        Meal result = repository.getYesterdayMeal().get();
        Meal expected = readJson(Meal.class, "__files/singleMealYesterday.json");
        assertEquals(expected, result);
        assertEquals(result.getDate(), today.minusDays(1));
    }

    @Test
    void shouldGetTodayMeal() throws Exception {
        Meal result = repository.getTodayMeal().get();
        Meal expected = readJson(Meal.class, "__files/singleMealToday.json");
        assertEquals(expected, result);
        assertEquals(result.getDate(), today);
    }

    @Test
    void shouldGetTomorrowMeal() throws Exception {
        Meal result = repository.getTomorrowMeal().get();
        Meal expected = readJson(Meal.class, "__files/singleMealTomorrow.json");
        assertEquals(expected, result);
        assertEquals(result.getDate(), today.plusDays(1));
    }

    @Test
    void shouldGetTwoDaysAheadMeal() throws Exception {
        Meal result = repository.getTwoDaysAheadMeal().get();
        Meal expected = readJson(Meal.class, "__files/singleMealTwoDaysAhead.json");
        assertEquals(expected, result);
        assertEquals(result.getDate(), today.plusDays(2));
    }

    // Meal lists

    @Test
    void shouldGetTwoWeeksAgoMeals() throws Exception {
        MealList result = repository.getTwoWeeksAgoMealList().get();
        MealList expected = readJson(MealList.class, "__files/mealListTwoWeeksAgo.json");
        assertEquals(expected, result);
    }

    @Test
    void shouldGetLastWeekMeals() throws Exception {
        MealList result = repository.getLastWeekMeals().get();
        MealList expected = readJson(MealList.class, "__files/mealListLastWeek.json");
        assertEquals(expected, result);
    }

    @Test
    void shouldGetCurrentWeekMeals() throws Exception {
        MealList result = repository.getCurrentWeekMeals().get();
        MealList expected = readJson(MealList.class, "__files/mealListCurrentWeek.json");
        assertEquals(expected, result);
    }

    @Test
    void shouldGetNextWeekMeals() throws Exception {
        MealList result = repository.getNextWeekMeals().get();
        MealList expected = readJson(MealList.class, "__files/mealListNextWeek.json");
        assertEquals(expected, result);
    }

    @Test
    void shouldGetTwoWeeksAheadMeals() throws Exception {
        MealList result = repository.getTwoWeeksAheadMealList().get();
        MealList expected = readJson(MealList.class, "__files/mealListTwoWeeksAhead.json");
        assertEquals(expected, result);
    }
}
