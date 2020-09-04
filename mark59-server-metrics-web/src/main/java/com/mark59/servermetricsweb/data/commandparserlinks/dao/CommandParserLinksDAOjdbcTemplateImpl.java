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
	@SuppressWarnings("rawtypes")
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
		Map row = rows.get(0);
		
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

		List<CommandParserLink> commandParserLinkList = new ArrayList<CommandParserLink>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(getCommandParserLinkListSelectionSQL(selectionCol, selectionValue));
		
		for (Map<String, Object> row : rows) {
			CommandParserLink commandParserLink = new CommandParserLink();
			commandParserLink.setCommandName((String)row.get("COMMAND_NAME"));
			commandParserLink.setScriptName((String)row.get("SCRIPT_NAME"));	
			commandParserLinkList.add(commandParserLink);
		}	
		return commandParserLinkList;
	}
	
	private String getCommandParserLinkListSelectionSQL(String selectionCol, String selectionValue){	
		String commandListSelectionSQL = "SELECT COMMAND_NAME, SCRIPT_NAME FROM COMMANDPARSERLINKS ";
		
		if (!selectionValue.isEmpty()  ) {			
			commandListSelectionSQL += "  where " + selectionCol + " like '" + selectionValue + "' ";
		} 
		commandListSelectionSQL += " order by COMMAND_NAME ";
		return  commandListSelectionSQL;
	}
	
	
	
	@Override
	public void insertCommandParserLink(CommandParserLink commandParserLink) {
		String sql = "INSERT INTO COMMANDPARSERLINKS ( COMMAND_NAME, SCRIPT_NAME ) " + 
				      " VALUES (?,?)";
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.update(sql,
				new Object[] {
						commandParserLink.getCommandName(), 			
						commandParserLink.getScriptName() 			
				});
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
		String sql = "delete from COMMANDPARSERLINKS "
				+ " where COMMAND_NAME = '" + commandName + "'";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
	}

	
	@Override
	public void deleteCommandParserLink(CommandParserLink commandParserLink) {
		String sql = "delete from COMMANDPARSERLINKS "
				+ " where COMMAND_NAME = '" + commandParserLink.getCommandName() + "'"
				+ "   and SCRIPT_NAME = '"  + commandParserLink.getScriptName()  + "'";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
	}

	@Override
	public void deleteCommandParserLinksForScriptName(String scriptName) {
		String sql = "delete from COMMANDPARSERLINKS "
				+ " where SCRIPT_NAME = '" + scriptName + "'";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
		
	}


}
