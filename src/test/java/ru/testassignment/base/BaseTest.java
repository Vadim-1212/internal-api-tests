package ru.testassignment.base;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.testassignment.config.TestConfig;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Базовый класс для всех тестов.
 *
 * Жизненный цикл:
 * 1. WireMock стартует (расширение JUnit)
 * 2. Тестируемое приложение стартует (@BeforeAll)
 * 3. Перед каждым тестом WireMock сбрасывает стабы (@BeforeEach)
 * 4. Тесты выполняются
 * 5. Приложение останавливается (@AfterAll)
 * 6. WireMock останавливается (расширение JUnit)
 */
public abstract class BaseTest {

    private static final AppManager appManager = new AppManager();

    // WireMock на порту 8888 — имитирует внешний сервис
    @RegisterExtension
    protected static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().port(TestConfig.MOCK_PORT))
            .build();

    @BeforeAll
    static void startApp() throws Exception {
        // Фильтр REST Assured → автоматически прикрепляет запросы/ответы к Allure
        RestAssured.filters(new AllureRestAssured());
        appManager.start();
    }

    @AfterAll
    static void stopApp() {
        appManager.stop();
    }

    @BeforeEach
    void resetMocks() {
        // Сброс стабов между тестами — изоляция
        wireMock.resetAll();
    }
}
