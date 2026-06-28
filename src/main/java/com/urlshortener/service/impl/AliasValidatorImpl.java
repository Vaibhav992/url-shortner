package com.urlshortener.service.impl;

import com.urlshortener.exception.InvalidAliasException;
import com.urlshortener.service.AliasValidator;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.regex.Pattern;

@Service
public class AliasValidatorImpl implements AliasValidator {

	private static final Pattern ALIAS_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,64}$");

	private static final Set<String> RESERVED = Set.of(
			"api", "health", "actuator", "swagger", "swagger-ui", "v1", "v3", "docs");

	@Override
	public void validate(String alias) {
		if (alias == null || alias.isBlank()) {
			throw new InvalidAliasException("Custom alias must not be blank");
		}
		if (!ALIAS_PATTERN.matcher(alias).matches()) {
			throw new InvalidAliasException("Custom alias must be 3–64 characters and contain only letters, digits, hyphens, or underscores");
		}
		if (RESERVED.contains(alias.toLowerCase())) {
			throw new InvalidAliasException("Custom alias is reserved");
		}
	}

}
