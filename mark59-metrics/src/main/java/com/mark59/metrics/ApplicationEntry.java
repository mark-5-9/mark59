/*
 *  Copyright 2019 Insurance Australia Group Limited
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mark59.metrics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import com.mark59.metrics.utils.AppConstantsServerMetricsWeb;

/**
 * @author Philip Webb
 * Written: Australian Autumn 2020  
 */
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class ApplicationEntry extends SpringBootServletInitializer {
	
	static final Logger LOG = LogManager.getLogger(ApplicationEntry.class);	

	
	public static void main(String[] args) {
		setUserAccessibleDefinedVariables();
		SpringApplication.run(ApplicationEntry.class, args);
	}


    /**
     *   Required for Application Server (Tomcat) deployment 
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ApplicationEntry.class);
    }

    
    private static void setUserAccessibleDefinedVariables() {
    	// this variable is used in the win demo scripts (it's awkward to getting to pwd using the WMIC cmd window) 
		System.setProperty(AppConstantsServerMetricsWeb.METRICS_BASE_DIR, System.getProperty("user.dir"));
		LOG.trace("User variable " + AppConstantsServerMetricsWeb.METRICS_BASE_DIR + " : "
				+ System.getProperty(AppConstantsServerMetricsWeb.METRICS_BASE_DIR));
    }
	
}
