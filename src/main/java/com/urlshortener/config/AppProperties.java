package com.urlshortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
		String baseUrl,
		Alias alias,
		Cache cache
) {

	public AppProperties {
		if (baseUrl == null || baseUrl.isBlank()) {
			throw new IllegalArgumentException("app.base-url must not be blank");
		}
		if (alias == null) {
			throw new IllegalArgumentException("app.alias must be configured");
		}
		if (cache == null) {
			throw new IllegalArgumentException("app.cache must be configured");
		}
	}

	public record Alias(int autoLength) {

		public Alias {
			if (autoLength < 3 || autoLength > 32) {
				throw new IllegalArgumentException("app.alias.auto-length must be between 3 and 32");
			}
		}

	}

	public record Cache(int maxSize, int expireMinutes) {

		public Cache {
			if (maxSize <= 0) {
				throw new IllegalArgumentException("app.cache.max-size must be positive");
			}
			if (expireMinutes <= 0) {
				throw new IllegalArgumentException("app.cache.expire-minutes must be positive");
			}
		}

	}

}
