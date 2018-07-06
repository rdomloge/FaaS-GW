package com.example.faasgw;

import java.util.Map;

public class JobRequest {

	private String functionName;
	
	private Map<String, String> params;
	
	private String responseRoutingKey;
	
	private String correlationId;

	
	public JobRequest(String functionName, Map<String, String> params, String responseRoutingKey,
			String correlationId) {
		this.functionName = functionName;
		this.params = params;
		this.responseRoutingKey = responseRoutingKey;
		this.correlationId = correlationId;
	}

	public String getFunctionName() {
		return functionName;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public String getResponseRoutingKey() {
		return responseRoutingKey;
	}

	public String getCorrelationId() {
		return correlationId;
	}
}
