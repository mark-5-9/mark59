package com.mark59.datahunter.test;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

// ref: https://spring.io/guides/gs/testing-web/
// @SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT) // will use port 8081 
// note @LocalServerPort has been deprecated/removed

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2mem")
public class DataHunterWebApplicationTests    {
	
	@Value("${local.server.port}")
	private int port;	
	
	
	@Test
	public void checkDataHunterApplicationContextLoads() {
		System.out.println("Started DataHunter using h2mem profile on port " + port);
		// try {Thread.sleep(300000);} catch (Exception e) {}
	}
}