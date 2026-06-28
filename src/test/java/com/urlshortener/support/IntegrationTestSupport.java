package com.urlshortener.support;

import org.springframework.test.context.DynamicPropertyRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class IntegrationTestSupport {

	private static final Path ENV_FILE = Path.of(".env");

	private IntegrationTestSupport() {
	}

	public static void registerPostgresProperties(DynamicPropertyRegistry registry) {
		loadDotEnvIfPresent();
		registry.add("spring.datasource.url", () -> required("DATABASE_URL"));
		registry.add("spring.datasource.username", () -> required("DATABASE_USERNAME"));
		registry.add("spring.datasource.password", () -> required("DATABASE_PASSWORD"));
		registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
	}

	private static void loadDotEnvIfPresent() {
		if (!Files.exists(ENV_FILE)) {
			return;
		}
		try {
			List<String> lines = Files.readAllLines(ENV_FILE);
			for (String line : lines) {
				parseLine(line);
			}
		}
		catch (IOException ex) {
			throw new IllegalStateException("Failed to read .env at " + ENV_FILE.toAbsolutePath(), ex);
		}
	}

	private static void parseLine(String line) {
		String trimmed = line.trim();
		if (trimmed.isEmpty() || trimmed.startsWith("#")) {
			return;
		}
		int separator = trimmed.indexOf('=');
		if (separator <= 0) {
			return;
		}
		String key = trimmed.substring(0, separator).trim();
		String value = trimmed.substring(separator + 1).trim();
		if (System.getenv(key) == null && System.getProperty(key) == null) {
			System.setProperty(key, value);
		}
	}

	private static String required(String key) {
		String value = System.getenv(key);
		if (value == null || value.isBlank()) {
			value = System.getProperty(key);
		}
		if (value == null || value.isBlank()) {
			throw new IllegalStateException(
					"Missing " + key + " for integration tests. Copy .env.example to .env and configure Neon/Postgres.");
		}
		return value;
	}

}
