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

package com.mark59.metrics.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;

import com.mark59.metrics.api.utils.AppConstantsServerMetrics;
import com.mark59.metrics.data.commandResponseParsers.dao.CommandResponseParsersDAO;
import com.mark59.metrics.data.commandResponseParsers.dao.CommandResponseParsersDAOexcelWorkbookImpl;
import com.mark59.metrics.data.commandparserlinks.dao.CommandParserLinksDAO;
import com.mark59.metrics.data.commandparserlinks.dao.CommandParserLinksDAOexcelWorkbookImpl;
import com.mark59.metrics.data.commands.dao.CommandsDAO;
import com.mark59.metrics.data.commands.dao.CommandsDAOexcelWorkbookImpl;
import com.mark59.metrics.data.servercommandlinks.dao.ServerCommandLinksDAO;
import com.mark59.metrics.data.servercommandlinks.dao.ServerCommandLinksDAOexcelWorkbookImpl;
import com.mark59.metrics.data.serverprofiles.dao.ServerProfilesDAO;
import com.mark59.metrics.data.serverprofiles.dao.ServerProfilesDAOexcelWorkbookImpl;
import com.mark59.metrics.drivers.ServerProfileRunner;
import com.mark59.metrics.pojos.ParsedCommandResponse;
import com.mark59.metrics.pojos.ParsedMetric;
import com.mark59.metrics.pojos.WebServerMetricsResponsePojo;

public class ServerProfileRunnerViaExcelTest  {
	
	
	@Test
    public final void testServerProfileRunnerUsingSimpleScriptSampleRunnerViaExcel() throws EncryptedDocumentException, IOException
    {
		String testModeNo = "Y";  // a hack to get the test summary
		File excelFile = new File("./src/test/resources/simpleSheetWithLocalhostProfileForEachOs/mark59serverprofiles.xlsx");
		String reqServerProfileName = "SimpleScriptSampleRunner";
    	 
    	Workbook workbook = WorkbookFactory.create(excelFile, null, true);  // Factory class necessary to avoid excel file being 'touched' 
        
    	Sheet serverprofilesSheet 		  = workbook.getSheet("SERVERPROFILES");
    	Sheet servercommandlinksSheet	  = workbook.getSheet("SERVERCOMMANDLINKS");
    	Sheet commandsSheet 			  = workbook.getSheet("COMMANDS");
    	Sheet commandparserlinksSheet 	  = workbook.getSheet("COMMANDPARSERLINKS");
    	Sheet commandresponseparsersSheet = workbook.getSheet("COMMANDRESPONSEPARSERS");
    	
    	ServerProfilesDAO serverProfilesDAO 				= new ServerProfilesDAOexcelWorkbookImpl(serverprofilesSheet); 
    	ServerCommandLinksDAO serverCommandLinksDAO 		= new ServerCommandLinksDAOexcelWorkbookImpl(servercommandlinksSheet);    	
    	CommandsDAO commandsDAO 							= new CommandsDAOexcelWorkbookImpl(commandsSheet);     	
    	CommandParserLinksDAO commandParserLinksDAO 		= new CommandParserLinksDAOexcelWorkbookImpl(commandparserlinksSheet);
    	CommandResponseParsersDAO commandResponseParsersDAO = new CommandResponseParsersDAOexcelWorkbookImpl(commandresponseparsersSheet);
        	
    	WebServerMetricsResponsePojo response = ServerProfileRunner.commandsResponse(reqServerProfileName, testModeNo, serverProfilesDAO,
				serverCommandLinksDAO, commandsDAO, commandParserLinksDAO, commandResponseParsersDAO,
				AppConstantsServerMetrics.RUNNING_VIA_EXCEL);
 		workbook.close();

 		ParsedCommandResponse parsedCommandResponse = response.getParsedCommandResponses().get(0);
		assertEquals("number of commands", 1, response.getParsedCommandResponses().size());
		assertEquals(parsedCommandResponse.isCommandFailure(), false);

		List<ParsedMetric> parsedMetrics = parsedCommandResponse.getParsedMetrics();
		assertEquals("number of parsed metrics", 3, parsedMetrics.size());
		assertEquals(parsedMetrics.get(0).toString(), "[label=a_memory_txn, result=123, dataType=MEMORY, success=true, parseFailMsg=null]");
		assertEquals(parsedMetrics.get(1).toString(), "[label=a_cpu_util_txn, result=33.3, dataType=CPU_UTIL, success=true, parseFailMsg=null]");		
		assertEquals(parsedMetrics.get(2).toString(), "[label=some_datapoint, result=66.6, dataType=DATAPOINT, success=true, parseFailMsg=null]");		
		assertEquals(response.getFailMsg(), "");
		assertTrue(response.getLogLines(), response.getTestModeResult().contains(
				"<font color='green'> You have received metrics results!  Please check the values are as you expect.</font>"));		
    }

	
	@Test
    public final void testServerProfileRunnerUsingDuffSimpleScriptSampleRunnerExcelSheet() throws EncryptedDocumentException, IOException
    {
		String testModeNo = "Y"; // a hack to get the test summary
		File excelFile = new File("./src/test/resources/duffSimpleSheetWithLocalhostProfileForEachOs/mark59serverprofiles.xlsx");
		String reqServerProfileName = "SimpleScriptSampleRunner";
    	 
    	Workbook workbook = WorkbookFactory.create(excelFile, null, true);  // Factory class necessary to avoid excel file being 'touched' 
        
    	Sheet serverprofilesSheet 		  = workbook.getSheet("SERVERPROFILES");
    	Sheet servercommandlinksSheet	  = workbook.getSheet("SERVERCOMMANDLINKS");
    	Sheet commandsSheet 			  = workbook.getSheet("COMMANDS");
    	Sheet commandparserlinksSheet 	  = workbook.getSheet("COMMANDPARSERLINKS");
    	Sheet commandresponseparsersSheet = workbook.getSheet("COMMANDRESPONSEPARSERS");
    	
    	ServerProfilesDAO serverProfilesDAO 				= new ServerProfilesDAOexcelWorkbookImpl(serverprofilesSheet); 
    	ServerCommandLinksDAO serverCommandLinksDAO 		= new ServerCommandLinksDAOexcelWorkbookImpl(servercommandlinksSheet);    	
    	CommandsDAO commandsDAO 							= new CommandsDAOexcelWorkbookImpl(commandsSheet);     	
    	CommandParserLinksDAO commandParserLinksDAO 		= new CommandParserLinksDAOexcelWorkbookImpl(commandparserlinksSheet);
    	CommandResponseParsersDAO commandResponseParsersDAO = new CommandResponseParsersDAOexcelWorkbookImpl(commandresponseparsersSheet);
        	
    	WebServerMetricsResponsePojo response = ServerProfileRunner.commandsResponse(reqServerProfileName, testModeNo, serverProfilesDAO,
				serverCommandLinksDAO, commandsDAO, commandParserLinksDAO, commandResponseParsersDAO,
				AppConstantsServerMetrics.RUNNING_VIA_EXCEL);
 		workbook.close();

		ParsedCommandResponse parsedCommandResponse = response.getParsedCommandResponses().get(0);
		assertEquals("number of commands", 1, response.getParsedCommandResponses().size());
		assertEquals(parsedCommandResponse.isCommandFailure(), true);

		assertTrue(parsedCommandResponse.getCommandResponse().contains(
				"Failure attempting to execute groovy script command : No such property: scriptResponseZ"));
	
		List<ParsedMetric> parsedMetrics = parsedCommandResponse.getParsedMetrics();
		assertEquals("number of parsed metrics", 0, parsedMetrics.size());
		
		assertTrue(response.getLogLines(), response.getLogLines().contains(
				"command SimpleScriptSampleCmd has failed"));	 		
		assertTrue(response.getTestModeResult(), response.getTestModeResult().contains(
				"<font color='red'> You have not received any metrics back!  Please check your commands (1 failures recorded)"));	
		assertEquals(response.getFailMsg(), "");
    }
	
}
