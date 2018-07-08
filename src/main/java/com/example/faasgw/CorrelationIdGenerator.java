package com.example.faasgw;

import org.springframework.stereotype.Component;

@Component
public class CorrelationIdGenerator {

	private long count;
	
	public synchronized String createCorrelationId() {
		return "CORRELATION-"+(count++);
	}
}
