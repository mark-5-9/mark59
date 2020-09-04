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

import java.util.List;

import com.mark59.servermetricsweb.data.beans.Command;


/**
 * @author Philip Webb
 * Written: Australian Summer 2020
 */
public interface CommandsDAO 
{
	public Command findCommand(String commandName);
	
	public List<Command> findCommands();
	
	public List<Command> findCommands(String selectionCol, String selectionValue);
	
	void insertCommand(Command command);

	void updateCommand(Command command);

	void deleteCommand(String commandName);

}