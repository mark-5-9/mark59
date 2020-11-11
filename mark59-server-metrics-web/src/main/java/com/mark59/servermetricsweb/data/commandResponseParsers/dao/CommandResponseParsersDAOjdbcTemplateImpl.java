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

package com.mark59.servermetricsweb.data.commandResponseParsers.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mark59.servermetricsweb.data.beans.CommandResponseParser;

/**
 * @author Philip Webb
 * Written: Australian Summer 2020  
 */
public class CommandResponseParsersDAOjdbcTemplateImpl implements CommandResponseParsersDAO 
{
	
	@Autowired  
	private DataSource dataSource;
		

	@Override
	@SuppressWarnings("rawtypes")
	public CommandResponseParser findCommandResponseParser(String scriptName){

		List<CommandResponseParser> commandResponseParsersList = new ArrayList<CommandResponseParser>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		String selectServerSQL   = "select SCRIPT_NAME, METRIC_TXN_TYPE, METRIC_NAME_SUFFIX, SCRIPT, COMMENT, SAMPLE_COMMAND_RESPONSE "
				+ "from COMMANDRESPONSEPARSERS where SCRIPT_NAME = '" + scriptName + "'"
				+ " order by SCRIPT_NAME asc;";
		
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectServerSQL);
		
		if (rows.size() == 0 ){
			return null;
		}
		Map row = rows.get(0);
		
		CommandResponseParser commandResponseParser = new CommandResponseParser();
		commandResponseParser.setScriptName((String)row.get("SCRIPT_NAME"));
		commandResponseParser.setMetricTxnType((String)row.get("METRIC_TXN_TYPE"));
		commandResponseParser.setMetricNameSuffix((String)row.get("METRIC_NAME_SUFFIX"));
		commandResponseParser.setScript((String)row.get("SCRIPT"));
		commandResponseParser.setComment((String)row.get("COMMENT"));
		commandResponseParser.setSampleCommandResponse((String)row.get("SAMPLE_COMMAND_RESPONSE"));
		commandResponseParser.setScriptName((String)row.get("SCRIPT_NAME"));

		commandResponseParsersList.add(commandResponseParser);
//		System.out.println("ServerCommandLinksDAOjdbcTemplateImpl.findCommandResponseParser  : " + commandResponseParser.toString()  ) ;		
		
		return  commandResponseParser;
	}

	
	@Override
	public List<CommandResponseParser> findCommandResponseParsers(){
		return  findCommandResponseParsers("","");
	}

	@Override
	public List<CommandResponseParser> findCommandResponseParsers(String selectionCol, String selectionValue){

		List<CommandResponseParser> commandResponseParsersList = new ArrayList<CommandResponseParser>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(getCommandResponseParserListSelectionSQL(selectionCol, selectionValue));
		
		for (Map<String, Object> row : rows) {
			CommandResponseParser commandResponseParser = new CommandResponseParser();
			commandResponseParser.setScriptName((String)row.get("SCRIPT_NAME"));
			commandResponseParser.setMetricTxnType((String)row.get("METRIC_TXN_TYPE"));
			commandResponseParser.setMetricNameSuffix((String)row.get("METRIC_NAME_SUFFIX"));
			commandResponseParser.setScript((String)row.get("SCRIPT"));
			commandResponseParser.setComment((String)row.get("COMMENT"));
			commandResponseParser.setSampleCommandResponse((String)row.get("SAMPLE_COMMAND_RESPONSE"));			
			commandResponseParser.setScriptName((String)row.get("SCRIPT_NAME"));

			commandResponseParsersList.add(commandResponseParser);
//			System.out.println("ServerCommandLinksDAOjdbcTemplateImpl.findCommandResponseParsers  : " + commandResponseParser.toString()  ) ;		
		}	
		return commandResponseParsersList;
	}
	
	private String getCommandResponseParserListSelectionSQL(String selectionCol, String selectionValue){	
		String commandResponseParserListSelectionSQL = "select SCRIPT_NAME, METRIC_TXN_TYPE, METRIC_NAME_SUFFIX, SCRIPT, COMMENT, SAMPLE_COMMAND_RESPONSE from COMMANDRESPONSEPARSERS ";
		
		if (!selectionValue.isEmpty()  ) {			
			commandResponseParserListSelectionSQL += "  where " + selectionCol + " like '" + selectionValue + "' ";
		} 
		commandResponseParserListSelectionSQL += " order by SCRIPT_NAME ";
		return  commandResponseParserListSelectionSQL;
	}
	
	
	
	@Override
	public void insertCommandResponseParser(CommandResponseParser commandResponseParser) {
		
		String sql = "INSERT INTO COMMANDRESPONSEPARSERS ( SCRIPT_NAME, METRIC_TXN_TYPE, METRIC_NAME_SUFFIX, SCRIPT, COMMENT, SAMPLE_COMMAND_RESPONSE ) " + 
				      " VALUES (?,?,?,?,?,?)";
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.update(sql,
				new Object[] {
						commandResponseParser.getScriptName(), 			
						commandResponseParser.getMetricTxnType(),		
						commandResponseParser.getMetricNameSuffix(),		
						commandResponseParser.getScript(),		
						commandResponseParser.getComment(),		
						commandResponseParser.getSampleCommandResponse()		
				});
	}
	
	
	@Override
	public void updateCommandResponseParser(CommandResponseParser commandResponseParser){

		String sql = "UPDATE COMMANDRESPONSEPARSERS set SCRIPT_NAME = ?, METRIC_TXN_TYPE = ?, METRIC_NAME_SUFFIX = ?, SCRIPT = ?, COMMENT = ?, SAMPLE_COMMAND_RESPONSE = ? "
				+ "where SCRIPT_NAME = ? ";
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		jdbcTemplate.update(sql,
				new Object[] {
						commandResponseParser.getScriptName(), 			
						commandResponseParser.getMetricTxnType(),		
						commandResponseParser.getMetricNameSuffix(),		
						commandResponseParser.getScript(),		
						commandResponseParser.getComment(),	
						commandResponseParser.getSampleCommandResponse(),							
						commandResponseParser.getScriptName() 				
				});
	}	
	
	
	@Override
	public void deleteCommandResponseParser(String scriptName) {
		String sql = "delete from COMMANDPARSERLINKS where SCRIPT_NAME ='" + scriptName	+ "'";
		System.out.println("delete commandResponseParser sql: " + sql);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);		
		
		sql = "delete from COMMANDRESPONSEPARSERS where SCRIPT_NAME ='" + scriptName	+ "'";
		System.out.println("delete commandResponseParser sql: " + sql);
		jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
	}	

}
