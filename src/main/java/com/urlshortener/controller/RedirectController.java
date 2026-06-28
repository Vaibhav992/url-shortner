package com.urlshortener.controller;

import com.urlshortener.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Set;

@RestController
public class RedirectController {

	private static final Set<String> EXCLUDED_ALIASES = Set.of(
			"api", "health", "actuator", "swagger", "swagger-ui", "v1", "v3", "docs");

	private final UrlService urlService;

	public RedirectController(UrlService urlService) {
		this.urlService = urlService;
	}

	@GetMapping("/{alias}")
	public ResponseEntity<Void> redirect(@PathVariable String alias) {
		if (EXCLUDED_ALIASES.contains(alias.toLowerCase())) {
			return ResponseEntity.notFound().build();
		}
		String originalUrl = urlService.resolveForRedirect(alias);
		return ResponseEntity.status(HttpStatus.FOUND)
				.location(URI.create(originalUrl))
				.build();
	}

}
