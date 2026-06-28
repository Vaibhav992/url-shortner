package com.urlshortener.service.impl;

import com.urlshortener.config.AsyncConfig;
import com.urlshortener.repository.ShortUrlRepository;
import com.urlshortener.service.AccessCountService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccessCountServiceImpl implements AccessCountService {

	private final ShortUrlRepository shortUrlRepository;

	public AccessCountServiceImpl(ShortUrlRepository shortUrlRepository) {
		this.shortUrlRepository = shortUrlRepository;
	}

	@Async(AsyncConfig.ACCESS_COUNT_EXECUTOR)
	@Transactional
	@Override
	public void incrementAccessCount(String alias) {
		shortUrlRepository.incrementAccessCount(alias);
	}

}
