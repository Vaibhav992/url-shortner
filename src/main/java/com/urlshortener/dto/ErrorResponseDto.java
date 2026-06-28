package com.urlshortener.dto;

import lombok.Getter;

@Getter
public class ErrorResponseDto {

	private final String error;

	private final String message;

	public ErrorResponseDto(String error, String message) {
		this.error = error;
		this.message = message;
	}

}
