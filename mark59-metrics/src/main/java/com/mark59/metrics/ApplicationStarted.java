/*
 *  Copyright 2019 Mark59.com
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
@Component
public class ApplicationStarted  {
	
	@Autowired
	PropertiesConfiguration springBootConfiguration;	


	@EventListener(ApplicationReadyEvent.class)
	public void messageoOnStartup() {
	    
		String hide     = springBootConfiguration.getMark59metricshide();
		String apiauth  = springBootConfiguration.getMark59metricsapiauth();
		
		if (hide != null && (hide.toLowerCase().startsWith("y") || hide.toLowerCase().startsWith("t"))) {
			System.out.println("hide activated");
		} else {
			
			String startupMsg = "UI id=" + springBootConfiguration.getMark59metricsid() 
				+ ", passwrd=" + springBootConfiguration.getMark59metricspasswrd() 
				+ ", API apiauth=" + apiauth;
			
			if (String.valueOf(true).equalsIgnoreCase(apiauth)){
				startupMsg = startupMsg + ", apiuser=" + springBootConfiguration.getMark59metricsapiuser() 
					+ ", apipass=" + springBootConfiguration.getMark59metricsapipass(); 
			}
			
			startupMsg = startupMsg + ". Please set 'mark59metricshide' to 'true' to stop this message "
				+ "(either as a command line argument, or OS environment variable)";
			
			System.out.println(startupMsg);
		}
	    
	}	
	 
	
}
