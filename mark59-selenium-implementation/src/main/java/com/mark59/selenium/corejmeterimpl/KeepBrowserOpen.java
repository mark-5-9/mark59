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

package com.mark59.selenium.corejmeterimpl;

/**
 * <p>To indicate when the browser should be left open or closed when testing a selenium script.</p>
 *  
 * <p>Note the option has no effect when running in headless mode (the selenium driver will always close).</p>  
 * 
 *  <p>Options are :
 *  <ul>
 *  <li>	NEVER     - the browser will always close at test end (default except when testing via the runSeleniumTest() method). 
 *  <li> 	ONFAILURE - if the script errors (exception or assertion error caught), the browser will stay open, otherwise it will close. Set as the default behaviour when testing.	    
 *	<li>	ALWAYS    - the browser will be left open at the end of the test.	
 *  </ul>
 *  
 * @see com.mark59.selenium.corejmeterimpl.SeleniumAbstractJavaSamplerClient#runSeleniumTest(KeepBrowserOpen)
 * @author Philip Webb
 * Written: Australian Winter 2019   *  
 */
public enum KeepBrowserOpen {
	NEVER,
	ONFAILURE,
	ALWAYS
}
