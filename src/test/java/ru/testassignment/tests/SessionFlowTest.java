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
@Feature("Полный жизненный цикл сессии")
public class SessionFlowTest extends BaseTest {

    @Test
    @Story("Полный цикл сессии")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Полный цикл: LOGIN → ACTION → LOGOUT — все шаги успешны")
    @Description("End-to-end проверка основного пользовательского сценария: "
            + "аутентификация, выполнение действия, завершение сессии. "
            + "Каждый шаг должен вернуть OK.")
    void should_completeFullCycle_when_loginActionLogout() {
        // Arrange
        ApiSteps.stubAuth(wireMock, 200);
        ApiSteps.stubDoAction(wireMock, 200);
        String token = TokenGenerator.validToken();

        // Act & Assert — полный цикл
        ApiSteps.assertResultOk(EndpointClient.login(token), 200);
        ApiSteps.assertResultOk(EndpointClient.action(token), 200);
        ApiSteps.assertResultOk(EndpointClient.logout(token), 200);
    }

    @Test
    @Story("Повторная сессия")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Повторная сессия: LOGIN → LOGOUT → LOGIN → ACTION — успешно")
    @Description("После завершения сессии пользователь может начать новую. "
            + "Новый LOGIN с другим токеном после LOGOUT должен работать корректно.")
    void should_allowNewSession_when_loginAfterLogout() {
        // Arrange
        ApiSteps.stubAuth(wireMock, 200);
        ApiSteps.stubDoAction(wireMock, 200);
        String token1 = TokenGenerator.validToken();
        String token2 = TokenGenerator.validToken();

        // Act — первая сессия
        ApiSteps.assertResultOk(EndpointClient.login(token1), 200);
        ApiSteps.assertResultOk(EndpointClient.logout(token1), 200);

        // Act — вторая сессия с новым токеном
        ApiSteps.assertResultOk(EndpointClient.login(token2), 200);
        ApiSteps.assertResultOk(EndpointClient.action(token2), 200);
    }

    @Test
    @Story("Независимость сессий")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Независимость сессий: LOGOUT одного токена не влияет на другой")
    @Description("Два пользователя работают параллельно. Завершение сессии одного "
            + "не должно влиять на сессию другого. ACTION второго токена "
            + "после LOGOUT первого должен вернуть OK.")
    void should_keepOtherSession_when_oneTokenLogsOut() {
        // Arrange — два пользователя залогинены
        ApiSteps.stubAuth(wireMock, 200);
        ApiSteps.stubDoAction(wireMock, 200);
        String token1 = TokenGenerator.validToken();
        String token2 = TokenGenerator.validToken();
        EndpointClient.login(token1);
        EndpointClient.login(token2);

        // Act — первый выходит
        EndpointClient.logout(token1);

        // Assert — второй продолжает работать
        ApiSteps.assertResultOk(EndpointClient.action(token2), 200);
        // Первый уже не может — 403 (токен удалён)
        ApiSteps.assertResultError(EndpointClient.action(token1), 403);
    }
}
