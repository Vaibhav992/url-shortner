package com.urlshortener.exception;

public class InvalidAliasException extends RuntimeException {

	public InvalidAliasException(String message) {
		super(message);
	}

}
