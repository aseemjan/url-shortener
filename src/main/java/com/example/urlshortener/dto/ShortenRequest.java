package com.example.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ShortenRequest {
    @NotBlank(message = "url is required")
    @Pattern(regexp = "^(https?://).+", message = "url must start with http:// or https://")
    private String url;

    public ShortenRequest() {}
    public ShortenRequest(String url) { this.url = url; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
