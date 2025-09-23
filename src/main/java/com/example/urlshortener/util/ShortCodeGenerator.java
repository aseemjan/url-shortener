package com.example.urlshortener.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public class ShortCodeGenerator {

    // simple deterministic generator using sha-1 + base64 (then take first 7 chars)
    public String generate(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] h = md.digest(input.getBytes(StandardCharsets.UTF_8));
            String b64 = Base64.getUrlEncoder().withoutPadding().encodeToString(h);
            return b64.substring(0, Math.min(7, b64.length()));
        } catch (Exception e) {
            // fallback
            return Long.toString(System.currentTimeMillis(), 36);
        }
    }
}
