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

package com.mark59.metrics.data.commandparserlinks.dao;

import java.util.List;

import com.mark59.metrics.data.beans.CommandParserLink;


/**
 * @author Philip Webb
 * Written: Australian Summer 2020
 */
public interface CommandParserLinksDAO 
{
	CommandParserLink findCommandParserLink(String commandName, String parserNames);
	
	List<CommandParserLink> findCommandParserLinks();
	List<CommandParserLink> findCommandParserLinksForCommand(String commandName);
	List<CommandParserLink> findCommandParserLinks(String selectionCol, String selectionValue);
	
	void insertCommandParserLink(CommandParserLink commandParserLink);

	// all fields are currently key - so any update methods are really add / delete
	// public void updateCommandParserLink(CommandParserLink commandParserLink); 
    void updateCommandParserLinksForCommandName(String commandName, List<String> parserNames);

	void deleteCommandParserLink(CommandParserLink commandParserLink);
	void deleteCommandParserLinksForCommandName(String commandName);
	void deleteCommandParserLinksForParserName(String parserNames);



}