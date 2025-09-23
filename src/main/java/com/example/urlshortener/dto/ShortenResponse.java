package com.example.urlshortener.dto;

public class ShortenResponse {
    private String shortKey;
    private String shortUrl;

    public ShortenResponse() {}

    public ShortenResponse(String shortKey, String shortUrl) {
        this.shortKey = shortKey;
        this.shortUrl = shortUrl;
    }

    public String getShortKey() {
        return shortKey;
    }

    public void setShortKey(String shortKey) {
        this.shortKey = shortKey;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }
}
