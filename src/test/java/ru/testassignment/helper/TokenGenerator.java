package ru.testassignment.helper;

import java.util.Random;

/**
 * Генератор токенов для тестов.
 * Валидный токен: 32 HEX-символа [0-9A-F] (регулярка приложения: ^[0-9A-F]{32}$).
 */
public final class TokenGenerator {

    // Приложение валидирует: ^[0-9A-F]{32}$ — только HEX-символы
    private static final String VALID_CHARS = "0123456789ABCDEF";
    private static final int VALID_LENGTH = 32;
    private static final Random RANDOM = new Random();

    private TokenGenerator() {
    }

    /** Валидный токен: 32 HEX-символа [0-9A-F] */
    public static String validToken() {
        StringBuilder sb = new StringBuilder(VALID_LENGTH);
        for (int i = 0; i < VALID_LENGTH; i++) {
            sb.append(VALID_CHARS.charAt(RANDOM.nextInt(VALID_CHARS.length())));
        }
        return sb.toString();
    }

    /** Слишком короткий токен (16 символов) */
    public static String shortToken() {
        return validToken().substring(0, 16);
    }

    /** Слишком длинный токен (64 символа) */
    public static String longToken() {
        return validToken() + validToken();
    }

    /** Токен в нижнем регистре (hex-символы, но lowercase) */
    public static String lowercaseToken() {
        return validToken().toLowerCase();
    }

    /** Токен со спецсимволами (32 символа, но содержит недопустимые) */
    public static String specialCharsToken() {
        return "ABCD1234!@#$%^&*5678ABCD!@#$%^&*";
    }

    /** Токен с буквами G-Z (невалидные для HEX) */
    public static String nonHexLettersToken() {
        return "GHIJKLMNOPQRSTUVWXYZ012345678901";
    }

    /** Пустой токен */
    public static String emptyToken() {
        return "";
    }
}
