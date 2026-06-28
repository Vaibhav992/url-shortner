package com.urlshortener.service.impl;

import com.urlshortener.config.AppProperties;
import com.urlshortener.service.AliasGenerator;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class AliasGeneratorImpl implements AliasGenerator {

	private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	private final AppProperties appProperties;

	private final SecureRandom random = new SecureRandom();

	public AliasGeneratorImpl(AppProperties appProperties) {
		this.appProperties = appProperties;
	}

	@Override
	public String generate() {
		int length = appProperties.alias().autoLength();
		StringBuilder alias = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			alias.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
		}
		return alias.toString();
	}

}
