package com.urlshortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ttl")
public record TtlProperties(long defaultSeconds) {

	public TtlProperties {
		if (defaultSeconds <= 0) {
			throw new IllegalArgumentException("app.ttl.default-seconds must be positive");
		}
	}

}
