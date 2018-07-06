package com.example.faasgw;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResponseReceiver {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseReceiver.class);
	
	public void receive(String message) {
		LOGGER.debug("Response received: {}", message);
	}
	
	public void receive(byte[] bytes) throws UnsupportedEncodingException {
		String message = new String(bytes, "utf-8");
		receive(message);
	}
}
