package com.example.urlshortener.controller;

import com.example.urlshortener.service.UrlShorteningService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class RedirectController {

    private final UrlShorteningService service;

    public RedirectController(UrlShorteningService service) {
        this.service = service;
    }

    @GetMapping("/{shortKey}")
    public ResponseEntity<Void> redirect(@PathVariable String shortKey) {
        return service.findByShortKey(shortKey)
                .map(mapping -> ResponseEntity.status(302)
                        .header(HttpHeaders.LOCATION, mapping.getLongUrl())
                        .<Void>build())
                .orElseGet(() -> ResponseEntity.notFound().<Void>build());
    }
}
