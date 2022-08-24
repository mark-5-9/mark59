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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.mark59.metrics.data.beans.Command;

/**
 * @author Philip Webb
 * Written: Australian Summer 2020  
 */
public class CommandsDAOjdbcTemplateImpl implements CommandsDAO 
{
	
	@Autowired  
	private DataSource dataSource;
		

	@Override
	public Command findCommand(String commandName){

		String sql   = "select COMMAND_NAME, EXECUTOR, COMMAND, IGNORE_STDERR, COMMENT, PARAM_NAMES "
				+ "from COMMANDS where COMMAND_NAME = :commandName "
				+ " order by COMMAND_NAME asc;";

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("commandName", commandName);

//		System.out.println(" findCommand : " + sql + " : " + commandName);
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sqlparameters);
		
		if (rows.size() == 0 ){
			return null;
		}
		Map<String, Object> row = rows.get(0);
		
		Command command = new Command();
		command.setCommandName((String)row.get("COMMAND_NAME"));
		command.setExecutor((String)row.get("EXECUTOR"));
		command.setCommand((String)row.get("COMMAND"));
		command.setIngoreStderr((String)row.get("IGNORE_STDERR"));
		command.setComment((String)row.get("COMMENT"));
		command.setParamNames(deserializeJsonToList((String)row.get("PARAM_NAMES")));
		return  command;
	}

	
	@Override
	public List<Command> findCommands(){
		return  findCommands("","");
	}

	
	@Override
	public List<Command> findCommands(String selectionCol, String selectionValue){
		
		String sql = "select COMMAND_NAME, EXECUTOR, COMMAND, IGNORE_STDERR, COMMENT, PARAM_NAMES from COMMANDS ";
		
		if (!selectionValue.isEmpty()  ) {			
			sql += "  where " + selectionCol + " like :selectionValue ";
		} 
		sql += " order by COMMAND_NAME ";		
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("selectionValue", selectionValue);

//		System.out.println(" findCommands : " + sql + Mark59Utils.prettyPrintMap(sqlparameters.getValues()));
		List<Command> commandList = new ArrayList<>();
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sqlparameters);
		
		for (Map<String, Object> row : rows) {
			Command command = new Command();
			command.setCommandName((String)row.get("COMMAND_NAME"));
			command.setExecutor((String)row.get("EXECUTOR"));
			command.setCommand((String)row.get("COMMAND"));
			command.setIngoreStderr((String)row.get("IGNORE_STDERR"));			
			command.setComment((String)row.get("COMMENT"));
			command.setParamNames(deserializeJsonToList((String)row.get("PARAM_NAMES")));	
			commandList.add(command);
//			System.out.println("CommandsDAOjdbcTemplateImpl.findCommands  : " + command.toString()  ) ;		
		}	
		return commandList;
	}
	
	
	@Override
	public void insertCommand(Command command) {
		
		String sql = "INSERT INTO COMMANDS ( COMMAND_NAME, EXECUTOR, COMMAND, IGNORE_STDERR, COMMENT, PARAM_NAMES ) " + 
				      " VALUES (?,?,?,?,?,?)";
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.update(sql,
				command.getCommandName(),
				command.getExecutor(),
				command.getCommand(),
				command.getIngoreStderr(),
				command.getComment(),
				serializeListToJson(command.getParamNames()));
	}
	
	
	@Override
	public void updateCommand(Command command){
//		System.out.println(">> CommandsDAOjdbcTemplateImpl.updateCommand  : " + command.toString()  ) ;		
		
		String sql = "UPDATE COMMANDS set EXECUTOR = ?, COMMAND = ?, IGNORE_STDERR = ?, COMMENT = ?, PARAM_NAMES = ? "
				+ "where COMMAND_NAME = ? ";
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		jdbcTemplate.update(sql,
				command.getExecutor(),
				command.getCommand(),
				command.getIngoreStderr(),
				command.getComment(),
				serializeListToJson(command.getParamNames()),
				command.getCommandName());
	}	
	
	
	@Override
	public void deleteCommand(String commandName) {
		
		String sql = "delete from SERVERCOMMANDLINKS where COMMAND_NAME = :commandName ";

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("commandName", commandName);		

		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
						
		sql = "delete from COMMANDPARSERLINKS where COMMAND_NAME = :commandName ";
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
				
		sql = "delete from COMMANDS where COMMAND_NAME = :commandName ";
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);

	}	
	
}
