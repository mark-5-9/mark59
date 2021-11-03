package com.mark59.selenium.utils
;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;

/**
 * General catch-all class for useful functions related to Selenium
 *  
 *  @author Philip Webb
 *  Written: Australian Winter 2021 
 */
public class SeleniumUtils {
	
	/**
	 * Directly hits the chromedriver and reads the initial message to obtain it's version (intended for internal use only).
	 * (Not in use - just delete it ?) 
	 * @param seleniumDriverPath  the path of the selenium Driver
	 * @return chromedriver version
	 */
	public static String interogateChromedriverVersion (String seleniumDriverPath) {
		String version = "NotFound";
		String chromeDriverMsg = "";
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(seleniumDriverPath);
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			chromeDriverMsg = reader.readLine();
			p.destroy();	
			version =  StringUtils.substringBetween(chromeDriverMsg, "ChromeDriver" , "(");
		} catch (Exception e) {
			return version + " (" + e.getMessage() + ")";
		}
		if (version == null) { 
			return "NotFound (chromeDriver msg : " + chromeDriverMsg + ")";
		} else {			
			return version.trim();
		}
	}

}
