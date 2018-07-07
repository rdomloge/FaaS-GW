package com.example.faasgw.ex;

@SuppressWarnings("serial")
public class NonCorrelationException extends Exception {

	public NonCorrelationException() {
		super();
	}

	public NonCorrelationException(String message, Throwable cause) {
		super(message, cause);
	}

	public NonCorrelationException(String message) {
		super(message);
	}

	public NonCorrelationException(Throwable cause) {
		super(cause);
	}

}
