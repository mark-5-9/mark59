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

package com.mark59.datahunter.application;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class DataHunterUtils  {

	public static void expireSession(HttpServletRequest httpServletRequest) {
		expireSession( httpServletRequest, 10); 
	}
	
	public static void expireSession(HttpServletRequest httpServletRequest, int intervalinSecs) {
		HttpSession httpSession = httpServletRequest.getSession(false); 
		if (httpSession != null){
			httpSession.setMaxInactiveInterval(intervalinSecs);
		}
	}

	public static boolean isEmpty(final String s) {
		return s == null || s.trim().isEmpty();
	}
	
}