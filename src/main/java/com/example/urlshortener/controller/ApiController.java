package com.example.urlshortener.controller;

import com.example.urlshortener.dto.ShortenRequest;
import com.example.urlshortener.dto.ShortenResponse;
import com.example.urlshortener.service.UrlShorteningService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ApiController {

    private final UrlShorteningService service;

    public ApiController(UrlShorteningService service) {
        this.service = service;
    }

    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest req) {
        ShortenResponse resp = service.shortenUrl(req.getUrl());

        java.net.URI location;
        if(resp.getShortUrl() != null && resp.getShortUrl().isBlank()){
            location = java.net.URI.create(resp.getShortUrl());
        }else{
            location = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                    .fromCurrentContextPath().pathSegment(resp.getShortKey())
                   .buildAndExpand(resp.getShortKey()).toUri();
        }
        // 201 Created with Location header is nice, but 200 OK is fine too.
        return ResponseEntity.created(location).body(resp);
    }
}
