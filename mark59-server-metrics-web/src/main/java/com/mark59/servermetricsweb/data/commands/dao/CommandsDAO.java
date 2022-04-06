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

package com.mark59.servermetricsweb.data.commands.dao;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mark59.servermetricsweb.data.beans.Command;


/**
 * @author Philip Webb
 * Written: Australian Summer 2020
 */
public interface CommandsDAO 
{
	Command findCommand(String commandName);
	
	List<Command> findCommands();
	
	List<Command> findCommands(String selectionCol, String selectionValue);
	
	void insertCommand(Command command);

	void updateCommand(Command command);

	void deleteCommand(String commandName);
	
	
	default String serializeListToJson(List<String> parameters)  {
		try {
			return new ObjectMapper().writeValueAsString(parameters);
		} catch (JsonProcessingException e) {
			return "";
		}
	}
	
	default List<String> deserializeJsonToList(String parameters)  {
		List<String> parametersList;
		TypeReference<ArrayList<String>> typeRef = new TypeReference<ArrayList<String>>(){};
		try {
			parametersList = new ObjectMapper().readValue(parameters, typeRef);
		} catch (Exception e) {
			return new ArrayList<>();
		}
		return parametersList;
	}	
	
	
	

}