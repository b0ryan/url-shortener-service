package com.urlshortener.service;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Генератор коротких ссылок
 */
public class ShortUrlGenerator {
    private static final String BASE_URL = "clck.ru/";
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHORT_URL_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    /**
     * Генерирует уникальную короткую ссылку для пользователя
     * @param userId UUID пользователя для обеспечения уникальности
     * @return короткая ссылка вида clck.ru/XXXXXX
     */
    public static String generateShortUrl(UUID userId) {
        StringBuilder shortCode = new StringBuilder();
        
        // Добавляем часть UUID для уникальности
        String userIdHash = userId.toString().replace("-", "").substring(0, 2);
        
        // Генерируем случайный код
        for (int i = 0; i < SHORT_URL_LENGTH - 2; i++) {
            shortCode.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        
        // Комбинируем для обеспечения уникальности для каждого пользователя
        return BASE_URL + userIdHash + shortCode.toString();
    }

    /**
     * Генерирует короткую ссылку с дополнительным хешем для большей уникальности
     */
    public static String generateUniqueShortUrl(UUID userId, String originalUrl) {
        // Комбинируем userId и originalUrl для создания уникального хеша
        String combined = userId.toString() + originalUrl;
        int hash = combined.hashCode();
        
        StringBuilder shortCode = new StringBuilder();
        String userIdHash = userId.toString().replace("-", "").substring(0, 2);
        
        // Используем хеш для генерации кода
        int hashValue = Math.abs(hash);
        for (int i = 0; i < SHORT_URL_LENGTH - 2; i++) {
            int index = (hashValue + i) % CHARACTERS.length();
            shortCode.append(CHARACTERS.charAt(index));
        }
        
        // Добавляем случайный элемент для уникальности
        shortCode.setCharAt(0, CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        
        return BASE_URL + userIdHash + shortCode.toString();
    }
}
