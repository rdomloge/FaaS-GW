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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

	@RequestMapping(value="/functions/{function}", method={ RequestMethod.GET })
	public void submit(@PathVariable("function") String functionName, HttpServletRequest req) throws IOException {
		
		LOGGER.debug("New job for {}", functionName);
		Enumeration<String> parameterNames = req.getParameterNames();
		Map<String, String> params = new HashMap<>();
		while(parameterNames.hasMoreElements()) {
			String key = parameterNames.nextElement();
			params.put(key, req.getParameter(key)); // assumes singular values
		}
		LOGGER.debug("Params: {}", params);
		JobRequest jobRequest = new JobRequest(functionName, params, gatewayRoutingKey);
		String json = mapper.writeValueAsString(jobRequest);
		rabbitTemplate.convertAndSend(json);
		LOGGER.debug("Message sent");
	}
}
