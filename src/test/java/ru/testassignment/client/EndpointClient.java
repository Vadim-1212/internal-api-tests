package ru.testassignment.client;

import io.restassured.response.ValidatableResponse;
import ru.testassignment.config.TestConfig;

import static io.restassured.RestAssured.given;

/**
 * HTTP-клиент для взаимодействия с эндпоинтом /endpoint.
 * Единая точка для всех HTTP-вызовов в тестах.
 */
public final class EndpointClient {

    private EndpointClient() {
    }

    /** Отправить запрос с валидным API-ключом */
    public static ValidatableResponse send(String token, String action) {
        return given()
                .header("X-Api-Key", TestConfig.API_KEY)
                .contentType("application/x-www-form-urlencoded")
                .formParam("token", token)
                .formParam("action", action)
                .when()
                .post(TestConfig.BASE_URL + TestConfig.ENDPOINT_PATH)
                .then();
    }

    /** Отправить запрос БЕЗ заголовка X-Api-Key */
    public static ValidatableResponse sendWithoutApiKey(String token, String action) {
        return given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("token", token)
                .formParam("action", action)
                .when()
                .post(TestConfig.BASE_URL + TestConfig.ENDPOINT_PATH)
                .then();
    }

    /** Отправить запрос с невалидным API-ключом */
    public static ValidatableResponse sendWithInvalidApiKey(String token, String action) {
        return given()
                .header("X-Api-Key", "INVALID_KEY")
                .contentType("application/x-www-form-urlencoded")
                .formParam("token", token)
                .formParam("action", action)
                .when()
                .post(TestConfig.BASE_URL + TestConfig.ENDPOINT_PATH)
                .then();
    }

    /** Отправить запрос без токена и без action */
    public static ValidatableResponse sendEmpty() {
        return given()
                .header("X-Api-Key", TestConfig.API_KEY)
                .contentType("application/x-www-form-urlencoded")
                .when()
                .post(TestConfig.BASE_URL + TestConfig.ENDPOINT_PATH)
                .then();
    }

    // --- Удобные методы для конкретных действий ---

    public static ValidatableResponse login(String token) {
        return send(token, "LOGIN");
    }

    public static ValidatableResponse action(String token) {
        return send(token, "ACTION");
    }

    public static ValidatableResponse logout(String token) {
        return send(token, "LOGOUT");
    }
}
