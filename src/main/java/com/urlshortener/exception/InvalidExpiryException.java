package com.urlshortener.exception;

public class InvalidExpiryException extends RuntimeException {

	public InvalidExpiryException(String message) {
		super(message);
	}

}
