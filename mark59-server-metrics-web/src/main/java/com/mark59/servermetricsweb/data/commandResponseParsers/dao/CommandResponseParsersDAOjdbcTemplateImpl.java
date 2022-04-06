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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

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
	public CommandResponseParser findCommandResponseParser(String scriptName){

		String sql = "select SCRIPT_NAME, METRIC_TXN_TYPE, METRIC_NAME_SUFFIX, SCRIPT, COMMENT, SAMPLE_COMMAND_RESPONSE "
				+ "from COMMANDRESPONSEPARSERS where SCRIPT_NAME = :scriptName "
				+ " order by SCRIPT_NAME asc;";
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("scriptName", scriptName);

//		System.out.println(" findCommandResponseParser : " + sql + " : " + scriptName);
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sqlparameters);
		
		if (rows.size() == 0 ){
			return null;
		}
		Map<String, Object> row = rows.get(0);
		
		CommandResponseParser commandResponseParser = new CommandResponseParser();
		commandResponseParser.setScriptName((String)row.get("SCRIPT_NAME"));
		commandResponseParser.setMetricTxnType((String)row.get("METRIC_TXN_TYPE"));
		commandResponseParser.setMetricNameSuffix((String)row.get("METRIC_NAME_SUFFIX"));
		commandResponseParser.setScript((String)row.get("SCRIPT"));
		commandResponseParser.setComment((String)row.get("COMMENT"));
		commandResponseParser.setSampleCommandResponse((String)row.get("SAMPLE_COMMAND_RESPONSE"));
		commandResponseParser.setScriptName((String)row.get("SCRIPT_NAME"));
//		System.out.println("ServerCommandLinksDAO..findCommandResponseParser : " + commandResponseParser.toString());		
		return  commandResponseParser;
	}

	
	@Override
	public List<CommandResponseParser> findCommandResponseParsers(){
		return  findCommandResponseParsers("","");
	}

	@Override
	public List<CommandResponseParser> findCommandResponseParsers(String selectionCol, String selectionValue){

		String sql = "select SCRIPT_NAME, METRIC_TXN_TYPE, METRIC_NAME_SUFFIX, SCRIPT, COMMENT, SAMPLE_COMMAND_RESPONSE from COMMANDRESPONSEPARSERS ";
		
		if (!selectionValue.isEmpty()  ) {			
			sql += "  where " + selectionCol + " like :selectionValue ";
		} 
		sql += " order by SCRIPT_NAME ";		

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("selectionValue", selectionValue);

//		System.out.println(" findCommandResponseParsers : " + sql + Mark59Utils.prettyPrintMap(sqlparameters.getValues()));
		List<CommandResponseParser> commandResponseParsersList = new ArrayList<>();
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sqlparameters);
		
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

	
	@Override
	public void insertCommandResponseParser(CommandResponseParser commandResponseParser) {
		
		String sql = "INSERT INTO COMMANDRESPONSEPARSERS ( SCRIPT_NAME, METRIC_TXN_TYPE, METRIC_NAME_SUFFIX, SCRIPT, COMMENT, SAMPLE_COMMAND_RESPONSE ) " + 
				      " VALUES (?,?,?,?,?,?)";
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.update(sql,
				commandResponseParser.getScriptName(),
				commandResponseParser.getMetricTxnType(),
				commandResponseParser.getMetricNameSuffix(),
				commandResponseParser.getScript(),
				commandResponseParser.getComment(),
				commandResponseParser.getSampleCommandResponse());
	}
	
	
	@Override
	public void updateCommandResponseParser(CommandResponseParser commandResponseParser){

		String sql = "UPDATE COMMANDRESPONSEPARSERS set SCRIPT_NAME = ?, METRIC_TXN_TYPE = ?, METRIC_NAME_SUFFIX = ?, SCRIPT = ?, COMMENT = ?, SAMPLE_COMMAND_RESPONSE = ? "
				+ "where SCRIPT_NAME = ? ";
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		jdbcTemplate.update(sql,
				commandResponseParser.getScriptName(),
				commandResponseParser.getMetricTxnType(),
				commandResponseParser.getMetricNameSuffix(),
				commandResponseParser.getScript(),
				commandResponseParser.getComment(),
				commandResponseParser.getSampleCommandResponse(),
				commandResponseParser.getScriptName());
	}	
	
	
	@Override
	public void deleteCommandResponseParser(String scriptName) {

		String sql = "delete from COMMANDPARSERLINKS where SCRIPT_NAME = :scriptName ";

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("scriptName", scriptName);		

		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
		
		sql = "delete from COMMANDRESPONSEPARSERS where SCRIPT_NAME = :scriptName ";
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
	}	

}
