package com.urlshortener.exception;

import com.urlshortener.dto.ErrorResponseDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(InvalidUrlException.class)
	public ResponseEntity<ErrorResponseDto> handleInvalidUrl(InvalidUrlException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponseDto("INVALID_URL", ex.getMessage()));
	}

	@ExceptionHandler(InvalidExpiryException.class)
	public ResponseEntity<ErrorResponseDto> handleInvalidExpiry(InvalidExpiryException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponseDto("INVALID_EXPIRY", ex.getMessage()));
	}

	@ExceptionHandler(InvalidAliasException.class)
	public ResponseEntity<ErrorResponseDto> handleInvalidAlias(InvalidAliasException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponseDto("INVALID_ALIAS", ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Validation failed")
				.orElse("Validation failed");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponseDto("VALIDATION_ERROR", message));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponseDto> handleUnreadableMessage(HttpMessageNotReadableException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponseDto("INVALID_REQUEST", "Malformed JSON request body"));
	}

	@ExceptionHandler(AliasConflictException.class)
	public ResponseEntity<ErrorResponseDto> handleAliasConflict(AliasConflictException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(new ErrorResponseDto("ALIAS_CONFLICT", ex.getMessage()));
	}

	@ExceptionHandler(UrlNotFoundException.class)
	public ResponseEntity<ErrorResponseDto> handleUrlNotFound(UrlNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ErrorResponseDto("NOT_FOUND", ex.getMessage()));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponseDto("INTERNAL_ERROR", "Unexpected database conflict"));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseDto> handleUnexpected(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponseDto("INTERNAL_ERROR", "An unexpected error occurred"));
	}

}
