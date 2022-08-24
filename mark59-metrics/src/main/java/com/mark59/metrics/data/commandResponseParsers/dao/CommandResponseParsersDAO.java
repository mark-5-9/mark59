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

package com.mark59.metrics.data.commandResponseParsers.dao;

import java.util.List;

import com.mark59.metrics.data.beans.CommandResponseParser;


/**
 * @author Philip Webb
 * Written: Australian Summer 2020
 */
public interface CommandResponseParsersDAO 
{
	CommandResponseParser findCommandResponseParser(String parserName);
	
	List<CommandResponseParser> findCommandResponseParsers();
	
	List<CommandResponseParser> findCommandResponseParsers(String selectionCol, String selectionValue);
	
	void insertCommandResponseParser(CommandResponseParser commandResponseParser);

	void updateCommandResponseParser(CommandResponseParser commandResponseParser);

	void deleteCommandResponseParser(String parserName);

}