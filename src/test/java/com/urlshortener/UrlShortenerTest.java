package com.urlshortener;

import com.urlshortener.model.Link;
import com.urlshortener.service.LinkService;
import com.urlshortener.service.ShortUrlGenerator;
import org.junit.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.Assert.*;

/**
 * Базовые тесты для сервиса сокращения ссылок
 */
public class UrlShortenerTest {

    @Test
    public void testCreateShortLink() {
        LinkService service = new LinkService();
        UUID userId = UUID.randomUUID();
        String originalUrl = "https://www.example.com";
        
        String shortUrl = service.createShortLink(originalUrl, userId, 10, 24);
        
        assertNotNull("Короткая ссылка не должна быть null", shortUrl);
        assertTrue("Короткая ссылка должна начинаться с clck.ru/", shortUrl.startsWith("clck.ru/"));
        
        Link link = service.getLinkInfo(shortUrl);
        assertNotNull("Ссылка должна быть найдена", link);
        assertEquals("Оригинальный URL должен совпадать", originalUrl, link.getOriginalUrl());
        assertEquals("User ID должен совпадать", userId, link.getUserId());
    }

    @Test
    public void testUniqueLinksForDifferentUsers() {
        LinkService service = new LinkService();
        String originalUrl = "https://www.example.com";
        
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        
        String shortUrl1 = service.createShortLink(originalUrl, userId1, 10, 24);
        String shortUrl2 = service.createShortLink(originalUrl, userId2, 10, 24);
        
        assertNotEquals("Разные пользователи должны получать разные короткие ссылки", 
                        shortUrl1, shortUrl2);
    }

    @Test
    public void testClickLimit() {
        LinkService service = new LinkService();
        UUID userId = UUID.randomUUID();
        String originalUrl = "https://www.example.com";
        int clickLimit = 2;
        
        String shortUrl = service.createShortLink(originalUrl, userId, clickLimit, 24);
        
        // Первый переход
        String url1 = service.getOriginalUrl(shortUrl);
        assertNotNull("Первый переход должен быть успешным", url1);
        
        // Второй переход
        String url2 = service.getOriginalUrl(shortUrl);
        assertNotNull("Второй переход должен быть успешным", url2);
        
        // Третий переход - должен быть заблокирован
        String url3 = service.getOriginalUrl(shortUrl);
        assertNull("Третий переход должен быть заблокирован", url3);
        
        Link link = service.getLinkInfo(shortUrl);
        assertTrue("Лимит переходов должен быть достигнут", link.isClickLimitReached());
    }

    @Test
    public void testLinkExpiration() {
        LinkService service = new LinkService();
        UUID userId = UUID.randomUUID();
        String originalUrl = "https://www.example.com";
        
        String shortUrl = service.createShortLink(originalUrl, userId, 10, 24);
        Link link = service.getLinkInfo(shortUrl);
        
        // Устанавливаем время истечения в прошлое
        link.setExpiresAt(LocalDateTime.now().minusHours(1));
        
        assertTrue("Ссылка должна быть просрочена", link.isExpired());
        assertFalse("Ссылка не должна быть доступна", link.canBeAccessed());
        
        String url = service.getOriginalUrl(shortUrl);
        assertNull("Просроченная ссылка не должна быть доступна", url);
    }

    @Test
    public void testShortUrlGenerator() {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        String originalUrl = "https://www.example.com";
        
        String shortUrl1 = ShortUrlGenerator.generateUniqueShortUrl(userId1, originalUrl);
        String shortUrl2 = ShortUrlGenerator.generateUniqueShortUrl(userId2, originalUrl);
        
        assertNotNull("Короткая ссылка не должна быть null", shortUrl1);
        assertTrue("Короткая ссылка должна начинаться с clck.ru/", shortUrl1.startsWith("clck.ru/"));
        assertNotEquals("Разные пользователи должны получать разные ссылки", shortUrl1, shortUrl2);
    }

    @Test
    public void testDeleteLink() {
        LinkService service = new LinkService();
        UUID userId = UUID.randomUUID();
        String originalUrl = "https://www.example.com";
        
        String shortUrl = service.createShortLink(originalUrl, userId, 10, 24);
        
        // Удаление своей ссылки
        boolean deleted = service.deleteLink(shortUrl, userId);
        assertTrue("Ссылка должна быть удалена", deleted);
        
        Link link = service.getLinkInfo(shortUrl);
        assertNull("Удаленная ссылка не должна быть найдена", link);
    }

    @Test
    public void testDeleteOtherUserLink() {
        LinkService service = new LinkService();
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        String originalUrl = "https://www.example.com";
        
        String shortUrl = service.createShortLink(originalUrl, userId1, 10);
        
        // Попытка удалить чужую ссылку
        boolean deleted = service.deleteLink(shortUrl, userId2);
        assertFalse("Чужая ссылка не должна быть удалена", deleted);
        
        Link link = service.getLinkInfo(shortUrl);
        assertNotNull("Ссылка должна остаться", link);
    }

    @Test
    public void testGetUserLinks() {
        LinkService service = new LinkService();
        UUID userId = UUID.randomUUID();
        
        String shortUrl1 = service.createShortLink("https://example1.com", userId, 10, 24);
        String shortUrl2 = service.createShortLink("https://example2.com", userId, 10, 24);
        
        var userLinks = service.getUserLinks(userId);
        assertEquals("У пользователя должно быть 2 ссылки", 2, userLinks.size());
    }

    @Test
    public void testLinkStatus() {
        LinkService service = new LinkService();
        UUID userId = UUID.randomUUID();
        String originalUrl = "https://www.example.com";
        
        String shortUrl = service.createShortLink(originalUrl, userId, 1);
        
        // Первый переход
        service.getOriginalUrl(shortUrl);
        
        // Проверка статуса после исчерпания лимита
        String status = service.checkLinkStatus(shortUrl);
        assertNotNull("Статус должен указывать на проблему", status);
        assertTrue("Статус должен указывать на исчерпание лимита", 
                   status.contains("Лимит") || status.contains("исчерпан"));
    }
}
