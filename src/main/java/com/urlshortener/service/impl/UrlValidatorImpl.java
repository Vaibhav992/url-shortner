package com.urlshortener.service.impl;

import com.urlshortener.exception.InvalidUrlException;
import com.urlshortener.service.UrlValidator;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;

@Service
public class UrlValidatorImpl implements UrlValidator {

	private static final int MAX_URL_LENGTH = 2048;

	@Override
	public void validate(String url) {
		if (url == null || url.isBlank()) {
			throw new InvalidUrlException("URL must not be blank");
		}
		if (url.length() > MAX_URL_LENGTH) {
			throw new InvalidUrlException("URL exceeds maximum length of " + MAX_URL_LENGTH);
		}
		String lower = url.toLowerCase();
		if (lower.startsWith("javascript:") || lower.startsWith("data:") || lower.startsWith("file:")) {
			throw new InvalidUrlException("URL scheme is not allowed");
		}
		try {
			URI uri = new URI(url);
			String scheme = uri.getScheme();
			if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
				throw new InvalidUrlException("Only HTTP and HTTPS URLs are allowed");
			}
			if (uri.getHost() == null || uri.getHost().isBlank()) {
				throw new InvalidUrlException("URL must include a valid host");
			}
		}
		catch (URISyntaxException ex) {
			throw new InvalidUrlException("URL is malformed");
		}
	}

}
