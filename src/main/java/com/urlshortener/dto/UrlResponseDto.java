package com.urlshortener.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * API response when a short URL is created or metadata is fetched.
 */
@Value
@Builder
public class UrlResponseDto {

	String shortUrl;

	String alias;

	String originalUrl;

	Instant createdAt;

	long accessCount;

	Instant expiresAt;

}
