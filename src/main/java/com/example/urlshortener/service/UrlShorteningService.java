package com.example.urlshortener.service;

import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.repository.UrlMappingRepository;
import com.example.urlshortener.util.ShortCodeGenerator;
import com.example.urlshortener.dto.ShortenResponse;

@Service
public class UrlShorteningService {

    private final UrlMappingRepository repo;
    private final ShortCodeGenerator generator;

    // optional: base url for constructing the short URL
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public UrlShorteningService(UrlMappingRepository repo, ShortCodeGenerator generator) {
        this.repo = repo;
        this.generator = generator;
    }

    /**
     * Create (or return) a short mapping for the provided longUrl.
     * Returns a DTO containing the shortKey and the full short URL.
     */
    public ShortenResponse shortenUrl(String longUrl) {
        // generate deterministic or random short key
        String key = generator.generate(longUrl);

        // create and save entity
        UrlMapping mapping = new UrlMapping();
        mapping.setShortKey(key);
        mapping.setLongUrl(longUrl);
        mapping.setCreatedAt(Instant.now().toEpochMilli()); // matches model Long

        UrlMapping saved = repo.save(mapping);

        String shortUrl = baseUrl.endsWith("/") ? baseUrl + key : baseUrl + "/" + key;
        return new ShortenResponse(saved.getShortKey(), shortUrl);
    }

    /**
     * Lookup by shortKey.
     */
    public Optional<UrlMapping> findByKey(String shortKey) {
        return repo.findByShortKey(shortKey);
    }
}
