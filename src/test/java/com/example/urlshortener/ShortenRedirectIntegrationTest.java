package com.example.urlshortener;

import com.example.urlshortener.dto.ShortenRequest;
import com.example.urlshortener.controller.RedirectController;
import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.repository.UrlMappingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ShortenRedirectIntegrationTest {

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

        // Forcing the Mysql Driver (Prevents H2)
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");

        registry.add("spring.flyway.enabled", () -> "true");

        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQL8Dialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");

        /*
        //Flyway Should Use The Same DB
        registry.add("spring.flyway.url", mysql::getJdbcUrl);
        registry.add("spring.flyway.user", mysql::getUsername);
        registry.add("spring.flyway.password", mysql::getPassword);
         */


    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UrlMappingRepository repository;

    @Test
    void shorten_then_getKey_redirectsToLongUrl() throws Exception {
        String longUrl = "https://redirect.example/path";

        // 1) create shorten mapping
        ShortenRequest req = new ShortenRequest();
        req.setUrl(longUrl);
        String requestJson = objectMapper.writeValueAsString(req);

        String shortenRespJson = mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // parse shortKey from response
        JsonNode node = objectMapper.readTree(shortenRespJson);
        String shortKey = node.path("shortKey").asText();
        assertThat(shortKey).isNotEmpty();

        // confirm DB has mapping
        UrlMapping mapping = repository.findByShortKey(shortKey).orElseThrow();
        assertThat(mapping.getLongUrl()).isEqualTo(longUrl);

        // 2) call redirect endpoint (GET /{key})

        mockMvc.perform(get("/" + shortKey))
                .andExpect(status().isFound()) // 302
                .andExpect(result -> {
                    String loc = result.getResponse().getHeader("Location");

                    assertThat(loc).isEqualTo(longUrl);
                });
    }
}
