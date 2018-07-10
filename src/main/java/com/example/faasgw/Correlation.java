package com.example.faasgw;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.faas.dto.JobResponse;
import com.example.faasgw.ex.CorrelationTimeoutException;
import com.example.faasgw.ex.NonCorrelationException;

@Component
public class Correlation {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Correlation.class);
	
	private ScheduledExecutorService exec;
	
	@Value("${CORRELATION_RESPONSE_KEEP_SECONDS:35}")
	private int responseKeepSeconds;
	
	@PostConstruct
	public void setup() {
		exec = Executors.newScheduledThreadPool(1);
	}
	
	@PreDestroy
	public void shutdown() {
		exec.shutdownNow();
	}
	
	class Wrapper {
		private JobResponse response;
		private boolean async;
		public Wrapper(boolean async) {
			this.async = async;
		}
		
		public void setResponse(JobResponse response) {
			this.response = response;
		}
		public JobResponse getResponse() {
			return response;
		}
		public boolean isAsync() {
			return async;
		}
	}
	
	private Map<String,Wrapper> map = new HashMap<>();
	
	public void submitAsyncFor(String corellationId) {
		Wrapper wrapper = new Wrapper(true);
		map.put(corellationId, wrapper);
	}

	public JobResponse waitFor(String corellationId) throws CorrelationTimeoutException, InterruptedException {
		Wrapper wrapper = new Wrapper(false);
		map.put(corellationId, wrapper);
		try {
			synchronized(wrapper) {
				wrapper.wait(10000);
			}
			if(null == wrapper.getResponse()) throw new CorrelationTimeoutException(corellationId);
		}
		finally {
			map.remove(corellationId);
		}
		return wrapper.getResponse();
	}
	
	public JobResponse pollFor(String corellationId) throws NonCorrelationException {
		if( ! map.containsKey(corellationId)) {
			throw new NonCorrelationException("No wrapper for "+corellationId);
		}
		
		Wrapper wrapper = map.get(corellationId);
		if( ! wrapper.isAsync()) {
			throw new NonCorrelationException("Request was not asynchronous: "+corellationId);
		}
		if(null == wrapper.getResponse()) {
			return null;
		}
		
		JobResponse response = wrapper.getResponse();
		map.remove(corellationId);
		return response;
	}
	
	public void responseReceived(String corellationId, JobResponse response) 
			throws NonCorrelationException {
		
		if( ! map.containsKey(corellationId)) {
			throw new NonCorrelationException("No wrapper for "+corellationId);
		}
		
		Wrapper wrapper = map.get(corellationId);
		
		if(wrapper.isAsync()) {
			wrapper.setResponse(response);
			// set a timer to remove it in 30s
			exec.schedule(new Runnable() {
				@Override
				public void run() {
					if(map.containsKey(corellationId)) {
						Wrapper removed = map.remove(corellationId);
						JobResponse removedResponse = removed.getResponse();
						LOGGER.warn("Reponse for {} not collected after {} seconds: {}", corellationId, 
							responseKeepSeconds, removedResponse.getPayload());
					}
					else {
						LOGGER.debug("Response used");
					}
					
				}}, responseKeepSeconds, TimeUnit.SECONDS);
		}
		else {
			synchronized(wrapper) {
				wrapper.setResponse(response);
				wrapper.notify();
			}
		}
	}
}
