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

import com.mark59.core.ScreenshotEnabledDriverWrapper;
/**
* @author Michael Cohen
* Written: Australian Winter 2019 
*/
public class MockScreenshotEnabledDriverWrapper extends ScreenshotEnabledDriverWrapper<MockDriver> {

	public MockScreenshotEnabledDriverWrapper(MockDriver driverPackage) {
		super(driverPackage);
	}


	@Override
	protected byte[] driverTakeScreenshot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void driverDispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDriverLogs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearDriverLogs() {
		// TODO Auto-generated method stub
		
	}

}
