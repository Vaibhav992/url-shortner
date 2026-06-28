package com.urlshortener.service;

import java.time.Instant;

public interface ExpiryResolver {

	Instant resolve(Instant requestedExpiresAt, Instant now);

}
