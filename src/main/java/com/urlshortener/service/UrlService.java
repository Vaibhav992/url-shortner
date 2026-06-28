package com.urlshortener.service;

import com.urlshortener.dto.CreateUrlRequestDto;
import com.urlshortener.dto.UrlResponseDto;

public interface UrlService {

	UrlResponseDto createUrl(CreateUrlRequestDto request);

	String resolveForRedirect(String alias);

	UrlResponseDto getUrlMetadata(String alias);

}
