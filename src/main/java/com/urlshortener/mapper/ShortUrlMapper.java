package com.urlshortener.mapper;

import com.urlshortener.domain.ShortUrl;
import com.urlshortener.dto.UrlResponseDto;
import org.springframework.stereotype.Component;

import java.time.Instant;


@Component
public class ShortUrlMapper {

	public UrlResponseDto toResponseDto(ShortUrl entity, String baseUrl) {
		String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
		return UrlResponseDto.builder()
				.shortUrl(normalizedBase + "/" + entity.getAlias())
				.alias(entity.getAlias())
				.originalUrl(entity.getOriginalUrl())
				.createdAt(entity.getCreatedAt())
				.accessCount(entity.getAccessCount())
				.expiresAt(entity.getExpiresAt())
				.build();
	}

	public ShortUrl toEntity(String alias, String originalUrl, Instant createdAt, Instant expiresAt) {
		return ShortUrl.builder()
				.alias(alias)
				.originalUrl(originalUrl)
				.accessCount(0L)
				.createdAt(createdAt)
				.expiresAt(expiresAt)
				.build();
	}

}
