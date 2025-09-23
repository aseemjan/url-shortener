package com.example.urlshortener.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.urlshortener.dto.ShortenRequest;
import com.example.urlshortener.dto.ShortenResponse;
import com.example.urlshortener.service.UrlShorteningService;

@RestController
@RequestMapping("/api")
public class UrlController {

    private final UrlShorteningService service;

    public UrlController(UrlShorteningService service) {
        this.service = service;
    }

    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> shortenUrl(@Valid @RequestBody ShortenRequest req) {
        ShortenResponse resp = service.shortenUrl(req.getUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/{shortKey}")
    public ResponseEntity<Void> redirectToLongUrl(@PathVariable String shortKey) {
        return service.findByKey(shortKey)
                .map(mapping -> ResponseEntity.status(HttpStatus.FOUND)
                        .header(HttpHeaders.LOCATION, mapping.getLongUrl())
                        .<Void>build())                       // <- force Void here
                .orElseGet(() -> ResponseEntity.notFound().<Void>build()); // <- and here
    }

}
