package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CreateUrlRequestDto {

	@NotBlank(message = "originalUrl is required")
	private String originalUrl;

	private Instant expiresAt;

	private String customAlias;

}
