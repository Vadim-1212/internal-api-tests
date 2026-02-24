package ru.testassignment.steps;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.equalTo;

/**
 * Шаги для Allure-отчёта.
 * Каждый метод = один видимый шаг в отчёте.
 */
public final class ApiSteps {

    private ApiSteps() {
    }

    // --- Настройка моков ---

    @Step("Настроить мок /auth → ответ {statusCode}")
    public static void stubAuth(WireMockExtension wm, int statusCode) {
        wm.stubFor(post(urlEqualTo("/auth"))
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));
    }

    @Step("Настроить мок /doAction → ответ {statusCode}")
    public static void stubDoAction(WireMockExtension wm, int statusCode) {
        wm.stubFor(post(urlEqualTo("/doAction"))
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));
    }

    // --- Проверки ответа ---

    @Step("Проверить: статус {expectedStatus}, результат OK")
    public static void assertResultOk(ValidatableResponse response, int expectedStatus) {
        response.statusCode(expectedStatus)
                .body("result", equalTo("OK"));
    }

    @Step("Проверить: статус {expectedStatus}, результат ERROR")
    public static void assertResultError(ValidatableResponse response, int expectedStatus) {
        response.statusCode(expectedStatus)
                .body("result", equalTo("ERROR"));
    }

    @Step("Проверить: HTTP статус {expectedStatus}")
    public static void assertStatus(ValidatableResponse response, int expectedStatus) {
        response.statusCode(expectedStatus);
    }

    // --- Проверки обращений к моку ---

    @Step("Проверить: мок /auth получил {count} запрос(ов)")
    public static void verifyAuthCalled(WireMockExtension wm, int count) {
        wm.verify(count, postRequestedFor(urlEqualTo("/auth")));
    }

    @Step("Проверить: мок /doAction получил {count} запрос(ов)")
    public static void verifyDoActionCalled(WireMockExtension wm, int count) {
        wm.verify(count, postRequestedFor(urlEqualTo("/doAction")));
    }

    @Step("Проверить: мок /auth получил запрос с токеном {token}")
    public static void verifyAuthCalledWithToken(WireMockExtension wm, String token) {
        wm.verify(postRequestedFor(urlEqualTo("/auth"))
                .withRequestBody(containing("token=" + token)));
    }

    @Step("Проверить: мок /doAction получил запрос с токеном {token}")
    public static void verifyDoActionCalledWithToken(WireMockExtension wm, String token) {
        wm.verify(postRequestedFor(urlEqualTo("/doAction"))
                .withRequestBody(containing("token=" + token)));
    }
}
