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

package com.mark59.servermetricsweb.data.commandparserlinks.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.mark59.servermetricsweb.data.beans.CommandParserLink;

/**
 * @author Philip Webb
 * Written: Australian Summer 2020  
 */
public class CommandParserLinksDAOjdbcTemplateImpl implements CommandParserLinksDAO 
{
	
	@Autowired  
	private DataSource dataSource;
		

	@Override
	public CommandParserLink findCommandParserLink(String commandName, String scriptName){

		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		String selectServerSQL   = "select COMMAND_NAME, SCRIPT_NAME from COMMANDPARSERLINKS"
				+ " where COMMAND_NAME = '" + commandName + "'"
				+ "   and SCRIPT_NAME = '"  + scriptName  + "'"
				+ " order by COMMAND_NAME;";
		
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectServerSQL);
		
		if (rows.size() == 0 ){
			return null;
		}
		Map<String, Object> row = rows.get(0);
		CommandParserLink commandParserLink = new CommandParserLink();
		commandParserLink.setCommandName((String)row.get("COMMAND_NAME"));
		commandParserLink.setScriptName((String)row.get("SCRIPT_NAME"));
		return  commandParserLink;
	}

	
	@Override
	public List<CommandParserLink> findCommandParserLinks(){
		return  findCommandParserLinks("","");
	}

	
	@Override
	public List<CommandParserLink> findCommandParserLinksForCommand(String commandName){
		return  findCommandParserLinks("COMMAND_NAME", commandName);
	}
	
	
	@Override
	public List<CommandParserLink> findCommandParserLinks(String selectionCol, String selectionValue){

		String sql = "SELECT COMMAND_NAME, SCRIPT_NAME FROM COMMANDPARSERLINKS ";
		
		if (!selectionValue.isEmpty()  ) {			
			sql += "  where " + selectionCol + " like :selectionValue ";
		} 
		sql += " order by COMMAND_NAME ";

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("selectionValue", selectionValue);

//		System.out.println(" ..CommandParserLinks: "+sql+Mark59Utils.prettyPrintMap(sqlparameters.getValues()));
		List<CommandParserLink> commandParserLinkList = new ArrayList<>();
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sqlparameters);
		
		for (Map<String, Object> row : rows) {
			CommandParserLink commandParserLink = new CommandParserLink();
			commandParserLink.setCommandName((String)row.get("COMMAND_NAME"));
			commandParserLink.setScriptName((String)row.get("SCRIPT_NAME"));	
			commandParserLinkList.add(commandParserLink);
		}	
		return commandParserLinkList;
	}
	
	
	@Override
	public void insertCommandParserLink(CommandParserLink commandParserLink) {
		String sql = "INSERT INTO COMMANDPARSERLINKS ( COMMAND_NAME, SCRIPT_NAME ) " + 
				      " VALUES (?,?)";
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.update(sql,
				commandParserLink.getCommandName(),
				commandParserLink.getScriptName());
	}

	
	
	@Override
	public void updateCommandParserLinksForCommandName(String commandName, List<String> scriptNames) {
		deleteCommandParserLinksForCommandName(commandName);
		for (String scriptName : scriptNames) {
			CommandParserLink commandParserLink = new CommandParserLink();
			commandParserLink.setCommandName(commandName);
			commandParserLink.setScriptName(scriptName);
			insertCommandParserLink(commandParserLink);
		}
	}	

	
	@Override
	public void deleteCommandParserLinksForCommandName(String commandName) {
		
		String sql = "delete from COMMANDPARSERLINKS where COMMAND_NAME = :commandName ";

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("commandName", commandName);		

		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
	}

	
	@Override
	public void deleteCommandParserLink(CommandParserLink commandParserLink) {
		String sql = "delete from COMMANDPARSERLINKS "
				+ " where COMMAND_NAME = :commandName "
				+ "   and SCRIPT_NAME = :scriptName ";

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("commandName", commandParserLink.getCommandName())
				.addValue("scriptName", commandParserLink.getScriptName());		

		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
	}

	@Override
	public void deleteCommandParserLinksForScriptName(String scriptName) {
		
		String sql = "delete from COMMANDPARSERLINKS where SCRIPT_NAME = :scriptName ";
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("scriptName", scriptName);		

		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
	}


}
