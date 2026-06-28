package com.urlshortener.domain;

import java.time.Instant;

public record RedirectTarget(String originalUrl, Instant expiresAt) {

}
