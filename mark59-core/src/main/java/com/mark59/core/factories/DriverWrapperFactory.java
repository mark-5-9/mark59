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

package com.mark59.core.factories;

import java.util.Map;

import com.mark59.core.DriverWrapper;

/**
* @author Michael Cohen
* Written: Australian Winter 2019
*/
public interface DriverWrapperFactory {

	/**
	 * Used to create an object to encapsulate an arbitrary Driver object, where the Driver object knows 
	 * how to perform an arbitrary set of functions. Drivers can be custom, or come from a third party,
	 * such as one of the SeleniumWebDrivers
	 *  
	 * @param <T> Concrete Driver to be wrapped
	 * @param arguments map of key value pairs known to the implementation 
	 * @return DriverWrapper
	 */
    <T extends DriverWrapper<?>> T makeDriverWrapper(Map<String, String> arguments);

}
