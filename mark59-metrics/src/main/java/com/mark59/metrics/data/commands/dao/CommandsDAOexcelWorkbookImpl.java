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

package com.mark59.metrics.data.commands.dao;

import java.util.Iterator;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.mark59.metrics.data.beans.Command;
import com.mark59.metrics.utils.ServerMetricsWebUtils;

/**
 * @author Philip Webb
 * Written: Australian Autumn 2020  
 */
public class CommandsDAOexcelWorkbookImpl implements CommandsDAO 
{
	
	Sheet commandsSheet;

	public CommandsDAOexcelWorkbookImpl(Sheet commandsSheet) {
		this.commandsSheet = commandsSheet;
	}


	@Override
	public Command findCommand(String commandName){
		Command command = null; 
        Iterator<Row> iterator = commandsSheet.iterator();
        iterator.next();   										 //a header row is assumed and bypassed
        boolean notFound = true;

        while (iterator.hasNext() && notFound ) {
            Row commandRow = iterator.next();
            // System.out.println("command key=" + ServerMetricsWebUtils.cellValue(commandRow.getCell(0)));
            
			if (commandName != null && commandName.equalsIgnoreCase(ServerMetricsWebUtils.cellValue(commandRow.getCell(0)))){	            
            	notFound=false;
            	command =new Command();
            	command.setCommandName 	(ServerMetricsWebUtils.cellValue(commandRow.getCell(0)));
            	command.setExecutor		(ServerMetricsWebUtils.cellValue(commandRow.getCell(1)));
            	command.setCommand		(ServerMetricsWebUtils.cellValue(commandRow.getCell(2)));
            	command.setIngoreStderr	(ServerMetricsWebUtils.cellValue(commandRow.getCell(3)));
            	command.setComment		(ServerMetricsWebUtils.cellValue(commandRow.getCell(4)));
            	command.setParamNames(deserializeJsonToList(ServerMetricsWebUtils.cellValue(commandRow.getCell(5))));
            }
        }   
		return  command;
	}


	@Override
	public List<Command> findCommands() {
		return null;
	}

	@Override
	public List<Command> findCommands(String selectionCol, String selectionValue) {
		return null;
	}

	@Override
	public void insertCommand(Command command) {
	}

	@Override
	public void updateCommand(Command command) {
	}

	@Override
	public void deleteCommand(String commandName) {
	}

}
