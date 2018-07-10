package com.example.faasgw;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.example.faas.dto.JobRequest;
import com.example.faas.dto.JobResponse;
import com.example.faas.dto.Outcome;
import com.example.faasgw.ex.CorrelationTimeoutException;
import com.example.faasgw.ex.NonCorrelationException;

public class CorrelationTest {

	private Correlation target;
	
	private CorrelationIdGenerator idgen;
	
	private ScheduledExecutorService exec;
	
	public CorrelationTest() {
		
	}
	
	@Before
	public void setUp() throws Exception {
		target = new Correlation();
		idgen = new CorrelationIdGenerator();
		exec = Executors.newScheduledThreadPool(100);
	}

	@After
	public void tearDown() throws Exception {
		exec.shutdownNow();
	}

	class Consumer implements Runnable {
		protected String id;
		public Consumer(String id) {
			this.id = id;
		}
		@Override
		public void run() {
			try {
				target.waitFor(id);
			} catch (CorrelationTimeoutException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	class Producer extends Consumer {

		public Producer(String id) {
			super(id);
		}
		
		public void run() {
			String json = "adsf";
			Map<String, String> params = null;
			JobRequest request = new JobRequest("test", params, null, id);
			JobResponse response = new JobResponse(request, json, Outcome.SUCCESS);
			try {
				target.responseReceived(id, response);
			} catch (NonCorrelationException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Test
	public void test() throws InterruptedException {
		for(int i=0; i < 50; i++) {
			String id = idgen.createCorrelationId();
			exec.schedule(new Consumer(id), 100, TimeUnit.MILLISECONDS);
			exec.schedule(new Producer(id), 103, TimeUnit.MILLISECONDS);
		}
		
		Thread.sleep(15000);
	}

}
