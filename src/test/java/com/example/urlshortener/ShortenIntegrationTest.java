package com.example.urlshortener;

import com.example.urlshortener.dto.ShortenRequest;
import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.repository.UrlMappingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class ShortenIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.33")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerMySqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "5");
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UrlMappingRepository repository;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void shortenAndPersist_shouldReturn201_andStoreRow() throws Exception {
        String longUrl = "https://integration.example/path";

        ShortenRequest req = new ShortenRequest();
        req.setUrl(longUrl);

        String json = objectMapper.writeValueAsString(req);

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        assertThat(repository.findByLongUrl(longUrl)).isPresent();

        UrlMapping mapping = repository.findByLongUrl(longUrl).get();
        assertThat(mapping.getShortKey()).isNotBlank();
        assertThat(mapping.getLongUrl()).isEqualTo(longUrl);
    }
}
