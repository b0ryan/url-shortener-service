package com.urlshortener.util;

import java.awt.Desktop;
import java.net.URI;

/**
 * Утилита для открытия URL в браузере
 */
public class BrowserOpener {
    
    /**
     * Открывает URL в браузере пользователя
     * @param url URL для открытия
     * @return true, если браузер успешно открыт
     */
    public static boolean openInBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                return true;
            } else {
                System.out.println("⚠️ Не удалось открыть браузер автоматически. Пожалуйста, откройте ссылку вручную: " + url);
                return false;
            }
        } catch (Exception e) {
            System.out.println("⚠️ Ошибка при открытии браузера: " + e.getMessage());
            System.out.println("Пожалуйста, откройте ссылку вручную: " + url);
            return false;
        }
    }
}
