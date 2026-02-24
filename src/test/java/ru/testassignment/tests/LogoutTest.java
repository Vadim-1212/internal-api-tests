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
@Feature("Завершение сессии (LOGOUT)")
public class LogoutTest extends BaseTest {

    @Test
    @Story("Успешное завершение сессии")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Успешный выход: LOGOUT после LOGIN возвращает OK")
    @Description("Основной сценарий: пользователь прошёл аутентификацию, "
            + "затем завершает сессию. Приложение должно вернуть OK и удалить токен.")
    void should_returnOk_when_logoutAfterLogin() {
        // Arrange — логинимся
        ApiSteps.stubAuth(wireMock, 200);
        String token = TokenGenerator.validToken();
        EndpointClient.login(token);

        // Act — выходим
        ValidatableResponse response = EndpointClient.logout(token);

        // Assert
        ApiSteps.assertResultOk(response, 200);
    }

    @Test
    @Story("Выход без аутентификации")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("LOGOUT без предварительного LOGIN возвращает ошибку (403)")
    @Description("Пользователь пытается завершить сессию, не пройдя аутентификацию. "
            + "Токен не найден в хранилище — приложение возвращает 403.")
    void should_returnError_when_logoutWithoutLogin() {
        // Arrange — токен не залогинен
        String token = TokenGenerator.validToken();

        // Act
        ValidatableResponse response = EndpointClient.logout(token);

        // Assert — 403: токен не найден
        ApiSteps.assertResultError(response, 403);
    }

    @Test
    @Story("Повторное завершение сессии")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Повторный LOGOUT возвращает ошибку (403)")
    @Description("Пользователь уже завершил сессию. Повторная попытка LOGOUT "
            + "должна вернуть ERROR — токен уже удалён из хранилища (403).")
    void should_returnError_when_logoutTwice() {
        // Arrange — логин и первый logout
        ApiSteps.stubAuth(wireMock, 200);
        String token = TokenGenerator.validToken();
        EndpointClient.login(token);
        EndpointClient.logout(token);

        // Act — повторный logout
        ValidatableResponse response = EndpointClient.logout(token);

        // Assert — 403: токен уже удалён
        ApiSteps.assertResultError(response, 403);
    }
}
