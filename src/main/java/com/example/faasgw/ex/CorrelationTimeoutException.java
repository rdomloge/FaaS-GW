package com.example.faasgw.ex;

@SuppressWarnings("serial")
public class CorrelationTimeoutException extends Exception {

	public CorrelationTimeoutException() {
		super();
	}

	public CorrelationTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public CorrelationTimeoutException(String message) {
		super(message);
	}

	public CorrelationTimeoutException(Throwable cause) {
		super(cause);
	}

}
