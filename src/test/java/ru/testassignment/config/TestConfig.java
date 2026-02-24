package ru.testassignment.config;

/**
 * Конфигурация тестового окружения.
 * Порты и секреты соответствуют параметрам запуска приложения.
 */
public final class TestConfig {

    // Порт тестируемого приложения
    public static final int APP_PORT = 8080;

    // Порт WireMock (внешний сервис)
    public static final int MOCK_PORT = 8888;

    // API-ключ для доступа к эндпоинту (передаётся через -Dsecret)
    public static final String API_KEY = "qazWSXedc";

    // Базовый URL приложения
    public static final String BASE_URL = "http://localhost:" + APP_PORT;

    // URL внешнего сервиса (WireMock)
    public static final String MOCK_URL = "http://localhost:" + MOCK_PORT + "/";

    // Путь к эндпоинту
    public static final String ENDPOINT_PATH = "/endpoint";

    // Таймаут ожидания запуска приложения (секунды)
    public static final int STARTUP_TIMEOUT_SEC = 30;

    // Интервал проверки готовности (миллисекунды)
    public static final int POLL_INTERVAL_MS = 500;

    private TestConfig() {
    }
}
