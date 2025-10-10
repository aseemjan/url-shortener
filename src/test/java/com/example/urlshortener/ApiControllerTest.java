package com.example.urlshortener;

import com.example.urlshortener.controller.ApiController;
import com.example.urlshortener.dto.ShortenResponse;
import com.example.urlshortener.service.UrlShorteningService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ApiController.class)
class ApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UrlShorteningService service;

    @Test
    void postShorten_returns201AndLocation() throws Exception {
        String longUrl = "https://example.com/new";
        String shortKey = "abc1234";

        ShortenResponse resp = new ShortenResponse();
        resp.setLongUrl(longUrl);
        resp.setShortKey(shortKey);
        resp.setShortUrl("http://localhost:8080/" + shortKey);

        when(service.shortenUrl(longUrl)).thenReturn(resp);

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"" + longUrl + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/abc1234"))
                .andExpect(jsonPath("$.shortKey").value(shortKey))
                .andExpect(jsonPath("$.longUrl").value(longUrl));

        verify(service).shortenUrl(longUrl);
    }
}
