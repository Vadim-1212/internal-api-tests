package ru.testassignment.base;

import ru.testassignment.config.TestConfig;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * Управление жизненным циклом тестируемого приложения.
 * Запускает jar через ProcessBuilder, ожидает готовности, останавливает.
 */
public class AppManager {

    private Process process;

    /**
     * Запускает приложение и ждёт, пока оно начнёт отвечать.
     * Jar ищется в app/ относительно корня проекта.
     */
    public void start() throws IOException, InterruptedException {
        String jarPath = findJar();

        ProcessBuilder pb = new ProcessBuilder(
                "java",
                "-jar",
                "-Dsecret=" + TestConfig.API_KEY,
                "-Dmock=" + TestConfig.MOCK_URL,
                jarPath
        );
        pb.redirectErrorStream(true);
        // Логи приложения в файл — не засоряют вывод тестов
        pb.redirectOutput(new File("target/app.log"));
        process = pb.start();

        waitForStartup();
    }

    /** Останавливает приложение */
    public void stop() {
        if (process != null && process.isAlive()) {
            process.destroy();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                process.destroyForcibly();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Проверяет готовность приложения — шлём запрос и ждём любой ответ.
     * Без X-Api-Key вернёт 401/403, но это значит что приложение запустилось.
     */
    private void waitForStartup() throws InterruptedException {
        long deadline = System.currentTimeMillis() + TestConfig.STARTUP_TIMEOUT_SEC * 1000L;

        while (System.currentTimeMillis() < deadline) {
            try {
                HttpURLConnection conn = (HttpURLConnection)
                        URI.create(TestConfig.BASE_URL + TestConfig.ENDPOINT_PATH).toURL().openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(1000);
                conn.setReadTimeout(1000);
                conn.getResponseCode(); // Любой ответ = приложение живо
                conn.disconnect();
                return;
            } catch (IOException e) {
                // Приложение ещё не готово — ждём
                Thread.sleep(TestConfig.POLL_INTERVAL_MS);
            }
        }

        stop();
        throw new RuntimeException(
                "Приложение не запустилось за " + TestConfig.STARTUP_TIMEOUT_SEC + " секунд");
    }

    /** Ищет jar-файл в директории app/ */
    private String findJar() {
        File appDir = new File("app");
        if (!appDir.exists()) {
            throw new RuntimeException("Директория app/ не найдена. Запускайте тесты из корня проекта.");
        }
        File[] jars = appDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars == null || jars.length == 0) {
            throw new RuntimeException("Jar-файл не найден в app/");
        }
        return jars[0].getAbsolutePath();
    }
}
