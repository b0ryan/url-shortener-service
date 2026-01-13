package com.urlshortener.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Модель пользователя
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private UUID id;
    private List<String> shortUrls;

    public User() {
        this.id = UUID.randomUUID();
        this.shortUrls = new ArrayList<>();
    }

    public User(UUID id) {
        this.id = id;
        this.shortUrls = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<String> getShortUrls() {
        return shortUrls;
    }

    public void setShortUrls(List<String> shortUrls) {
        this.shortUrls = shortUrls;
    }

    public void addShortUrl(String shortUrl) {
        this.shortUrls.add(shortUrl);
    }

    public void removeShortUrl(String shortUrl) {
        this.shortUrls.remove(shortUrl);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", shortUrls=" + shortUrls.size() +
                '}';
    }
}
