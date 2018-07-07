package com.example.faasgw;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.faas.dto.JobResponse;
import com.example.faasgw.ex.CorrelationTimeoutException;
import com.example.faasgw.ex.NonCorrelationException;

@Component
public class Correlation {
	
	class Wrapper {
		private JobResponse response;
		
		public void setResponse(JobResponse response) {
			this.response = response;
		}
		public JobResponse getResponse() {
			return response;
		}
	}
	
	private Map<String,Wrapper> map = new HashMap<>();

	public JobResponse waitFor(String corellationId) throws CorrelationTimeoutException, InterruptedException {
		Wrapper wrapper = new Wrapper();
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
	
	public void responseReceived(String corellationId, JobResponse response) 
			throws NonCorrelationException {
		
		if( ! map.containsKey(corellationId)) {
			throw new NonCorrelationException("No wrapper for "+corellationId);
		}
		
		Wrapper wrapper = map.get(corellationId);
		
		synchronized(wrapper) {
			wrapper.setResponse(response);
			wrapper.notify();
		}
	}
}
