# URL Shortener

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

---

![Banner](https://raw.githubusercontent.com/aseemjan/url-shortener/main/src/main/assets/banner.png)



*A simple URL Shortener service built with Spring Boot.*

This project implements a clean backend architecture for shortening URLs.  
It follows REST best practices (`201 Created`, `Location` header),  
handles collision-free short-code generation,  
and uses JPA with Flyway for database migrations.

## Key Features
* Generate unique short codes for long URLs with retry-based collision handling
* Redirect short links using 302 responses with `Location` header
* REST API with 201 Created responses and consistent JSON DTOs
* Flyway-managed MySQL schema for persistence
* Centralized exception handling for validation and not-found errors
* Comprehensive unit and integration tests (JUnit + MockMvc)

## Tech Stack

* Java 17
* Spring Boot 3
* Spring Web
* Spring Data JPA
* MySQL (production), H2 (for local/testing)
* Flyway for schema versioning
* JUnit 5 + Mockito for testing

## How to Run

```bash
mvn spring-boot:run
```

## API

### Create short URL

**Endpoint**

```
POST /api/v1/shorten
Content-Type: application/json
```

**Request body**

```json
{
  "url": "https://example.com/very/long/path"
}
```

**Successful response (201 CREATED)**

```json
{
  "shortKey": "a1b2C3",
  "shortUrl": "http://localhost:8080/a1b2C3",
  "longUrl": "https://example.com/very/long/path"
}
```

**Field notes**

* `shortKey` — the generated identifier stored in DB (e.g., `a1b2C3`).
* `shortUrl` — the user-facing URL you can share (base from `app.host-base` + `shortKey`).
* `longUrl` — the original long URL that was shortened.

**cURL example**

```bash
curl -s -X POST http://localhost:8080/api/v1/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://news.ycombinator.com/"}' | jq
```

---

### Redirect (follow short link)

## Design Highlights
* **Idempotent shortening** — same long URL always returns the same short key.
* **Collision resolution** — automatic retries with salted codes and DB uniqueness checks.
* **Resilience** — DataIntegrityViolation handling for concurrent requests.
* **Extensible** — future-ready for Redis caching and analytics.


**Endpoint**

```
GET /{shortKey}
```

If the `shortKey` exists, the server returns a `302 Found` response with a `Location` header pointing to the `longUrl`.

**cURL example**

```bash
curl -i http://localhost:8080/a1b2C3
# Expect HTTP/1.1 302 Found and a Location: header with the original URL
```

---

## Config

## Future Improvements
* Redis caching for faster redirects
* Docker Compose for local setup (app + MySQL)
* Click analytics and metrics with Micrometer
* Horizontal sharding for scalability


In `src/main/resources/application.properties` set the base host for generated short URLs:

```
app.host-base=http://localhost:8080
```
