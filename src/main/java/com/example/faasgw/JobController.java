package com.example.faasgw;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.faas.dto.JobRequest;
import com.example.faas.dto.JobResponse;
import com.example.faasgw.ex.CorrelationTimeoutException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class JobController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JobController.class);
	
	@Value("${routing-key}")
	private String gatewayRoutingKey;
	
	@Autowired
    private RabbitTemplate rabbitTemplate;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private CorrelationIdGenerator correlationIdGenerator;
	
	@Autowired
	private Correlation correlation;

	@RequestMapping(value="/functions/{function}", method={ RequestMethod.GET })
	public ResponseEntity<JobResponse> submit(@PathVariable("function") String functionName, 
			HttpServletRequest req) throws IOException, InterruptedException {
		
		LOGGER.debug("New job for {}", functionName);
		Enumeration<String> parameterNames = req.getParameterNames();
		Map<String, String> params = new HashMap<>();
		while(parameterNames.hasMoreElements()) {
			String key = parameterNames.nextElement();
			params.put(key, req.getParameter(key)); // assumes singular values
		}
		LOGGER.debug("Params: {}", params);
		String correlationId = correlationIdGenerator.createCorrelationId();
		JobRequest jobRequest = new JobRequest(functionName, params, gatewayRoutingKey, correlationId);
		String json = mapper.writeValueAsString(jobRequest);
		rabbitTemplate.convertAndSend(json);
		LOGGER.debug("Message sent");
		
		try {
			LOGGER.debug("Waiting for response for {}", correlationId);
			JobResponse jobResponse = correlation.waitFor(correlationId);
			LOGGER.debug("Response received");
			return new ResponseEntity<JobResponse>(jobResponse, HttpStatus.OK);
		} 
		catch (CorrelationTimeoutException e) {
			LOGGER.error("Timed out waiting for "+correlationId);
			return new ResponseEntity<JobResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
/*
 {
	 "jobRequest": {"functionName":"test",
	   "params":{"model":"330",
	       "drive":"hybrid",
	       "vehicle":"car",
	       "capacity":"3.0l"},
	    "responseRoutingKey":"ramsay",
	    "correlationId":"CORRELATION-0"
	 },
	 "jsonResponse": "this is some json"
 }
 */
