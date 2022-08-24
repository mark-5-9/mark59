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

package com.mark59.test.core;

import com.mark59.core.interfaces.DriverFunctions;

/**
* @author Michael Cohen
* Written: Australian Winter 2019 
*/
public class DriverFunctionMock implements DriverFunctions<MockDriver> {

	MockDriver mockDriver;
	
	/**
	 * @param webDriver the WebDriver to package
	 */
	public DriverFunctionMock(MockDriver mockDriver) {
		this.mockDriver = mockDriver;
	}
	
	@Override
	public MockDriver getDriver() {
		return this.mockDriver;
	}

	@Override
	public byte[] captureScreenshot() {
		return "ShouldReturnSomeIminageAsaByteArray".getBytes();
	}

	@Override
	public void driverDispose() {
		// TODO Auto-generated method stub
	}

	public void clearDriverLogs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDriverClass() {
		// TODO Auto-generated method stub
		return null;
	}

}
