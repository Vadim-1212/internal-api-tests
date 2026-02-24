package ru.testassignment.tests;

import io.qameta.allure.*;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.testassignment.base.BaseTest;
import ru.testassignment.client.EndpointClient;
import ru.testassignment.helper.TokenGenerator;
import ru.testassignment.steps.ApiSteps;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@Epic("API тестирование сервиса аутентификации")
@Feature("Валидация входных данных")
public class ValidationTest extends BaseTest {

    // --- API-ключ ---

    @Test
    @Story("Отсутствие API-ключа")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Запрос без заголовка X-Api-Key отклоняется")
    @Description("API-ключ обязателен для доступа к эндпоинту. "
            + "Запрос без заголовка X-Api-Key должен быть отклонён на уровне безопасности.")
    void should_returnUnauthorized_when_noApiKey() {
        // Arrange
        String token = TokenGenerator.validToken();

        // Act
        ValidatableResponse response = EndpointClient.sendWithoutApiKey(token, "LOGIN");

        // Assert — Spring Security возвращает 401 или 403
        response.statusCode(anyOf(is(401), is(403)));
    }

    @Test
    @Story("Невалидный API-ключ")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Запрос с неверным X-Api-Key отклоняется")
    @Description("API-ключ передан, но значение неверное. "
            + "Приложение должно отклонить запрос.")
    void should_returnUnauthorized_when_invalidApiKey() {
        // Arrange
        String token = TokenGenerator.validToken();

        // Act
        ValidatableResponse response = EndpointClient.sendWithInvalidApiKey(token, "LOGIN");

        // Assert
        response.statusCode(anyOf(is(401), is(403)));
    }

    // --- Токен ---

    @Test
    @Story("Слишком короткий токен")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Токен короче 32 символов отклоняется (400)")
    @Description("Валидный токен — ровно 32 HEX-символа (0-9, A-F). "
            + "Токен из 16 символов не проходит валидацию ^[0-9A-F]{32}$.")
    void should_returnBadRequest_when_tokenTooShort() {
        // Arrange
        String token = TokenGenerator.shortToken();

        // Act
        ValidatableResponse response = EndpointClient.login(token);

        // Assert — Spring validation отвечает 400
        response.statusCode(400)
                .body("result", equalTo("ERROR"));
    }

    @Test
    @Story("Слишком длинный токен")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Токен длиннее 32 символов отклоняется (400)")
    @Description("Валидный токен — ровно 32 HEX-символа. "
            + "Токен из 64 символов не проходит валидацию.")
    void should_returnBadRequest_when_tokenTooLong() {
        // Arrange
        String token = TokenGenerator.longToken();

        // Act
        ValidatableResponse response = EndpointClient.login(token);

        // Assert
        response.statusCode(400)
                .body("result", equalTo("ERROR"));
    }

    @Test
    @Story("Токен в нижнем регистре")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Токен из строчных букв отклоняется (400)")
    @Description("Токен должен содержать только символы 0-9 и A-F в верхнем регистре. "
            + "Строчные буквы (a-f) недопустимы.")
    void should_returnBadRequest_when_tokenLowercase() {
        // Arrange
        String token = TokenGenerator.lowercaseToken();

        // Act
        ValidatableResponse response = EndpointClient.login(token);

        // Assert
        response.statusCode(400)
                .body("result", equalTo("ERROR"));
    }

    @Test
    @Story("Токен со спецсимволами")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Токен со спецсимволами отклоняется (400)")
    @Description("Токен содержит символы !@#$%^&* — они недопустимы. "
            + "Допускаются только 0-9 и A-F.")
    void should_returnBadRequest_when_tokenHasSpecialChars() {
        // Arrange
        String token = TokenGenerator.specialCharsToken();

        // Act
        ValidatableResponse response = EndpointClient.login(token);

        // Assert
        response.statusCode(400)
                .body("result", equalTo("ERROR"));
    }

    @Test
    @Story("Пустой токен")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Пустой токен отклоняется (400)")
    @Description("Токен не может быть пустой строкой — не соответствует ^[0-9A-F]{32}$.")
    void should_returnBadRequest_when_tokenEmpty() {
        // Arrange
        String token = TokenGenerator.emptyToken();

        // Act
        ValidatableResponse response = EndpointClient.login(token);

        // Assert
        response.statusCode(400)
                .body("result", equalTo("ERROR"));
    }

    @Test
    @Story("Токен с невалидными буквами")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Токен с буквами G-Z отклоняется (400)")
    @Description("Допустимы только HEX-символы (0-9, A-F). "
            + "Буквы G-Z не входят в HEX-диапазон.")
    void should_returnBadRequest_when_tokenHasNonHexLetters() {
        // Arrange
        String token = TokenGenerator.nonHexLettersToken();

        // Act
        ValidatableResponse response = EndpointClient.login(token);

        // Assert
        response.statusCode(400)
                .body("result", equalTo("ERROR"));
    }

    // --- Action ---

    @Test
    @Story("Пустой action")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Пустое значение action отклоняется")
    @Description("Поле action обязательно и должно содержать одно из значений: "
            + "LOGIN, ACTION, LOGOUT. Пустая строка недопустима.")
    void should_returnError_when_actionEmpty() {
        // Arrange
        String token = TokenGenerator.validToken();

        // Act
        ValidatableResponse response = EndpointClient.send(token, "");

        // Assert — 400 (невалидный action) или 200+ERROR
        response.statusCode(anyOf(is(200), is(400)))
                .body("result", equalTo("ERROR"));
    }

    @Test
    @Story("Невалидный action")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Несуществующий action ('INVALID') отклоняется")
    @Description("Action 'INVALID' не входит в допустимые значения (LOGIN/ACTION/LOGOUT). "
            + "Приложение должно отклонить такой запрос.")
    void should_returnError_when_actionInvalid() {
        // Arrange
        String token = TokenGenerator.validToken();

        // Act
        ValidatableResponse response = EndpointClient.send(token, "INVALID");

        // Assert
        response.statusCode(anyOf(is(200), is(400)))
                .body("result", equalTo("ERROR"));
    }

    @Test
    @Story("Action в нижнем регистре")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Action в нижнем регистре ('login') отклоняется")
    @Description("Action чувствителен к регистру. 'login' вместо 'LOGIN' "
            + "должен быть отклонён.")
    void should_returnError_when_actionLowercase() {
        // Arrange
        String token = TokenGenerator.validToken();

        // Act
        ValidatableResponse response = EndpointClient.send(token, "login");

        // Assert
        response.statusCode(anyOf(is(200), is(400)))
                .body("result", equalTo("ERROR"));
    }

    @Test
    @Story("Пустой запрос")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Запрос без token и action отклоняется")
    @Description("Оба обязательных поля отсутствуют. "
            + "Приложение должно отклонить запрос.")
    void should_returnError_when_requestEmpty() {
        // Act
        ValidatableResponse response = EndpointClient.sendEmpty();

        // Assert
        response.statusCode(anyOf(is(200), is(400)))
                .body("result", equalTo("ERROR"));
    }
}
