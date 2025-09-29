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
}
