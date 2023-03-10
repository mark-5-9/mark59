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

package com.mark59.trends.load;

import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.mark59.trends.application.AppConstantsMetrics;

/**
 * Entry point into TrendsLoad.  Loads test run the data into a database used by the Mark59 Trends web application,
 * checking transaction and metric SLAs for the run in the process.  
 *
 */
@SpringBootApplication
public class ApplicationEntry  implements CommandLineRunner  {
		
	/**
	 * Spring's behavior is that it will execute this (required Override) method <b>and</b> the override 'run' method in TrendsLoad
	 */
	@Override
	public void run(String... args) throws Exception {
	}


    //For a quick and dirty test set the args at the start of the main  (examples in the commented out lines below):

	public static void main(String[] args) {
  
		System.out.println("Starting Trends Load..   Version: "  + AppConstantsMetrics.MARK59_VERSION_TRENDING );

//		args = new String[] { "-a", "DataHunter", "-i", "C:/Mark59_Runs/Jmeter_Results/DataHunter", "-d","mysql", "-h","localhost", "-p", "3306", "-q", "?allowPublicKeyRetrieval=true&useSSL=false", "-t", "JMETER", "-r", "sample_run_01" };
//		args = new String[] { "-a", "DataHunter", "-i", "C:/Mark59_Runs/Jmeter_Results/DataHunter", "-d","pg",    "-h","localhost", "-t", "JMETER", "-q", "?sslmode=disable", "-r", "sample_run_pg01"  };
//      args = new String[] { "-a", "DataHunter", "-i", "C:/Mark59_Runs/Jmeter_Results/DataHunter", "-d","h2" }; //	h2mem h2tcpclient	
//		args = new String[] { "-a", "DataHunter", "-i", "./src/test/resources/JmeterResultsDataHunterGeneral", "-d","h2" }; 	
//		args = new String[] { "-a", "UsingLRapp", "-i", "C:/Mark59_Runs/LR_Results/LRapp/An_Session1/An_Session1.mdb", "-d","mysql", "-t","LOADRUNNER"};
//		args = new String[] { "-a", "DataHunter", "-i", "./src/test/resources/GatlingResults", "-l","simulation.logv341", "-e","responseTimeInMillis", "-d","h2", "-t","GATLING" };		
//		args = new String[] { "-a", "MyApp", "-i", "C:/Mark59_Runs/Gatling_Results/MyApp", "-e","responseTimeInMillis|someOtherAssert", "-d","h2", "-t","GATLING" };		
	
		
		// arguments are parsed before Spring Configuration, as the config uses values passed in the args.
		TrendsLoad.parseArguments(args);

		SpringApplication application = new SpringApplication(ApplicationEntry.class);
		application.setWebApplicationType(WebApplicationType.NONE);
		application.setBannerMode(Banner.Mode.OFF);
		application.run(args);
		
		System.out.println("\nTrends Load completed.");		
	}
}
