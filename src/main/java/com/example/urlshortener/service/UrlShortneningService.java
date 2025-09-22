package com.example.urlshortener.service;

import com.example.urlshortener.dto.ShortenResponse;
import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.repository.UrlMappingRepository;
import com.example.urlshortener.util.ShortCodeGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class UrlShorteningService {

    private final UrlMappingRepository repo;
    private final String hostBase;
    private static final int SHORT_KEY_LENGTH = 6;
    private static final int MAX_TRIES = 5;

    public UrlShorteningService(UrlMappingRepository repo,
                                @Value("${app.host-base:http://localhost:8080}") String hostBase) {
        this.repo = repo;
        this.hostBase = hostBase.endsWith("/") ? hostBase : hostBase + "/";
    }

    @Transactional
    public ShortenResponse shortenUrl(String url) {
        // url is the incoming long URL (client field name). We persist it in entity's longUrl.
        Optional<UrlMapping> existing = repo.findByLongUrl(url);
        if (existing.isPresent()) {
            UrlMapping em = existing.get();
            return new ShortenResponse(em.getShortKey(), hostBase + em.getShortKey(), em.getLongUrl());
        }

        // generate unique key (retry on collision)
        String key = null;
        int tries = 0;
        while (tries < MAX_TRIES && key == null) {
            String candidate = ShortCodeGenerator.generate(SHORT_KEY_LENGTH);
            if (!repo.existsByShortKey(candidate)) key = candidate;
            else tries++;
        }

        if (key == null) {
            // fallback deterministic key (very unlikely)
            key = Long.toString(Instant.now().toEpochMilli(), 36);
        }

        UrlMapping mapping = new UrlMapping();
        // adapt if your entity field names differ
        mapping.setLongUrl(url);
        mapping.setShortKey(key);

        // createdAt: if your entity uses Instant, keep this; if LocalDateTime, convert
        try {
            mapping.setCreatedAt(Instant.now());
        } catch (NoSuchMethodError | Exception ignored) {
            // If your UrlMapping uses a different createdAt type, set it accordingly.
            // e.g., mapping.setCreatedAt(LocalDateTime.now());
        }

        repo.save(mapping);

        return new ShortenResponse(key, hostBase + key, url);
    }

    public Optional<String> resolveLongUrl(String shortKey) {
        return repo.findByShortKey(shortKey).map(UrlMapping::getLongUrl);
    }
}
