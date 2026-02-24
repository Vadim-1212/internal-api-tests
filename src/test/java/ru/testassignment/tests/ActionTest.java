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
@Feature("Выполнение действия (ACTION)")
public class ActionTest extends BaseTest {

    @Test
    @Story("Успешное выполнение действия")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Успешное действие: ACTION после LOGIN возвращает OK")
    @Description("Основной сценарий: пользователь прошёл аутентификацию (LOGIN), "
            + "затем выполняет действие (ACTION). Внешний сервис отвечает 200 — результат OK.")
    void should_returnOk_when_actionAfterLogin() {
        // Arrange — логинимся, настраиваем мок для doAction
        ApiSteps.stubAuth(wireMock, 200);
        ApiSteps.stubDoAction(wireMock, 200);
        String token = TokenGenerator.validToken();
        EndpointClient.login(token);

        // Act — выполняем действие
        ValidatableResponse response = EndpointClient.action(token);

        // Assert
        ApiSteps.assertResultOk(response, 200);
    }

    @Test
    @Story("Действие без аутентификации")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("ACTION без предварительного LOGIN возвращает ошибку")
    @Description("Пользователь пытается выполнить действие без аутентификации. "
            + "Токен не зарегистрирован в хранилище — приложение должно вернуть ERROR.")
    void should_returnError_when_actionWithoutLogin() {
        // Arrange — мок настроен, но LOGIN не делаем
        ApiSteps.stubDoAction(wireMock, 200);
        String token = TokenGenerator.validToken();

        // Act
        ValidatableResponse response = EndpointClient.action(token);

        // Assert — 403: токен не найден в хранилище
        ApiSteps.assertResultError(response, 403);
    }

    @Test
    @Story("Действие после завершения сессии")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("ACTION после LOGOUT возвращает ошибку (403)")
    @Description("Пользователь залогинился, затем вышел (LOGOUT). "
            + "Попытка выполнить ACTION после выхода должна вернуть ERROR — токен удалён (403).")
    void should_returnError_when_actionAfterLogout() {
        // Arrange — полный цикл: login → logout
        ApiSteps.stubAuth(wireMock, 200);
        ApiSteps.stubDoAction(wireMock, 200);
        String token = TokenGenerator.validToken();
        EndpointClient.login(token);
        EndpointClient.logout(token);

        // Act — пытаемся выполнить действие после выхода
        ValidatableResponse response = EndpointClient.action(token);

        // Assert — 403: токен удалён из хранилища
        ApiSteps.assertResultError(response, 403);
    }

    @Test
    @Story("Ошибка внешнего сервиса при действии")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("ACTION при сбое внешнего сервиса (500) возвращает ошибку")
    @Description("Пользователь залогинен, но внешний сервис /doAction отвечает 500. "
            + "Приложение должно обработать ошибку и вернуть ERROR (500).")
    void should_returnError_when_actionAndMockReturns500() {
        // Arrange
        ApiSteps.stubAuth(wireMock, 200);
        ApiSteps.stubDoAction(wireMock, 500);
        String token = TokenGenerator.validToken();
        EndpointClient.login(token);

        // Act
        ValidatableResponse response = EndpointClient.action(token);

        // Assert — 500: ошибка внешнего сервиса пробрасывается
        ApiSteps.assertResultError(response, 500);
    }

    @Test
    @Story("Ошибка внешнего сервиса при действии")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("ACTION при ответе 400 от внешнего сервиса возвращает ошибку")
    @Description("Пользователь залогинен, но внешний сервис /doAction отвечает 400. "
            + "Приложение должно обработать ошибку и вернуть ERROR (500).")
    void should_returnError_when_actionAndMockReturns400() {
        // Arrange
        ApiSteps.stubAuth(wireMock, 200);
        ApiSteps.stubDoAction(wireMock, 400);
        String token = TokenGenerator.validToken();
        EndpointClient.login(token);

        // Act
        ValidatableResponse response = EndpointClient.action(token);

        // Assert — 500: ошибка внешнего сервиса пробрасывается
        ApiSteps.assertResultError(response, 500);
    }

    @Test
    @Story("Множественные действия")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Три ACTION подряд после одного LOGIN — все успешны")
    @Description("После однократной аутентификации пользователь может выполнять "
            + "действия многократно. Каждый ACTION должен возвращать OK.")
    void should_returnOk_when_multipleActionsAfterLogin() {
        // Arrange
        ApiSteps.stubAuth(wireMock, 200);
        ApiSteps.stubDoAction(wireMock, 200);
        String token = TokenGenerator.validToken();
        EndpointClient.login(token);

        // Act & Assert — три действия подряд
        ApiSteps.assertResultOk(EndpointClient.action(token), 200);
        ApiSteps.assertResultOk(EndpointClient.action(token), 200);
        ApiSteps.assertResultOk(EndpointClient.action(token), 200);
    }
}
