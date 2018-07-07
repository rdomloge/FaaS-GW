package com.example.faasgw;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.faas.dto.JobResponse;
import com.example.faasgw.ex.NonCorrelationException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ResponseReceiver {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseReceiver.class);
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private Correlation correlation;
	
	public void receive(String message) throws IOException {
		LOGGER.debug("Response received: {}", message);
		JobResponse jobResponse = mapper.readValue(message, JobResponse.class);
		LOGGER.debug("It's a response to {}", jobResponse.getJobRequest().getCorrelationId());
		try {
			correlation.responseReceived(jobResponse.getJobRequest().getCorrelationId(), jobResponse);
		} 
		catch (NonCorrelationException e) {
			// need to catch and log - prevent poison message
			LOGGER.warn("Response for an unknown correlation: {}", e.getMessage());
		}
	}
	
	public void receive(byte[] bytes) throws IOException {
		String message = new String(bytes, "utf-8");
		receive(message);
	}
}
