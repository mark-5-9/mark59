package com.mark59.datahunter.test;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;

// ref: https://spring.io/guides/gs/testing-web/
// @SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT) // will use port 8081 

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2mem")
public class DataHunterWebApplicationTests    {
	
	@LocalServerPort
	private int port;	
	
	@Test
	public void checkDataHunterApplicationContextLoads() {
		System.out.println("Started DataHunter using h2mem profile on port " + port);
		// try {Thread.sleep(300000);} catch (Exception e) {}
	}
}