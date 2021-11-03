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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.StringUtils;

import com.mark59.core.interfaces.JmeterFunctions;

/**
* @author Michael Cohen
* Written: Australian Winter 2019
*/
public class TesterFactory {

	private TesterFactory() {
	}

	/**
	 * no longer used - to be removed future release
	 * @param testerClassName testerClassName
	 * @param threadName threadName
	 * @return  JmeterFunctions 
	 * @throws ClassNotFoundException ClassNotFoundException
	 * @throws NoSuchMethodException NoSuchMethodException
	 * @throws InstantiationException InstantiationException
	 * @throws IllegalAccessException IllegalAccessException
	 * @throws InvocationTargetException InvocationTargetException
	 */
	public static JmeterFunctions getTester(String testerClassName, String threadName) throws ClassNotFoundException,
			NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		
		if (StringUtils.isBlank(testerClassName))
			throw new IllegalArgumentException("testerClass cannot be null or empty");

		Class<?> testerClass = Class.forName(testerClassName);
						
		Constructor<?> testerConstructor = testerClass.getConstructor(String.class);

		return (JmeterFunctions) testerConstructor.newInstance(threadName);

	}

}
