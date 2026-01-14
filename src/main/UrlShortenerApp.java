import model.Link;
import service.LinkService;
import util.BrowserOpener;
import util.NotificationService;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * Главное консольное приложение для работы с сервисом сокращения ссылок
 */
public class UrlShortenerApp {
    private static LinkService linkService = new LinkService();
    private static Scanner scanner = new Scanner(System.in);
    private static UUID currentUserId = null;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n💾 Сохранение данных...");
            linkService.shutdown();
            System.out.println("✅ Данные сохранены. До свидания!");
        }));
        
        printWelcomeMessage();
        
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            
            try {
                switch (choice) {
                    case "1":
                        createShortLink();
                        break;
                    case "2":
                        openShortLink();
                        break;
                    case "3":
                        viewMyLinks();
                        break;
                    case "4":
                        editLink();
                        break;
                    case "5":
                        deleteLink();
                        break;
                    case "6":
                        showUserId();
                        break;
                    case "7":
                        setUserId();
                        break;
                    case "0":
                        running = false;
                        System.out.println("До свидания!");
                        break;
                    default:
                        System.out.println("Неверный выбор. Попробуйте снова.");
                }
            } catch (Exception e) {
                NotificationService.notifyError("Произошла ошибка: " + e.getMessage());
                e.printStackTrace();
            }
            
            if (running) {
                System.out.println("\nНажмите Enter для продолжения...");
                scanner.nextLine();
            }
        }
        
        linkService.shutdown();
        scanner.close();
    }

    private static void printWelcomeMessage() {
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("    СЕРВИС СОКРАЩЕНИЯ ССЫЛОК");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println();
    }

    private static void printMenu() {
        System.out.println("\n═══════════════════════════════════════════════════════");
        System.out.println("МЕНЮ:");
        System.out.println("1. Создать короткую ссылку");
        System.out.println("2. Перейти по короткой ссылке");
        System.out.println("3. Просмотреть мои ссылки");
        System.out.println("4. Редактировать ссылку");
        System.out.println("5. Удалить ссылку");
        System.out.println("6. Показать мой User ID");
        System.out.println("7. Установить User ID");
        System.out.println("0. Выход");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.print("Выберите действие: ");
    }

    private static void createShortLink() {
        System.out.println("\n--- Создание короткой ссылки ---");
        
        if (currentUserId == null) {
            currentUserId = UUID.randomUUID();
            System.out.println("Ваш User ID: " + currentUserId);
            System.out.println("Сохраните этот ID для доступа к вашим ссылкам!");
        }
        
        System.out.print("Введите URL для сокращения: ");
        String originalUrl = scanner.nextLine().trim();
        
        if (originalUrl.isEmpty()) {
            NotificationService.notifyError("URL не может быть пустым");
            return;
        }
        
        System.out.print("Введите лимит переходов (или Enter для 10): ");
        String limitInput = scanner.nextLine().trim();
        int clickLimit = 10; 
        if (!limitInput.isEmpty()) {
            try {
                clickLimit = Integer.parseInt(limitInput);
                if (clickLimit <= 0) {
                    NotificationService.notifyError("Лимит должен быть больше 0");
                    return;
                }
            } catch (NumberFormatException e) {
                NotificationService.notifyError("Неверный формат числа");
                return;
            }
        }
        

        System.out.print("Введите время жизни ссылки (например: 24ч, 3д, 12 часов, 2 дня) или Enter для 24 часов: ");
        String expirationInput = scanner.nextLine().trim();
        int expirationHours = 24; 
        
        if (!expirationInput.isEmpty()) {
            expirationInput = expirationInput.toLowerCase().trim();
            
            boolean isDays = false;
            int value = 0;
            
            if (expirationInput.endsWith("д") || expirationInput.endsWith("день") || 
                expirationInput.endsWith("дней") || expirationInput.endsWith("d") || 
                expirationInput.endsWith("day") || expirationInput.endsWith("days")) {
                isDays = true;
                String numberPart = expirationInput.replaceAll("[^0-9]", "");
                if (numberPart.isEmpty()) {
                    NotificationService.notifyError("Неверный формат времени жизни");
                    return;
                }
                value = Integer.parseInt(numberPart);
            } else if (expirationInput.endsWith("ч") || expirationInput.endsWith("час") || 
                       expirationInput.endsWith("часов") || expirationInput.endsWith("h") || 
                       expirationInput.endsWith("hour") || expirationInput.endsWith("hours")) {
                isDays = false;
                String numberPart = expirationInput.replaceAll("[^0-9]", "");
                if (numberPart.isEmpty()) {
                    NotificationService.notifyError("Неверный формат времени жизни");
                    return;
                }
                value = Integer.parseInt(numberPart);
            } else {
                try {
                    value = Integer.parseInt(expirationInput);
                    isDays = false;
                } catch (NumberFormatException e) {
                    NotificationService.notifyError("Неверный формат времени жизни. Используйте формат: число + единица (ч/д)");
                    return;
                }
            }
            
            if (value <= 0) {
                NotificationService.notifyError("Время жизни должно быть больше 0");
                return;
            }
            
            if (isDays) {
                expirationHours = value * 24;
            } else {
                expirationHours = value;
            }
        }
        
        try {
            String shortUrl = linkService.createShortLink(originalUrl, currentUserId, clickLimit, expirationHours);
            NotificationService.notifyLinkCreated(shortUrl, originalUrl);
            System.out.println("Лимит переходов: " + clickLimit);
            
            String expirationText;
            if (expirationHours >= 24 && expirationHours % 24 == 0) {
                int days = expirationHours / 24;
                expirationText = days + " " + formatDays(days);
            } else {
                expirationText = expirationHours + " " + formatHours(expirationHours);
            }
            System.out.println("Срок действия: " + expirationText);
        } catch (IllegalArgumentException e) {
            NotificationService.notifyError(e.getMessage());
        }
    }
    
    /**
     * Форматирует количество часов для вывода
     */
    private static String formatHours(int hours) {
        if (hours == 1) {
            return "час";
        } else if (hours >= 2 && hours <= 4) {
            return "часа";
        } else {
            return "часов";
        }
    }
    
    /**
     * Форматирует количество дней для вывода
     */
    private static String formatDays(int days) {
        if (days == 1) {
            return "день";
        } else if (days >= 2 && days <= 4) {
            return "дня";
        } else {
            return "дней";
        }
    }
    
    /**
     * Форматирует дату и время в формате dd.MM.yyyy HH:mm
     */
    private static String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return "не указано";
        }
        
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return dateTime.format(formatter);
    }

    private static void openShortLink() {
        System.out.println("\n--- Переход по короткой ссылке ---");
        System.out.print("Введите короткую ссылку (например, clck.ru/XXXXXX): ");
        String shortUrl = scanner.nextLine().trim();
        
        if (shortUrl.isEmpty()) {
            NotificationService.notifyError("Ссылка не может быть пустой");
            return;
        }
        
        String status = linkService.checkLinkStatus(shortUrl);
        if (status != null) {
            Link link = linkService.getLinkInfo(shortUrl);
            NotificationService.notifyLinkUnavailable(link, status);
            return;
        }
        
        String originalUrl = linkService.getOriginalUrl(shortUrl);
        
        if (originalUrl == null) {
            NotificationService.notifyError("Ссылка не найдена или недоступна");
            return;
        }
        
        System.out.println("Переход на: " + originalUrl);
        System.out.println("Открываю в браузере...");
        
        if (BrowserOpener.openInBrowser(originalUrl)) {
            System.out.println("✅ Браузер открыт!");
        }
        
        Link link = linkService.getLinkInfo(shortUrl);
        if (link != null && link.isClickLimitReached()) {
            NotificationService.notifyLinkUnavailable(link, "Лимит переходов исчерпан");
        }
    }

    private static void viewMyLinks() {
        System.out.println("\n--- Мои ссылки ---");
        
        if (currentUserId == null) {
            NotificationService.notifyError("User ID не установлен. Создайте ссылку или установите User ID.");
            return;
        }
        
        List<Link> userLinks = linkService.getUserLinks(currentUserId);
        
        if (userLinks.isEmpty()) {
            System.out.println("У вас пока нет созданных ссылок.");
            return;
        }
        
        System.out.println("Всего ссылок: " + userLinks.size());
        System.out.println("─────────────────────────────────────────────────────");
        
        for (int i = 0; i < userLinks.size(); i++) {
            Link link = userLinks.get(i);
            System.out.println((i + 1) + ". " + link.getShortUrl());
            System.out.println("   Оригинал: " + link.getOriginalUrl());
            System.out.println("   Переходов: " + link.getCurrentClicks() + " / " + link.getClickLimit());
            System.out.println("   Создана: " + formatDateTime(link.getCreatedAt()));
            System.out.println("   Истекает: " + formatDateTime(link.getExpiresAt()));
            System.out.println("   Статус: " + (link.canBeAccessed() ? "✅ Активна" : "❌ Недоступна"));
            System.out.println("─────────────────────────────────────────────────────");
        }
    }

    private static void editLink() {
        System.out.println("\n--- Редактирование ссылки ---");
        
        if (currentUserId == null) {
            NotificationService.notifyError("User ID не установлен.");
            return;
        }
        
        System.out.print("Введите короткую ссылку для редактирования: ");
        String shortUrl = scanner.nextLine().trim();
        
        if (shortUrl.isEmpty()) {
            NotificationService.notifyError("Ссылка не может быть пустой");
            return;
        }
        
        Link link = linkService.getLinkInfo(shortUrl);
        if (link == null) {
            NotificationService.notifyError("Ссылка не найдена");
            return;
        }
        
        if (!link.getUserId().equals(currentUserId)) {
            NotificationService.notifyError("Вы не являетесь владельцем этой ссылки");
            return;
        }
        
        System.out.println("\nТекущие параметры ссылки:");
        System.out.println("Короткая ссылка: " + link.getShortUrl());
        System.out.println("Оригинальный URL: " + link.getOriginalUrl());
        System.out.println("Лимит переходов: " + link.getClickLimit() + " (использовано: " + link.getCurrentClicks() + ")");
        
        long hoursRemaining = java.time.Duration.between(java.time.LocalDateTime.now(), link.getExpiresAt()).toHours();
        if (hoursRemaining > 0) {
            if (hoursRemaining >= 24 && hoursRemaining % 24 == 0) {
                System.out.println("Осталось времени: " + (hoursRemaining / 24) + " " + formatDays((int)(hoursRemaining / 24)));
            } else {
                System.out.println("Осталось времени: " + hoursRemaining + " " + formatHours((int)hoursRemaining));
            }
        } else {
            System.out.println("Время жизни: истекло");
        }
        
        System.out.println("\n--- Изменение параметров ---");
        System.out.println("(Нажмите Enter, чтобы оставить значение без изменений)");
        
        System.out.print("Новый лимит переходов (текущий: " + link.getClickLimit() + "): ");
        String limitInput = scanner.nextLine().trim();
        Integer newClickLimit = null;
        
        if (!limitInput.isEmpty()) {
            try {
                newClickLimit = Integer.parseInt(limitInput);
                if (newClickLimit <= 0) {
                    NotificationService.notifyError("Лимит должен быть больше 0");
                    return;
                }
            } catch (NumberFormatException e) {
                NotificationService.notifyError("Неверный формат числа");
                return;
            }
        }
        
        System.out.print("Новое время жизни ссылки (например: 24ч, 3д) или Enter для текущего: ");
        String expirationInput = scanner.nextLine().trim();
        Integer newExpirationHours = null;
        
        if (!expirationInput.isEmpty()) {
            expirationInput = expirationInput.toLowerCase().trim();
            
            boolean isDays = false;
            int value = 0;
            
            if (expirationInput.endsWith("д") || expirationInput.endsWith("день") || 
                expirationInput.endsWith("дней") || expirationInput.endsWith("d") || 
                expirationInput.endsWith("day") || expirationInput.endsWith("days")) {
                isDays = true;
                String numberPart = expirationInput.replaceAll("[^0-9]", "");
                if (numberPart.isEmpty()) {
                    NotificationService.notifyError("Неверный формат времени жизни");
                    return;
                }
                value = Integer.parseInt(numberPart);
            } else if (expirationInput.endsWith("ч") || expirationInput.endsWith("час") || 
                       expirationInput.endsWith("часов") || expirationInput.endsWith("h") || 
                       expirationInput.endsWith("hour") || expirationInput.endsWith("hours")) {
                isDays = false;
                String numberPart = expirationInput.replaceAll("[^0-9]", "");
                if (numberPart.isEmpty()) {
                    NotificationService.notifyError("Неверный формат времени жизни");
                    return;
                }
                value = Integer.parseInt(numberPart);
            } else {
                try {
                    value = Integer.parseInt(expirationInput);
                    isDays = false;
                } catch (NumberFormatException e) {
                    NotificationService.notifyError("Неверный формат времени жизни. Используйте формат: число + единица (ч/д)");
                    return;
                }
            }
            
            if (value <= 0) {
                NotificationService.notifyError("Время жизни должно быть больше 0");
                return;
            }
            
            if (isDays) {
                newExpirationHours = value * 24;
            } else {
                newExpirationHours = value;
            }
        }
        
        boolean updated = linkService.updateLink(shortUrl, currentUserId, newClickLimit, newExpirationHours);
        
        if (updated) {
            NotificationService.notifySuccess("Ссылка успешно обновлена!");
            System.out.println("⚠️ Внимание: При изменении параметров счетчик переходов был сброшен.");
        } else if (newClickLimit == null && newExpirationHours == null) {
            NotificationService.notifySuccess("Параметры ссылки не изменены.");
        } else {
            boolean limitSame = newClickLimit != null && newClickLimit.equals(link.getClickLimit());
            boolean expirationSame = newExpirationHours != null;
            
            if (limitSame || expirationSame) {
                NotificationService.notifySuccess("Параметры ссылки не изменены (новые значения совпадают с текущими).");
            } else {
                NotificationService.notifyError("Ошибка при обновлении ссылки");
            }
        }
    }

    private static void deleteLink() {
        System.out.println("\n--- Удаление ссылки ---");
        
        if (currentUserId == null) {
            NotificationService.notifyError("User ID не установлен.");
            return;
        }
        
        System.out.print("Введите короткую ссылку для удаления: ");
        String shortUrl = scanner.nextLine().trim();
        
        if (shortUrl.isEmpty()) {
            NotificationService.notifyError("Ссылка не может быть пустой");
            return;
        }
        
        boolean deleted = linkService.deleteLink(shortUrl, currentUserId);
        if (deleted) {
            NotificationService.notifyLinkDeleted(shortUrl);
        } else {
            Link link = linkService.getLinkInfo(shortUrl);
            if (link == null) {
                NotificationService.notifyError("Ссылка не найдена");
            } else {
                NotificationService.notifyError("Вы не являетесь владельцем этой ссылки");
            }
        }
    }

    private static void showUserId() {
        System.out.println("\n--- Мой User ID ---");
        if (currentUserId == null) {
            System.out.println("User ID не установлен. Он будет создан при создании первой ссылки.");
        } else {
            System.out.println("Ваш User ID: " + currentUserId);
            System.out.println("Сохраните этот ID для доступа к вашим ссылкам в будущем!");
        }
    }

    private static void setUserId() {
        System.out.println("\n--- Установка User ID ---");
        System.out.print("Введите User ID (UUID): ");
        String userIdInput = scanner.nextLine().trim();
        
        if (userIdInput.isEmpty()) {
            NotificationService.notifyError("User ID не может быть пустым");
            return;
        }
        
        try {
            currentUserId = UUID.fromString(userIdInput);
            NotificationService.notifySuccess("User ID установлен: " + currentUserId);
            
            List<Link> userLinks = linkService.getUserLinks(currentUserId);
            System.out.println("Найдено ссылок для этого User ID: " + userLinks.size());
        } catch (IllegalArgumentException e) {
            NotificationService.notifyError("Неверный формат UUID");
        }
    }
}
