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

package com.mark59.servermetricsweb.data.serverprofiles.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mark59.servermetricsweb.data.beans.ServerProfile;


/**
 * @author Philip Webb
 * Written: Australian Summer 2020
 */
public interface ServerProfilesDAO 
{
	ServerProfile findServerProfile(String serverProfileName);
	
	List<ServerProfile> findServerProfiles();
	
	List<ServerProfile> findServerProfiles(String selectionCol, String selectionValue);
	
	void insertServerProfile(ServerProfile serverProfile);

	void updateServerProfile(ServerProfile serverProfile);

	void deleteServerProfile(String serverProfileName);
	
	
	default String serializeMapToJson(Map<String,String> parameters)  {
		try {
			return new ObjectMapper().writeValueAsString(parameters);
		} catch (JsonProcessingException e) {
			return "";
		}
	}
		
	default Map<String,String> deserializeJsonToMap(String parameters)  {
		Map<String, String> parametersMap;
		TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>(){};
		try {
			parametersMap = new ObjectMapper().readValue(parameters, typeRef);
		} catch (Exception e) {
			return new HashMap<>();
		}
		return parametersMap;
	}	
	
	

}