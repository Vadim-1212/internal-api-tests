package ru.testassignment.tests;

import io.qameta.allure.*;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.testassignment.base.BaseTest;
import ru.testassignment.client.EndpointClient;
import ru.testassignment.helper.TokenGenerator;
import ru.testassignment.steps.ApiSteps;

@Epic("API тестирование сервиса аутентификации")
@Feature("Аутентификация (LOGIN)")
public class LoginTest extends BaseTest {

    @Test
    @Story("Успешная аутентификация")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Успешный вход: LOGIN при работающем внешнем сервисе возвращает OK")
    @Description("Проверяем основной сценарий: пользователь отправляет LOGIN с валидным токеном, "
            + "внешний сервис /auth отвечает 200 — приложение возвращает OK.")
    void should_returnOk_when_loginWithValidTokenAndMockReturns200() {
        // Arrange — настраиваем мок внешнего сервиса
        ApiSteps.stubAuth(wireMock, 200);
        String token = TokenGenerator.validToken();

        // Act — отправляем запрос LOGIN
        ValidatableResponse response = EndpointClient.login(token);

        // Assert — ожидаем успешный ответ
        ApiSteps.assertResultOk(response, 200);
    }

    @Test
    @Story("Ошибка внешнего сервиса")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Ошибка входа: LOGIN при сбое внешнего сервиса (500) возвращает ошибку")
    @Description("Внешний сервис /auth отвечает 500 Internal Server Error. "
            + "Приложение должно корректно обработать ошибку и вернуть ERROR.")
    void should_returnError_when_loginAndMockReturns500() {
        // Arrange
        ApiSteps.stubAuth(wireMock, 500);
        String token = TokenGenerator.validToken();

        // Act
        ValidatableResponse response = EndpointClient.login(token);

        // Assert
        // Assert — приложение возвращает 500 при ошибке внешнего сервиса
        ApiSteps.assertResultError(response, 500);
    }

    @Test
    @Story("Ошибка внешнего сервиса")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Ошибка входа: LOGIN при ответе 400 от внешнего сервиса возвращает ошибку")
    @Description("Внешний сервис /auth отвечает 400 Bad Request. "
            + "Приложение должно обработать ошибку и вернуть ERROR.")
    void should_returnError_when_loginAndMockReturns400() {
        // Arrange
        ApiSteps.stubAuth(wireMock, 400);
        String token = TokenGenerator.validToken();

        // Act
        ValidatableResponse response = EndpointClient.login(token);

        // Assert — приложение возвращает 500 при ошибке внешнего сервиса
        ApiSteps.assertResultError(response, 500);
    }

    @Test
    @Story("Ошибка внешнего сервиса")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Ошибка входа: LOGIN при ответе 403 от внешнего сервиса возвращает ошибку")
    @Description("Внешний сервис /auth отвечает 403 Forbidden. "
            + "Приложение должно обработать ошибку и вернуть ERROR.")
    void should_returnError_when_loginAndMockReturns403() {
        // Arrange
        ApiSteps.stubAuth(wireMock, 403);
        String token = TokenGenerator.validToken();

        // Act
        ValidatableResponse response = EndpointClient.login(token);

        // Assert — приложение возвращает 500 при ошибке внешнего сервиса
        ApiSteps.assertResultError(response, 500);
    }

    @Test
    @Story("Повторная аутентификация")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Повторный LOGIN с тем же токеном возвращает ошибку (409)")
    @Description("Токен уже прошёл аутентификацию. Повторный LOGIN с тем же токеном "
            + "должен вернуть ERROR — токен уже существует в хранилище (409 Conflict).")
    void should_returnError_when_loginWithSameTokenTwice() {
        // Arrange — первый LOGIN успешный
        ApiSteps.stubAuth(wireMock, 200);
        String token = TokenGenerator.validToken();
        EndpointClient.login(token);

        // Act — повторный LOGIN с тем же токеном
        ValidatableResponse response = EndpointClient.login(token);

        // Assert — 409 Conflict: токен уже в хранилище
        ApiSteps.assertResultError(response, 409);
    }

    @Test
    @Story("Независимость сессий")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("LOGIN двумя разными токенами — обе сессии успешны")
    @Description("Два разных пользователя могут пройти аутентификацию одновременно. "
            + "Токены независимы друг от друга.")
    void should_returnOk_when_loginWithTwoDifferentTokens() {
        // Arrange
        ApiSteps.stubAuth(wireMock, 200);
        String token1 = TokenGenerator.validToken();
        String token2 = TokenGenerator.validToken();

        // Act
        ValidatableResponse response1 = EndpointClient.login(token1);
        ValidatableResponse response2 = EndpointClient.login(token2);

        // Assert — оба успешны
        ApiSteps.assertResultOk(response1, 200);
        ApiSteps.assertResultOk(response2, 200);
    }
}
