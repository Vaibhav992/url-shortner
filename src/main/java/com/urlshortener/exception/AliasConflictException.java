package com.urlshortener.exception;

public class AliasConflictException extends RuntimeException {

	public AliasConflictException(String message) {
		super(message);
	}

}
