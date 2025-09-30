package com.example.urlshortener.repository;

import com.example.urlshortener.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
    Optional<UrlMapping> findByShortKey(String shortKey);
    Optional<UrlMapping> findByLongUrl(String longUrl);
    boolean existsByShortKey(String shortKey);
}