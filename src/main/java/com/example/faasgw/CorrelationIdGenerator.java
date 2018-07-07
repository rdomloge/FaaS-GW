package com.example.faasgw;

import org.springframework.stereotype.Component;

@Component
public class CorrelationIdGenerator {

	private long count;
	
	public String createCorrelationId() {
		return "CORRELATION-"+(count++);
	}
}
