package repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import utils.DateUtils;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MealPlannerRepositoryTest {
    private WireMockServer wireMockServer;
    private MealPlannerRepository repository;

    private <T> T readJson(Class<T> clazz, String filename) {
        URL resource = getClass().getClassLoader().getResource(filename);
        if (resource == null) {
            throw new IllegalArgumentException("File not found: " + filename);
        }
        try {
            ObjectMapper mapper = JsonMapper.builder()
                    .addModule(new JavaTimeModule())
                    .build();
            return mapper.readValue(new File(resource.toURI()), clazz);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Mock
    private ConfigRepository config;

    @Mock
    private DateUtils dateUtils;

    @BeforeEach
    void setUp() {
        long time = System.currentTimeMillis();
        int port = 8080 + (int) (time % 30000);
        System.out.println("Using port: " + port);

        MockitoAnnotations.openMocks(this);
        Mockito.when(config.getString("api.baseURL")).thenReturn("http://localhost:8080");
        Mockito.when(config.getString("api.token")).thenReturn("uuid");
        Mockito.when(dateUtils.getYesterdayDate()).thenReturn(LocalDate.of(2022, 7, 17));
        Mockito.when(dateUtils.getLastWeekNumber()).thenReturn(7);

        wireMockServer = new WireMockServer(port);
        wireMockServer.start();

        configureFor("localhost", port);

        stubFor(get(urlEqualTo("/meals/2022-07-17"))
                .willReturn(aResponse().withBodyFile("singleMealYesterday.json")));
        stubFor(get(urlEqualTo("/meals/today"))
                .willReturn(aResponse().withBodyFile("singleMealToday.json")));
        stubFor(get(urlEqualTo("/meals/tomorrow"))
                .willReturn(aResponse().withBodyFile("singleMealTomorrow.json")));

        stubFor(get(urlEqualTo("/meals/week/7"))
                .willReturn(aResponse().withBodyFile("mealListLastWeek.json")));
        stubFor(get(urlEqualTo("/meals/week/current"))
                .willReturn(aResponse().withBodyFile("mealListCurrentWeek.json")));
        stubFor(get(urlEqualTo("/meals/week/next"))
                .willReturn(aResponse().withBodyFile("mealListNextWeek.json")));

        repository = new MealPlannerRepository(config, dateUtils);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void shouldGetYesterdayMeal() throws Exception {
        Meal result = repository.getYesterdayMeal().get();
        Meal expected = readJson(Meal.class, "__files/singleMealYesterday.json");
        assertEquals(expected, result);
    }

    @Test
    void shouldGetTodayMeal() throws Exception {
        Meal result = repository.getTodayMeal().get();
        Meal expected = readJson(Meal.class, "__files/singleMealToday.json");
        assertEquals(expected, result);
    }

    @Test
    void shouldGetTomorrowMeal() throws Exception {
        Meal result = repository.getTomorrowMeal().get();
        Meal expected = readJson(Meal.class, "__files/singleMealTomorrow.json");
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
}
