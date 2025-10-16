package com.example.urlshortener.model;

import jakarta.persistence.*;

@Entity
@Table(name = "url_mappings")
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    // Use a non-reserved, descriptive column name (shortKey)
    @Column(name = "short_key", nullable = false, unique = true)
    private String shortKey;

    @Column(name = "long_url", nullable = false, length = 2048)
    private String longUrl;

    // store epoch millis for simplicity
    @Column(name = "created_at", nullable = false)
    public Long createdAt;

    // --- getters & setters ---
    public void setId(Long id) {
        this.id = id;
    }

    public String getShortKey() {
        return shortKey;
    }

    public void setShortKey(String shortKey) {
        this.shortKey = shortKey;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
