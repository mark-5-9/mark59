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

package com.mark59.core.utils;

/**
 * The current list of properties used by the Mark59 framework are defined here.
 * <p>
 * <b>mark59.screenshot.directory</b> : value defines the directory where transaction-level logging will occur
 * <b>mark59.selenium.driver.path.chrome</b> : location of the chrome driver executable
 * <b>mark59.selenium.driver.path.firefox</b> :  location of the geokodriver executable 
 * </p>
 * 
 * @author Michael Cohen
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class PropertiesKeys {

	private PropertiesKeys() {
	}
	
	public static final String MARK59_PROP_SCREENSHOT_DIRECTORY 	= "mark59.screenshot.directory";
	public static final String MARK59_PROP_DRIVER_CHROME			= "mark59.selenium.driver.path.chrome";
	public static final String MARK59_PROP_DRIVER_FIREFOX			= "mark59.selenium.driver.path.firefox";
	
	public static final String[] MARK59_PROPERTY_KEYS =  { MARK59_PROP_SCREENSHOT_DIRECTORY, MARK59_PROP_DRIVER_CHROME, MARK59_PROP_DRIVER_FIREFOX };
}
