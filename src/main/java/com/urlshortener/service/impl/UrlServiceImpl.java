package com.urlshortener.service.impl;

import com.urlshortener.config.AppProperties;
import com.urlshortener.config.CacheConfig;
import com.urlshortener.domain.RedirectTarget;
import com.urlshortener.domain.ShortUrl;
import com.urlshortener.dto.CreateUrlRequestDto;
import com.urlshortener.dto.UrlResponseDto;
import com.urlshortener.exception.AliasConflictException;
import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.mapper.ShortUrlMapper;
import com.urlshortener.repository.ShortUrlRepository;
import com.urlshortener.service.AccessCountService;
import com.urlshortener.service.AliasGenerator;
import com.urlshortener.service.AliasValidator;
import com.urlshortener.service.ExpiryResolver;
import com.urlshortener.service.UrlService;
import com.urlshortener.service.UrlValidator;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;

@Service
public class UrlServiceImpl implements UrlService {

	private static final int MAX_ALIAS_RETRIES = 3;

	private static final String NOT_FOUND_MESSAGE = "Short URL not found or expired";

	private final ShortUrlRepository shortUrlRepository;

	private final ShortUrlMapper shortUrlMapper;

	private final UrlValidator urlValidator;

	private final AliasValidator aliasValidator;

	private final AliasGenerator aliasGenerator;

	private final ExpiryResolver expiryResolver;

	private final AppProperties appProperties;

	private final CacheManager cacheManager;

	private final AccessCountService accessCountService;

	private final Clock clock;

	public UrlServiceImpl(
			ShortUrlRepository shortUrlRepository,
			ShortUrlMapper shortUrlMapper,
			UrlValidator urlValidator,
			AliasValidator aliasValidator,
			AliasGenerator aliasGenerator,
			ExpiryResolver expiryResolver,
			AppProperties appProperties,
			CacheManager cacheManager,
			AccessCountService accessCountService,
			Clock clock) {
		this.shortUrlRepository = shortUrlRepository;
		this.shortUrlMapper = shortUrlMapper;
		this.urlValidator = urlValidator;
		this.aliasValidator = aliasValidator;
		this.aliasGenerator = aliasGenerator;
		this.expiryResolver = expiryResolver;
		this.appProperties = appProperties;
		this.cacheManager = cacheManager;
		this.accessCountService = accessCountService;
		this.clock = clock;
	}

	@Override
	@Transactional
	public UrlResponseDto createUrl(CreateUrlRequestDto request) {
		urlValidator.validate(request.getOriginalUrl());

		Instant now = clock.instant();
		Instant expiresAt = expiryResolver.resolve(request.getExpiresAt(), now);

		if (StringUtils.hasText(request.getCustomAlias())) {
			String alias = request.getCustomAlias().trim();
			aliasValidator.validate(alias);
			return saveUrl(alias, request.getOriginalUrl(), now, expiresAt, true);
		}

		for (int attempt = 0; attempt < MAX_ALIAS_RETRIES; attempt++) {
			String alias = aliasGenerator.generate();
			try {
				return saveUrl(alias, request.getOriginalUrl(), now, expiresAt, false);
			}
			catch (DataIntegrityViolationException ex) {
				if (attempt == MAX_ALIAS_RETRIES - 1) {
					throw ex;
				}
			}
		}
		throw new IllegalStateException("Failed to generate unique alias");
	}

	@Override
	@Transactional(readOnly = true)
	public String resolveForRedirect(String alias) {
		Cache cache = cacheManager.getCache(CacheConfig.REDIRECT_CACHE);
		if (cache == null) {
			throw new IllegalStateException("redirectCache is not configured");
		}

		Instant now = clock.instant();
		RedirectTarget cached = cache.get(alias, RedirectTarget.class);
		if (cached != null) {
			if (!cached.expiresAt().isAfter(now)) {
				cache.evict(alias);
				throw new UrlNotFoundException(NOT_FOUND_MESSAGE);
			}
			accessCountService.incrementAccessCount(alias);
			return cached.originalUrl();
		}

		ShortUrl entity = findActiveShortUrl(alias);
		RedirectTarget target = new RedirectTarget(entity.getOriginalUrl(), entity.getExpiresAt());
		cache.put(alias, target);
		accessCountService.incrementAccessCount(alias);
		return target.originalUrl();
	}

	@Override
	@Transactional(readOnly = true)
	public UrlResponseDto getUrlMetadata(String alias) {
		ShortUrl entity = findActiveShortUrl(alias);
		return shortUrlMapper.toResponseDto(entity, appProperties.baseUrl());
	}

	private ShortUrl findActiveShortUrl(String alias) {
		ShortUrl entity = shortUrlRepository.findByAlias(alias)
				.orElseThrow(() -> new UrlNotFoundException(NOT_FOUND_MESSAGE));
		if (!entity.getExpiresAt().isAfter(clock.instant())) {
			throw new UrlNotFoundException(NOT_FOUND_MESSAGE);
		}
		return entity;
	}

	private UrlResponseDto saveUrl(String alias, String originalUrl, Instant now, Instant expiresAt, boolean customAlias) {
		ShortUrl entity = shortUrlMapper.toEntity(alias, originalUrl, now, expiresAt);
		try {
			ShortUrl saved = shortUrlRepository.saveAndFlush(entity);
			return shortUrlMapper.toResponseDto(saved, appProperties.baseUrl());
		}
		catch (DataIntegrityViolationException ex) {
			if (customAlias) {
				throw new AliasConflictException("Alias already exists: " + alias);
			}
			throw ex;
		}
	}

}
