package com.urlshortener.controller;

import com.urlshortener.dto.CreateUrlRequestDto;
import com.urlshortener.dto.UrlResponseDto;
import com.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/urls")
public class UrlController {

	private final UrlService urlService;

	public UrlController(UrlService urlService) {
		this.urlService = urlService;
	}

	@PostMapping
	public ResponseEntity<UrlResponseDto> createUrl(@Valid @RequestBody CreateUrlRequestDto request) {
		UrlResponseDto response = urlService.createUrl(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{alias}")
	public UrlResponseDto getUrlMetadata(@PathVariable String alias) {
		return urlService.getUrlMetadata(alias);
	}

}
