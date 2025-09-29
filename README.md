# URL Shortener

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

---

![Banner](![](/Users/admin/Desktop/JOB/urlshortener/src/main/assets/banner.png)./src/main/assets/banner.png)


*A simple URL Shortener service built with Spring Boot.*

## Features (to be added later)

* Convert long URLs into short codes
* Store and retrieve mappings from database
* Redirect short links to original URLs

## Tech Stack

* Java 17
* Spring Boot 3
* Spring Web
* Spring Data JPA
* H2 Database (for development, later Postgres/MySQL)

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

**Successful response (200 OK)**

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

In `src/main/resources/application.properties` set the base host for generated short URLs:

```
app.host-base=http://localhost:8080
```
