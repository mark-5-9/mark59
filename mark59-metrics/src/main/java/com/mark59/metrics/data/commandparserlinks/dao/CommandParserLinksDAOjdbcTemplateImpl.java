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
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.mark59.metrics.data.beans.CommandParserLink;

/**
 * @author Philip Webb
 * Written: Australian Summer 2020  
 */
public class CommandParserLinksDAOjdbcTemplateImpl implements CommandParserLinksDAO 
{
	
	@Autowired  
	private DataSource dataSource;
		

	@Override
	public CommandParserLink findCommandParserLink(String commandName, String parserName){

		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		String selectServerSQL   = "select COMMAND_NAME, PARSER_NAME from COMMANDPARSERLINKS"
				+ " where COMMAND_NAME = '" + commandName + "'"
				+ "   and PARSER_NAME = '"  + parserName  + "'"
				+ " order by COMMAND_NAME;";
		
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectServerSQL);
		
		if (rows.size() == 0 ){
			return null;
		}
		Map<String, Object> row = rows.get(0);
		CommandParserLink commandParserLink = new CommandParserLink();
		commandParserLink.setCommandName((String)row.get("COMMAND_NAME"));
		commandParserLink.setParserName((String)row.get("PARSER_NAME"));
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

		String sql = "SELECT COMMAND_NAME, PARSER_NAME FROM COMMANDPARSERLINKS ";
		
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
			commandParserLink.setParserName((String)row.get("PARSER_NAME"));	
			commandParserLinkList.add(commandParserLink);
		}	
		return commandParserLinkList;
	}
	
	
	@Override
	public void insertCommandParserLink(CommandParserLink commandParserLink) {
		String sql = "INSERT INTO COMMANDPARSERLINKS ( COMMAND_NAME, PARSER_NAME ) " + 
				      " VALUES (?,?)";
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.update(sql,
				commandParserLink.getCommandName(),
				commandParserLink.getParserName());
	}

	
	
	@Override
	public void updateCommandParserLinksForCommandName(String commandName, List<String> parserNames) {
		deleteCommandParserLinksForCommandName(commandName);
		for (String parserName : parserNames) {
			CommandParserLink commandParserLink = new CommandParserLink();
			commandParserLink.setCommandName(commandName);
			commandParserLink.setParserName(parserName);
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
				+ "   and PARSER_NAME = :parserName ";

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("commandName", commandParserLink.getCommandName())
				.addValue("parserName", commandParserLink.getParserName());		

		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
	}

	@Override
	public void deleteCommandParserLinksForParserName(String parserName) {
		
		String sql = "delete from COMMANDPARSERLINKS where PARSER_NAME = :parserName ";
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("parserName", parserName);		

		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
	}


}
