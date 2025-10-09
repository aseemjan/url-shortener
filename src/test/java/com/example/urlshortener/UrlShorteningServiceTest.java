package com.example.urlshortener;

import com.example.urlshortener.dto.ShortenResponse;
import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.repository.UrlMappingRepository;
import com.example.urlshortener.service.UrlShorteningService;
import com.example.urlshortener.util.ShortCodeGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShorteningServiceTest {

    @Mock UrlMappingRepository repository;
    @Mock ShortCodeGenerator generator;

    @InjectMocks UrlShorteningService service;

    @Captor ArgumentCaptor<UrlMapping> mappingCaptor;

    @Test
    void shortenUrl_newLongUrl_generatesCode_saves_andReturnsDTO() {
        String longUrl = "https://example.com/new";
        String code = "abc1234";

        // repo doesn’t know this URL yet
        when(repository.findByLongUrl(longUrl)).thenReturn(Optional.empty());
        // generator proposes a code
        when(generator.generate(longUrl)).thenReturn(code);

        // repo returns the entity it would persist
        UrlMapping persisted = new UrlMapping();
        persisted.setId(1L);
        persisted.setLongUrl(longUrl);
        persisted.setShortKey(code);

        when(repository.save(any(UrlMapping.class))).thenReturn(persisted);

        // service returns DTO
        ShortenResponse result = service.shortenUrl(longUrl);

        assertNotNull(result);
        assertEquals(longUrl, result.getLongUrl());
        assertEquals(code, result.getShortKey());

        // verify interactions and what was saved
        verify(repository).findByLongUrl(longUrl);
        verify(generator).generate(longUrl);
        verify(repository).save(mappingCaptor.capture());

        UrlMapping toSave = mappingCaptor.getValue();
        assertEquals(longUrl, toSave.getLongUrl());
        assertEquals(code, toSave.getShortKey());
    }

    @Test
    void shortenUrl_existingLongUrl_returnsExistingDTO_withoutSaveOrGenerate() {
        String longUrl = "https://example.com/already";

        UrlMapping existing = new UrlMapping();
        existing.setId(2L);
        existing.setLongUrl(longUrl);
        existing.setShortKey("exst001");

        when(repository.findByLongUrl(longUrl)).thenReturn(Optional.of(existing));

        ShortenResponse result = service.shortenUrl(longUrl);

        assertNotNull(result);
        assertEquals(longUrl, result.getLongUrl());
        assertEquals("exst001", result.getShortKey());

        verify(repository).findByLongUrl(longUrl);
        verify(generator, never()).generate(anyString());
        verify(repository, never()).save(any());
    }

    @Test
    void expandUrl_knownShortKey_returnsLongUrlInsideOptional() {
        String shortKey = "abc1234";
        String longUrl = "https://example.com/known";

        UrlMapping mapping = new UrlMapping();
        mapping.setId(3L);
        mapping.setShortKey(shortKey);
        mapping.setLongUrl(longUrl);

        when(repository.findByShortKey(shortKey)).thenReturn(Optional.of(mapping));

        Optional<String> result = service.expandUrl(shortKey);

        assertTrue(result.isPresent());
        assertEquals(longUrl, result.get());
        verify(repository).findByShortKey(shortKey);
    }

    @Test
    void expandUrl_unknownShortKey_returnsEmptyOptional() {
        when(repository.findByShortKey("missing")).thenReturn(Optional.empty());

        Optional<String> result = service.expandUrl("missing");

        assertFalse(result.isPresent());
        verify(repository).findByShortKey("missing");
    }

    @Test
    void shortenUrl_whenGeneratedShortKeyCollides_retriesUntilUnique() {
        String longUrl = "https://example.com/collision";
        String firstCode = "dup001";
        String secondCode = "uniq002";

        // URL not already present
        when(repository.findByLongUrl(longUrl)).thenReturn(Optional.empty());

        // generator first produces a duplicate, then a unique code
        when(generator.generate(longUrl))
                .thenReturn(firstCode)
                .thenReturn(secondCode);

        // repository reports the first code already exists, second one is new
        when(repository.existsByShortKey(firstCode)).thenReturn(true);
        when(repository.existsByShortKey(secondCode)).thenReturn(false);

        UrlMapping persisted = new UrlMapping();
        persisted.setId(10L);
        persisted.setLongUrl(longUrl);
        persisted.setShortKey(secondCode);

        when(repository.save(any(UrlMapping.class))).thenReturn(persisted);

        ShortenResponse result = service.shortenUrl(longUrl);

        assertNotNull(result);
        assertEquals(longUrl, result.getLongUrl());
        assertEquals(secondCode, result.getShortKey());

        // Verify generator called twice (collision + retry)
        verify(generator, times(2)).generate(longUrl);

        // Verify repository existence check for both codes
        verify(repository).existsByShortKey(firstCode);
        verify(repository).existsByShortKey(secondCode);

        // Verify only one save — for the unique code
        verify(repository, times(1)).save(any(UrlMapping.class));
    }

    @Test
    void shortenUrl_whenAllGeneratedCodesCollide_throwsExceptionAfterMaxRetries() {
        String longUrl = "https://example.com/exhausted";
        when(repository.findByLongUrl(longUrl)).thenReturn(Optional.empty());

        // Suppose the service allows up to 3 retries (you can adjust to match your actual implementation)
        when(generator.generate(longUrl))
                .thenReturn("dup1")
                .thenReturn("dup2")
                .thenReturn("dup3");

        // All generated codes are taken
        when(repository.existsByShortKey(anyString())).thenReturn(true);

        // No mapping ever saved
        assertThrows(RuntimeException.class, () -> service.shortenUrl(longUrl));

        verify(generator, atLeast(3)).generate(longUrl);
        verify(repository, never()).save(any(UrlMapping.class));
    }

}
