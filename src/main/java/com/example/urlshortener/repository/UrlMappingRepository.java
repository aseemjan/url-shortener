package com.example.urlshortener.repository;

import com.example.urlshortener.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.urlshortener.model.UrlMapping;
import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
    Optional<UrlMapping> findByKey(String key);
    Optional<UrlMapping> findByShortKey(String shortKey);
    Optional<UrlMapping> findByLongUrl(String longUrl);
    boolean existsByShortKey(String shortKey);
}