package com.example.urlshortener.service;

import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;


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
    private String baseUrl = "http://localhost:8080";

    @Value("${shortener.max-retries:3}")
    private int maxRetries = 3;


    public UrlShorteningService(UrlMappingRepository repo, ShortCodeGenerator generator) {
        this.repo = repo;
        this.generator = generator;
    }

    /**
     * Create (or return) a short mapping for the provided longUrl.
     * Returns a DTO containing the shortKey and the full short URL.
     */
    public ShortenResponse shortenUrl(String longUrl) {
        // 0) Normalize base URL (Option-B: tolerate null/blank)
        final String bz = (baseUrl == null || baseUrl.isBlank()) ? "" : baseUrl.trim();
        final boolean hasBase = !bz.isBlank();

        // 1) Reuse existing mapping if present
        Optional<UrlMapping> existing = repo.findByLongUrl(longUrl);
        if (existing.isPresent()) {
            UrlMapping m = existing.get();
            String shortUrl = hasBase
                    ? (bz.endsWith("/") ? bz + m.getShortKey() : bz + "/" + m.getShortKey())
                    : m.getShortKey(); // no base => just key
            // Build DTO with longUrl + shortKey (+ shortUrl)
            ShortenResponse dto = new ShortenResponse();
            dto.setLongUrl(m.getLongUrl());
            dto.setShortKey(m.getShortKey());
            dto.setShortUrl(shortUrl);
            return dto;
        }

        // 2) Try generate + save with collision handling and retries
        for (int attempt = 1; attempt <= Math.max(1, maxRetries); attempt++) {
            String key = generator.generate(longUrl);

            // quick pre-check to avoid unnecessary save if already exists
            try {
                if (repo.existsByShortKey(key)) {
                    // collision detected — try next attempt
                    continue;
                }
            } catch (UnsupportedOperationException ignored) {
                // in case repo does not provide existsByShortKey, we'll rely on DB constraint below
            }

            UrlMapping mapping = new UrlMapping();
            mapping.setShortKey(key);
            mapping.setLongUrl(longUrl);
            mapping.setCreatedAt(Instant.now().toEpochMilli()); // model uses Long

            try {
                UrlMapping saved = repo.save(mapping);

                String shortUrl = hasBase
                        ? (bz.endsWith("/") ? bz + key : bz + "/" + key)
                        : key;

                ShortenResponse dto = new ShortenResponse();
                dto.setLongUrl(saved.getLongUrl());
                dto.setShortKey(saved.getShortKey());
                dto.setShortUrl(shortUrl);
                return dto;
            } catch (DataIntegrityViolationException dive) {
                // likely a unique constraint violation caused by concurrent insert -> retry
                // fall through to next attempt
            }
        }

       // 3) Exhausted retries -> escalate
        throw new RuntimeException("Failed to generate unique short key after " + maxRetries + " attempts");
    }

    public Optional<String> expandUrl(String shortCode) {
        if (shortCode == null) return Optional.empty();
        String code = shortCode.trim();
        if (code.isEmpty()) return Optional.empty();

        return repo.findByShortKey(code).map(UrlMapping::getLongUrl);
    }


    public Optional<UrlMapping> findByShortKey(String shortKey) {
        return repo.findByShortKey(shortKey);
    }
}
