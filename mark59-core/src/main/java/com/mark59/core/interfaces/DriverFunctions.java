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

package com.mark59.core.interfaces;

/**
 * Wrapper class used to encapsulate an arbitrary Driver object, where the
 * Driver object knows how to perform an arbitrary set of functions necessary to
 * execute the test.
 * 
 * <p>A driver is also expected to know how to take a screenshot, accessible 
 * through {@link #captureScreenshot()} 
 * 
 * <p>For a selenium implementation of a driver, it would be expected to be a type of WebDriver  
 * 
 * @param <T> Concrete Driver to be wrapped
 * @author Michael Cohen 
 * @author Philip Webb 
 * Written: Australian Winter 2019
 */
public interface DriverFunctions<T> {

	/**
	 * Returns the concrete driver encapsulated by this class
	 * @return driver
	 */
	T getDriver();

	
	/**
	 * Returns the class name of the encapsulated Driver.
	 * @return String
	 */
	String getDriverClass();

	/**
	 * @return byte[] captured screenshot as a byte array (abstract)
	 */
	byte[] captureScreenshot();


	/**
	 * Handles any needed cleanup once the driver is finished with, if any cleanup is required.
	 */
	void driverDispose();



}
