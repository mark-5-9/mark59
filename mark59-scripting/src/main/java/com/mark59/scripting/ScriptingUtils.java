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

package com.mark59.scripting;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Contains utility methods to be used within mark59-scripting 
 */
public final class ScriptingUtils  {

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 * This class contains only static methods and should not be instantiated.
	 */
	private ScriptingUtils() {
		throw new AssertionError("Utility class should not be instantiated");
	}
	
	
	/**
	 * Splits the given browser launch arguments string into a list of individual arguments.
	 * The input string can be delimited by either:<br>
	 *  semicolons (;)<br> OR <br>
	 *  commas (,) followed by optional whitespace and double dashes (--).  The dashes
	 *  remain part of the launch argument. 
	 * <p>This allows commas to be used to separate values in a multi-value argument
	 * <p>For example 
	 * <code>"--arg1=A; --arg2=B,C;--arg3, --arg4,--arg5=D,E,F,--arg6,--arg7"</code><br>
	 * will create this list of launch args:<br> 
	 * <code>
	 * --arg1=A<br>
	 * --arg2=B,C<br>
	 * --arg3<br>
	 * --arg4<br>
	 * --arg5=D,E,F<br>
	 * --arg6<br>
	 * --arg7"</code>
	 * 
	 * <p>Another example. This one will work in a chromium browser, opening devtools and 
	 * putting the browser in 'dark mode') 
	 * <code>"--auto-open-devtools-for-tabs,--force-dark-mode"</code><br>
	 * 
	 * @param browserLaunchArgs the string containing the browser launch arguments
	 * @return a list of individual browser launch arguments
	 */
	public static List<String> splitBrowserLaunchArgs(String browserLaunchArgs) {

		List<String> browserLaunchArgsList = new ArrayList<String>();
		if (StringUtils.isNotBlank(browserLaunchArgs)){
			String[] parts = browserLaunchArgs.split(";|,(?=\\s*--)");
			for (String part : parts) {
				String trimmed = part.trim();	
				if (!trimmed.isEmpty()) {
					browserLaunchArgsList.add(trimmed);
				}
			}
		}
		return browserLaunchArgsList;
	}
	
}
