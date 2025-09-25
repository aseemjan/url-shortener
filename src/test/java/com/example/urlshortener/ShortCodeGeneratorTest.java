package com.example.urlshortener;

import com.example.urlshortener.util.ShortCodeGenerator;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ShortCodeGeneratorTest {

    // instantiate the real generator directly (no Spring context required)
    private final ShortCodeGenerator generator = new ShortCodeGenerator();

    @Test
    void generate_shouldReturnNonNullNonEmptyCode() {
        String code = generator.generate("https://example.com/some/long/url");
        assertNotNull(code, "Generated code should not be null");
        assertFalse(code.isEmpty(), "Generated code should not be empty");
    }

    @Test
    void generate_shouldReturnCodeOfExpectedLength() {
        String code = generator.generate("https://example.com/another/url");
        int expectedMin = 4;
        int expectedMax = 12;
        assertTrue(code.length() >= expectedMin && code.length() <= expectedMax,
                () -> "Code length should be within reasonable bounds: " + code.length());
    }

    @Test
    void generate_shouldUseAllowedCharacters_onlyAlnumOrUrlSafe() {
        String code = generator.generate("https://example.com");
        assertTrue(code.matches("^[A-Za-z0-9_-]+$"),
                "Short code must contain only URL-safe characters (alphanumeric, '-' or '_')");
    }

    @Test
    void generate_shouldProduceUniqueCodes_forManyInputs() {
        int iterations = 2000;
        Set<String> seen = new HashSet<>(iterations);
        for (int i = 0; i < iterations; i++) {
            String url = "https://example.com/resource/" + i;
            String code = generator.generate(url);
            assertNotNull(code);
            assertFalse(code.isEmpty());
            assertTrue(seen.add(code), "Collision detected for code: " + code + " at iteration " + i);
        }
    }
}
