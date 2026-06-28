package com.urlshortener.service.impl;

import com.urlshortener.config.TtlProperties;
import com.urlshortener.exception.InvalidExpiryException;
import com.urlshortener.service.ExpiryResolver;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ExpiryResolverImpl implements ExpiryResolver {

	private final TtlProperties ttlProperties;

	public ExpiryResolverImpl(TtlProperties ttlProperties) {
		this.ttlProperties = ttlProperties;
	}

	@Override
	public Instant resolve(Instant requestedExpiresAt, Instant now) {
		if (requestedExpiresAt == null) {
			return now.plusSeconds(ttlProperties.defaultSeconds());
		}
		if (!requestedExpiresAt.isAfter(now)) {
			throw new InvalidExpiryException("Expiry must be in the future");
		}
		return requestedExpiresAt;
	}

}
