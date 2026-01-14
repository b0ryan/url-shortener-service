package service;

import model.Link;
import model.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис для сохранения и загрузки данных на диск
 */
public class StorageService {
    private static final String DATA_DIR = "data";
    private static final String LINKS_FILE = DATA_DIR + File.separator + "links.dat";
    private static final String USERS_FILE = DATA_DIR + File.separator + "users.dat";

    /**
     * Сохраняет ссылки на диск
     */
    public static void saveLinks(Map<String, Link> links) {
        try {
            createDataDirectory();
            
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(LINKS_FILE))) {
                oos.writeObject(links);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении ссылок: " + e.getMessage());
        }
    }

    /**
     * Загружает ссылки с диска
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Link> loadLinks() {
        Map<String, Link> links = new HashMap<>();
        
        if (!Files.exists(Paths.get(LINKS_FILE))) {
            return links;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(LINKS_FILE))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                links = (Map<String, Link>) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка при загрузке ссылок: " + e.getMessage());
            // Если файл поврежден, создаем новый
            links = new HashMap<>();
        }

        return links;
    }

    /**
     * Сохраняет пользователей на диск
     */
    public static void saveUsers(Map<UUID, User> users) {
        try {
            createDataDirectory();
            
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(USERS_FILE))) {
                oos.writeObject(users);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении пользователей: " + e.getMessage());
        }
    }

    /**
     * Загружает пользователей с диска
     */
    @SuppressWarnings("unchecked")
    public static Map<UUID, User> loadUsers() {
        Map<UUID, User> users = new HashMap<>();
        
        if (!Files.exists(Paths.get(USERS_FILE))) {
            return users;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(USERS_FILE))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                users = (Map<UUID, User>) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка при загрузке пользователей: " + e.getMessage());
            // Если файл поврежден, создаем новый
            users = new HashMap<>();
        }

        return users;
    }

    /**
     * Создает директорию для данных, если её нет
     */
    private static void createDataDirectory() {
        try {
            Path dataPath = Paths.get(DATA_DIR);
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при создании директории данных: " + e.getMessage());
        }
    }

    /**
     * Сохраняет все данные (ссылки и пользователей)
     */
    public static void saveAll(Map<String, Link> links, Map<UUID, User> users) {
        saveLinks(links);
        saveUsers(users);
    }

    /**
     * Удаляет все сохраненные данные
     */
    public static void clearAll() {
        try {
            Files.deleteIfExists(Paths.get(LINKS_FILE));
            Files.deleteIfExists(Paths.get(USERS_FILE));
        } catch (IOException e) {
            System.err.println("Ошибка при удалении данных: " + e.getMessage());
        }
    }
}
