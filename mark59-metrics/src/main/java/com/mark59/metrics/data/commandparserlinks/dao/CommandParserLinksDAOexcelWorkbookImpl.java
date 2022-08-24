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

package com.mark59.metrics.data.commandparserlinks.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.mark59.metrics.data.beans.CommandParserLink;
import com.mark59.metrics.utils.ServerMetricsWebUtils;

/**
 * @author Philip Webb
 * Written: Australian Autumn 2020  
 */
public class CommandParserLinksDAOexcelWorkbookImpl implements CommandParserLinksDAO 
{
	
	Sheet commandparserlinksSheet;

	public CommandParserLinksDAOexcelWorkbookImpl(Sheet commandparserlinksSheet) {
		this.commandparserlinksSheet = commandparserlinksSheet;
	}

	@Override
	public List<CommandParserLink> findCommandParserLinksForCommand(String commandName){

		List<CommandParserLink> commandParserLinkList = new ArrayList<>();

		Iterator<Row> iterator = commandparserlinksSheet.iterator();
		iterator.next(); // a header row is assumed and bypassed

		while (iterator.hasNext()) {
			Row commandparserlinksRow = iterator.next();
			// System.out.println("commandparserlinks key =" + ServerMetricsWebUtils.cellValue(commandparserlinksRow.getCell(0)));
			
			if (commandName != null && commandName.equalsIgnoreCase(ServerMetricsWebUtils.cellValue(commandparserlinksRow.getCell(0)))) {				
				CommandParserLink commandParserLink = new CommandParserLink();
				commandParserLink.setCommandName(ServerMetricsWebUtils.cellValue(commandparserlinksRow.getCell(0)));
				commandParserLink.setParserName	(ServerMetricsWebUtils.cellValue(commandparserlinksRow.getCell(1)));
				commandParserLinkList.add(commandParserLink);
			}

		}	
		return commandParserLinkList;
	}

	
	@Override
	public CommandParserLink findCommandParserLink(String commandName, String parserName) {
		return null;
	}

	@Override
	public List<CommandParserLink> findCommandParserLinks() {
		return null;
	}

	@Override
	public List<CommandParserLink> findCommandParserLinks(String selectionCol, String selectionValue) {
		return null;
	}

	@Override
	public void insertCommandParserLink(CommandParserLink commandParserLink) {
	}

	@Override
	public void updateCommandParserLinksForCommandName(String commandName, List<String> parserNames) {
	}

	@Override
	public void deleteCommandParserLink(CommandParserLink commandParserLink) {
	}

	@Override
	public void deleteCommandParserLinksForCommandName(String commandName) {
	}

	@Override
	public void deleteCommandParserLinksForParserName(String parserName) {
	}
	
}
