package ru.testassignment.tests;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.testassignment.base.BaseTest;
import ru.testassignment.client.EndpointClient;
import ru.testassignment.helper.TokenGenerator;
import ru.testassignment.steps.ApiSteps;

@Epic("API тестирование сервиса аутентификации")
@Feature("Корректность обращений к внешнему сервису")
public class WireMockVerificationTest extends BaseTest {

    @Test
    @Story("LOGIN → запрос на /auth")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("При LOGIN приложение отправляет POST /auth с правильным токеном")
    @Description("Проверяем, что при действии LOGIN приложение корректно обращается "
            + "к внешнему сервису: отправляет POST на /auth и передаёт токен в теле запроса.")
    void should_sendAuthRequest_when_login() {
        // Arrange
        ApiSteps.stubAuth(wireMock, 200);
        String token = TokenGenerator.validToken();

        // Act
        EndpointClient.login(token);

        // Assert — мок получил запрос с правильным токеном
        ApiSteps.verifyAuthCalled(wireMock, 1);
        ApiSteps.verifyAuthCalledWithToken(wireMock, token);
    }

    @Test
    @Story("ACTION → запрос на /doAction")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("При ACTION приложение отправляет POST /doAction с правильным токеном")
    @Description("Проверяем, что при действии ACTION приложение обращается "
            + "к внешнему сервису: отправляет POST на /doAction и передаёт токен в теле запроса.")
    void should_sendDoActionRequest_when_action() {
        // Arrange — сначала логин
        ApiSteps.stubAuth(wireMock, 200);
        ApiSteps.stubDoAction(wireMock, 200);
        String token = TokenGenerator.validToken();
        EndpointClient.login(token);

        // Act
        EndpointClient.action(token);

        // Assert — мок /doAction получил запрос с правильным токеном
        ApiSteps.verifyDoActionCalled(wireMock, 1);
        ApiSteps.verifyDoActionCalledWithToken(wireMock, token);
    }

    @Test
    @Story("LOGOUT → без обращений к моку")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("При LOGOUT приложение НЕ обращается к внешнему сервису")
    @Description("LOGOUT — внутренняя операция удаления токена из хранилища. "
            + "Приложение не должно отправлять запросы к внешнему сервису при LOGOUT.")
    void should_notCallMock_when_logout() {
        // Arrange — логинимся
        ApiSteps.stubAuth(wireMock, 200);
        String token = TokenGenerator.validToken();
        EndpointClient.login(token);
        // Сбрасываем счётчик обращений после логина
        wireMock.resetRequests();

        // Act — выходим
        EndpointClient.logout(token);

        // Assert — мок не получал запросов при LOGOUT
        ApiSteps.verifyAuthCalled(wireMock, 0);
        ApiSteps.verifyDoActionCalled(wireMock, 0);
    }

    @Test
    @Story("Формат запроса к внешнему сервису")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Токен передаётся в form-urlencoded формате")
    @Description("Проверяем, что приложение передаёт токен во внешний сервис "
            + "в формате application/x-www-form-urlencoded (token=ЗНАЧЕНИЕ).")
    void should_sendTokenInFormUrlEncoded_when_login() {
        // Arrange
        ApiSteps.stubAuth(wireMock, 200);
        String token = TokenGenerator.validToken();

        // Act
        EndpointClient.login(token);

        // Assert — запрос содержит token=ЗНАЧЕНИЕ в теле
        ApiSteps.verifyAuthCalledWithToken(wireMock, token);
    }
}
