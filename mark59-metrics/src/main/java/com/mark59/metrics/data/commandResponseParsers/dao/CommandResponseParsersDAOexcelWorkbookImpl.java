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

import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.mark59.metrics.data.beans.CommandResponseParser;
import com.mark59.metrics.utils.ServerMetricsWebUtils;


/**
 * @author Philip Webb
 * Written: Australian Autumn 2020  
 */
public class CommandResponseParsersDAOexcelWorkbookImpl implements CommandResponseParsersDAO 
{
	
	Sheet commandresponseparsersSheet;
	

	public CommandResponseParsersDAOexcelWorkbookImpl(Sheet commandresponseparsersSheet) {
		this.commandresponseparsersSheet = commandresponseparsersSheet;
	}


	@Override
	public CommandResponseParser findCommandResponseParser(String parserName){

		CommandResponseParser commandResponseParser = null; 
        Iterator<Row> iterator = commandresponseparsersSheet.iterator();
        iterator.next();  //a header row is assumed and bypassed
        boolean notFound = true;
		
        while (iterator.hasNext() && notFound ) {
            Row commandResponseParserRow = iterator.next();
            //System.out.println("commandResponseParser parserName=" + ServerMetricsWebUtils.cellValue(commandResponseParserRow.getCell(0)));
            
			if (parserName != null && parserName.equalsIgnoreCase(ServerMetricsWebUtils.cellValue(commandResponseParserRow.getCell(0)))){	
            	notFound=false;
            	commandResponseParser = new CommandResponseParser();
            	commandResponseParser.setParserName				(ServerMetricsWebUtils.cellValue(commandResponseParserRow.getCell(0)));
            	commandResponseParser.setMetricTxnType			(ServerMetricsWebUtils.cellValue(commandResponseParserRow.getCell(1)));
            	commandResponseParser.setMetricNameSuffix    	(ServerMetricsWebUtils.cellValue(commandResponseParserRow.getCell(2)));
            	commandResponseParser.setScript  				(ServerMetricsWebUtils.cellValue(commandResponseParserRow.getCell(3)));
            	commandResponseParser.setComment   				(ServerMetricsWebUtils.cellValue(commandResponseParserRow.getCell(4)));
            	commandResponseParser.setSampleCommandResponse  (ServerMetricsWebUtils.cellValue(commandResponseParserRow.getCell(5)));
            }
        }   	
		return  commandResponseParser;
	}


	@Override
	public List<CommandResponseParser> findCommandResponseParsers() {
		return null;
	}

	@Override
	public List<CommandResponseParser> findCommandResponseParsers(String selectionCol, String selectionValue) {
		return null;
	}

	@Override
	public void insertCommandResponseParser(CommandResponseParser commandResponseParser) {
	}

	@Override
	public void updateCommandResponseParser(CommandResponseParser commandResponseParser) {
	}

	@Override
	public void deleteCommandResponseParser(String parserName) {
	}

}
