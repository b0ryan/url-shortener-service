package util;

import model.Link;

/**
 * Сервис уведомлений пользователя
 */
public class NotificationService {
    
    /**
     * Уведомляет пользователя о недоступности ссылки
     * @param link ссылка
     * @param reason причина недоступности
     */
    public static void notifyLinkUnavailable(Link link, String reason) {
        if (link == null) {
            System.out.println("⚠️ Уведомление: Ссылка не найдена");
            return;
        }

        System.out.println("═══════════════════════════════════════");
        System.out.println("⚠️ УВЕДОМЛЕНИЕ");
        System.out.println("═══════════════════════════════════════");
        System.out.println("Короткая ссылка: " + link.getShortUrl());
        System.out.println("Оригинальный URL: " + link.getOriginalUrl());
        System.out.println("Причина: " + reason);
        
        if (link.isClickLimitReached()) {
            System.out.println("Переходов использовано: " + link.getCurrentClicks() + " / " + link.getClickLimit());
        }
        
        if (link.isExpired()) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            System.out.println("Срок действия истек: " + link.getExpiresAt().format(formatter));
        }
        
        System.out.println("═══════════════════════════════════════");
    }

    /**
     * Уведомляет о создании ссылки
     * @param shortUrl короткая ссылка
     * @param originalUrl оригинальный URL
     */
    public static void notifyLinkCreated(String shortUrl, String originalUrl) {
        System.out.println("✅ Ссылка успешно создана!");
        System.out.println("Короткая ссылка: " + shortUrl);
        System.out.println("Оригинальный URL: " + originalUrl);
    }

    /**
     * Уведомляет об удалении ссылки
     * @param shortUrl короткая ссылка
     */
    public static void notifyLinkDeleted(String shortUrl) {
        System.out.println("🗑️ Ссылка " + shortUrl + " успешно удалена");
    }

    /**
     * Уведомляет об ошибке
     * @param message сообщение об ошибке
     */
    public static void notifyError(String message) {
        System.out.println("❌ Ошибка: " + message);
    }

    /**
     * Уведомляет об успешной операции
     * @param message сообщение
     */
    public static void notifySuccess(String message) {
        System.out.println("✅ " + message);
    }
}
