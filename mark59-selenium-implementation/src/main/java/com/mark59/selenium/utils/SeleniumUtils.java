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
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * General catch-all class for useful functions related to Selenium
 *  
 *  @author Philip Webb
 *  Written: Australian Winter 2021 
 */
public class SeleniumUtils {
	
	private static final Logger LOG = LogManager.getLogger(SeleniumUtils.class);

	/**
	 * Allows a map with additional or entries that exist in a base map to be combined, or where the same key exists in both maps, the 
	 * value in the override map will be used.  
	 * <p>returned as Jmeter Arguments 
	 * 
	 * @param baseMap  Map of base key values 
	 * @param additionalEntriesMap  additional of override key values
	 * @return jmeterArguments
	 */
	public static Arguments mergeMapWithAnOverrideMap(Map<String,String> baseMap, Map<String, String> additionalEntriesMap) {
		Arguments jmeterArguments = new Arguments();
		Map<String, String> jmeterArgumentsMap = new LinkedHashMap<String,String>();
		Map<String,String> baseMapMergedWithAdditionalEntriesMap = new LinkedHashMap<String,String>(); 
		
		for (Map.Entry<String, String> defaultEntry : baseMap.entrySet()) {
			if (additionalEntriesMap.containsKey(defaultEntry.getKey())){
				baseMapMergedWithAdditionalEntriesMap.put(defaultEntry.getKey(), additionalEntriesMap.get(defaultEntry.getKey()));
				additionalEntriesMap.remove(defaultEntry.getKey());
			} else {
				baseMapMergedWithAdditionalEntriesMap.put(defaultEntry.getKey(), baseMap.get(defaultEntry.getKey()));
			}
		}
		
		jmeterArgumentsMap.putAll(additionalEntriesMap);
		jmeterArgumentsMap.putAll(baseMapMergedWithAdditionalEntriesMap);
		
		for (Map.Entry<String, String> parameter : jmeterArgumentsMap.entrySet()) {
			jmeterArguments.addArgument(parameter.getKey(), parameter.getValue());
		}
		
		if (LOG.isDebugEnabled()){LOG.debug("jmeter arguments at end of mergeMapWithAnOverrideMap : " + Arrays.toString(jmeterArguments.getArgumentsAsMap().entrySet().toArray()));} 
		return jmeterArguments;
	}
	
	
	/**
	 * Directly hits the chromedriver and reads the initial message to obtain it's version (intended for internal use only). 
	 * @param seleniumDriverPath  the path of the selenium Driver
	 * @return chomedriver version
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

	
	/**
	 * On Win older versions of the chromedriver (2.x) expect Chrome to be installed within the C:/Program Files (x86)/ directory,
	 * but more recent versions of Chrome now install at  C:/Program Files/
	 * <p>This is a 'hacky' work-around to see if Chrome is now installed at the newer location.  Just meant to help make the 'demo'     
	 * script run, and intended for internal use only.  
	 * 
	 * @return updated chrome.exe file location
	 */
	public static String legacyChromedriverHack() {
		String legacyWinChromeLocation = "C:/Program Files (x86)/Google/Chrome/Application/chrome.exe";
		String recentWinChromeLocation = "C:/Program Files/Google/Chrome/Application/chrome.exe";
		
		if ( ! new File(legacyWinChromeLocation).exists()) {
			if (new File(recentWinChromeLocation).exists()) {
				LOG.warn("\n\n The Mark59 framework has detected you are using an old (2.x) version of the chromedriver, " +
						"and your Chrome instance is in a location that it cannot find by default.\n" +
						" You are advised to change to a more recent version of the chromedriver.\n" );
				return recentWinChromeLocation; 
			}
		}
		return null;
	}

}
