package com.nebula.nebulaCloud.utils;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;

public class DatabaseCredentialGenerator {

    private static final SecureRandom random = new SecureRandom();
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+";
    private static final String ALL = UPPER + LOWER + DIGITS + SYMBOLS;

    // 🔹 Genera una contraseña segura aleatoria
    public static String generateSecurePassword(int length) {
        if (length < 12) length = 16; // mínimo 12-16 caracteres
        StringBuilder sb = new StringBuilder(length);

        // garantizar inclusión de tipos de caracteres
        sb.append(UPPER.charAt(random.nextInt(UPPER.length())));
        sb.append(LOWER.charAt(random.nextInt(LOWER.length())));
        sb.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        sb.append(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));

        for (int i = 4; i < length; i++) {
            sb.append(ALL.charAt(random.nextInt(ALL.length())));
        }

        // mezclar los caracteres
        char[] array = sb.toString().toCharArray();
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }

        return new String(array);
    }

    // 🔹 Genera un nombre de usuario aleatorio basado en el ID del usuario
    public static String generateRandomUser(Long userId) {
        String suffix = Long.toHexString(Math.abs(random.nextLong())).substring(0, 4);
        return String.format("u%d_%s", userId, suffix).toLowerCase(Locale.ROOT);
    }

    // 🔹 Genera un nombre de base de datos único
    public static String generateDatabaseName(Long userId) {
        long timestamp = Instant.now().getEpochSecond();
        return String.format("db_u%d_%d", userId, timestamp);
    }
}

