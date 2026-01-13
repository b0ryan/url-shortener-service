package com.urlshortener.service;

import com.urlshortener.model.Link;
import com.urlshortener.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Сервис для управления ссылками
 */
public class LinkService {
    private static final int DEFAULT_EXPIRATION_HOURS = 24; // Сутки по умолчанию
    
    // Хранилище ссылок: shortUrl -> Link
    private final Map<String, Link> links = new ConcurrentHashMap<>();
    
    // Хранилище пользователей: userId -> User
    private final Map<UUID, User> users = new ConcurrentHashMap<>();
    
    // Поток для очистки просроченных ссылок
    private Timer expirationTimer;

    public LinkService() {
        // Загружаем данные с диска при создании сервиса
        loadData();
        startExpirationCleanup();
    }

    /**
     * Загружает данные с диска
     */
    private void loadData() {
        Map<String, Link> loadedLinks = StorageService.loadLinks();
        Map<UUID, User> loadedUsers = StorageService.loadUsers();
        
        if (!loadedLinks.isEmpty() || !loadedUsers.isEmpty()) {
            links.putAll(loadedLinks);
            users.putAll(loadedUsers);
            System.out.println("💾 Загружено данных: " + loadedLinks.size() + " ссылок, " + loadedUsers.size() + " пользователей");
        }
    }

    /**
     * Сохраняет данные на диск
     */
    public void saveData() {
        StorageService.saveAll(links, users);
    }

    /**
     * Создает короткую ссылку для пользователя
     * @param originalUrl исходный URL
     * @param userId UUID пользователя (если null, создается новый пользователь)
     * @param clickLimit лимит переходов
     * @param expirationHours время жизни ссылки в часах
     * @return короткая ссылка
     */
    public String createShortLink(String originalUrl, UUID userId, int clickLimit, int expirationHours) {
        // Валидация URL
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("URL не может быть пустым");
        }
        
        if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
            originalUrl = "https://" + originalUrl;
        }

        // Создаем или получаем пользователя
        User user;
        if (userId == null) {
            user = new User();
            users.put(user.getId(), user);
        } else {
            user = users.get(userId);
            if (user == null) {
                user = new User(userId);
                users.put(user.getId(), user);
            }
        }

        // Генерируем уникальную короткую ссылку
        String shortUrl = ShortUrlGenerator.generateUniqueShortUrl(user.getId(), originalUrl);
        
        // Проверяем уникальность (на случай коллизии)
        int attempts = 0;
        while (links.containsKey(shortUrl) && attempts < 10) {
            shortUrl = ShortUrlGenerator.generateUniqueShortUrl(user.getId(), originalUrl + System.currentTimeMillis());
            attempts++;
        }

        // Создаем ссылку с заданным временем жизни
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(expirationHours);
        Link link = new Link(shortUrl, originalUrl, user.getId(), clickLimit, expiresAt);

        // Сохраняем ссылку
        links.put(shortUrl, link);
        user.addShortUrl(shortUrl);

        // Сохраняем данные на диск
        saveData();

        return shortUrl;
    }

    /**
     * Получает оригинальный URL по короткой ссылке
     * @param shortUrl короткая ссылка
     * @return оригинальный URL или null, если ссылка недоступна
     */
    public String getOriginalUrl(String shortUrl) {
        Link link = links.get(shortUrl);
        
        if (link == null) {
            return null;
        }

        // Проверяем доступность ссылки
        if (!link.canBeAccessed()) {
            return null;
        }

        // Увеличиваем счетчик переходов
        link.incrementClicks();
        
        // Сохраняем изменения (счетчик переходов)
        saveData();
        
        return link.getOriginalUrl();
    }

    /**
     * Получает информацию о ссылке
     * @param shortUrl короткая ссылка
     * @return объект Link или null
     */
    public Link getLinkInfo(String shortUrl) {
        return links.get(shortUrl);
    }

    /**
     * Получает все ссылки пользователя
     * @param userId UUID пользователя
     * @return список ссылок пользователя
     */
    public List<Link> getUserLinks(UUID userId) {
        User user = users.get(userId);
        if (user == null) {
            return new ArrayList<>();
        }
        
        return user.getShortUrls().stream()
                .map(links::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Обновляет параметры ссылки (только если пользователь является владельцем)
     * При изменении параметров сбрасывает счетчик переходов и время жизни
     * @param shortUrl короткая ссылка
     * @param userId UUID пользователя
     * @param newClickLimit новый лимит переходов (null, если не изменять)
     * @param newExpirationHours новое время жизни в часах (null, если не изменять)
     * @return true, если ссылка обновлена
     */
    public boolean updateLink(String shortUrl, UUID userId, Integer newClickLimit, Integer newExpirationHours) {
        Link link = links.get(shortUrl);
        
        if (link == null) {
            return false;
        }

        if (!link.getUserId().equals(userId)) {
            return false;
        }

        boolean clickLimitChanged = false;
        boolean expirationChanged = false;
        
        // Обновляем лимит переходов только если значение действительно изменилось
        if (newClickLimit != null && !newClickLimit.equals(link.getClickLimit())) {
            link.setClickLimit(newClickLimit);
            clickLimitChanged = true;
        }
        
        // Обновляем время жизни только если значение действительно изменилось
        if (newExpirationHours != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime newExpiresAt = now.plusHours(newExpirationHours);
            LocalDateTime currentExpiresAt = link.getExpiresAt();
            
            // Вычисляем оставшееся время до текущего истечения
            long currentHoursRemaining = java.time.Duration.between(now, currentExpiresAt).toHours();
            
            // Если оставшееся время отличается от нового времени жизни более чем на 1 час, считаем что время изменилось
            if (Math.abs(currentHoursRemaining - newExpirationHours) > 1) {
                link.setExpiresAt(newExpiresAt);
                expirationChanged = true;
            }
        }
        
        // Сбрасываем счетчик переходов только если действительно изменился лимит или время жизни
        if (clickLimitChanged || expirationChanged) {
            link.setCurrentClicks(0);
            // Сохраняем изменения
            saveData();
            return true;
        }
        
        // Если ничего не изменилось, возвращаем false
        return false;
    }

    /**
     * Проверяет статус ссылки и возвращает причину недоступности, если есть
     * @param shortUrl короткая ссылка
     * @return сообщение о статусе или null, если ссылка доступна
     */
    public String checkLinkStatus(String shortUrl) {
        Link link = links.get(shortUrl);
        
        if (link == null) {
            return "Ссылка не найдена";
        }

        if (link.isExpired()) {
            return "Срок действия ссылки истек";
        }

        if (link.isClickLimitReached()) {
            return "Лимит переходов исчерпан";
        }

        if (!link.isActive()) {
            return "Ссылка деактивирована";
        }

        return null; // Ссылка доступна
    }

    /**
     * Запускает периодическую очистку просроченных ссылок
     */
    private void startExpirationCleanup() {
        expirationTimer = new Timer(true); // daemon thread
        expirationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cleanupExpiredLinks();
            }
        }, 0, 60000); // Проверка каждую минуту
    }

    /**
     * Удаляет просроченные ссылки
     */
    private void cleanupExpiredLinks() {
        List<String> expiredUrls = links.entrySet().stream()
                .filter(entry -> entry.getValue().isExpired())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        for (String shortUrl : expiredUrls) {
            Link link = links.remove(shortUrl);
            if (link != null) {
                User user = users.get(link.getUserId());
                if (user != null) {
                    user.removeShortUrl(shortUrl);
                }
            }
        }
        
        // Сохраняем изменения после очистки
        if (!expiredUrls.isEmpty()) {
            saveData();
        }
    }

    /**
     * Останавливает сервис и очищает ресурсы
     */
    public void shutdown() {
        // Сохраняем данные перед закрытием
        saveData();
        
        if (expirationTimer != null) {
            expirationTimer.cancel();
        }
    }

    /**
     * Получает статистику по ссылке
     * @param shortUrl короткая ссылка
     * @return строка со статистикой
     */
    public String getLinkStatistics(String shortUrl) {
        Link link = links.get(shortUrl);
        if (link == null) {
            return "Ссылка не найдена";
        }

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        
        return String.format(
            "Статистика ссылки %s:\n" +
            "Оригинальный URL: %s\n" +
            "Переходов: %d / %d\n" +
            "Создана: %s\n" +
            "Истекает: %s\n" +
            "Статус: %s",
            shortUrl,
            link.getOriginalUrl(),
            link.getCurrentClicks(),
            link.getClickLimit(),
            link.getCreatedAt().format(formatter),
            link.getExpiresAt().format(formatter),
            link.canBeAccessed() ? "Активна" : "Недоступна"
        );
    }
}
