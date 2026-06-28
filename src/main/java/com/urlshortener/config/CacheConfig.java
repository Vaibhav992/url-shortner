package com.urlshortener.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

	public static final String REDIRECT_CACHE = "redirectCache";

	@Bean
	CacheManager cacheManager(AppProperties appProperties) {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager(REDIRECT_CACHE);
		cacheManager.setCaffeine(Caffeine.newBuilder()
				.maximumSize(appProperties.cache().maxSize())
				.expireAfterWrite(appProperties.cache().expireMinutes(), TimeUnit.MINUTES));
		return cacheManager;
	}

}
