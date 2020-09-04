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

package com.mark59.servermetricsweb.data.commands.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mark59.servermetricsweb.data.beans.Command;

/**
 * @author Philip Webb
 * Written: Australian Summer 2020  
 */
public class CommandsDAOjdbcTemplateImpl implements CommandsDAO 
{
	
	@Autowired  
	private DataSource dataSource;

		

	@Override
	@SuppressWarnings("rawtypes")
	public Command findCommand(String commandName){

		List<Command> commandList = new ArrayList<Command>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		String selectServerSQL   = "select COMMAND_NAME, EXECUTOR, COMMAND, IGNORE_STDERR, COMMENT "
				+ "from COMMANDS where COMMAND_NAME = '" + commandName + "'"
				+ " order by COMMAND_NAME asc;";
		
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectServerSQL);
		
		if (rows.size() == 0 ){
			return null;
		}
		Map row = rows.get(0);
		
		Command command = new Command();
		command.setCommandName((String)row.get("COMMAND_NAME"));
		command.setExecutor((String)row.get("EXECUTOR"));
		command.setCommand((String)row.get("COMMAND"));
		command.setIngoreStderr((String)row.get("IGNORE_STDERR"));
		command.setComment((String)row.get("COMMENT"));
		commandList.add(command);
		return  command;
	}

	
	@Override
	public List<Command> findCommands(){
		return  findCommands("","");
	}

	@Override
	public List<Command> findCommands(String selectionCol, String selectionValue){

		List<Command> commandList = new ArrayList<Command>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(getCommandListSelectionSQL(selectionCol, selectionValue));
		
		for (Map<String, Object> row : rows) {
			Command command = new Command();
			command.setCommandName((String)row.get("COMMAND_NAME"));
			command.setExecutor((String)row.get("EXECUTOR"));
			command.setCommand((String)row.get("COMMAND"));
			command.setIngoreStderr((String)row.get("IGNORE_STDERR"));			
			command.setComment((String)row.get("COMMENT"));
			commandList.add(command);
//			System.out.println("CommandsDAOjdbcTemplateImpl.findCommands  : " + command.toString()  ) ;		
		}	
		return commandList;
	}
	
	private String getCommandListSelectionSQL(String selectionCol, String selectionValue){	
		String commandListSelectionSQL = "select COMMAND_NAME, EXECUTOR, COMMAND, IGNORE_STDERR, COMMENT from COMMANDS ";
		
		if (!selectionValue.isEmpty()  ) {			
			commandListSelectionSQL += "  where " + selectionCol + " like '" + selectionValue + "' ";
		} 
		commandListSelectionSQL += " order by COMMAND_NAME ";
		return  commandListSelectionSQL;
	}
	
	
	
	@Override
	public void insertCommand(Command command) {
		
		String sql = "INSERT INTO COMMANDS ( COMMAND_NAME, EXECUTOR, COMMAND, IGNORE_STDERR, COMMENT ) " + 
				      " VALUES (?,?,?,?,?)";
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.update(sql,
				new Object[] {
						command.getCommandName(), 			
						command.getExecutor(), 			
						command.getCommand(), 			
						command.getIngoreStderr(), 			
						command.getComment() 			
				});
	}
	
	
	@Override
	public void updateCommand(Command command){

		String sql = "UPDATE COMMANDS set EXECUTOR = ?, COMMAND = ?, IGNORE_STDERR = ?, COMMENT = ? "
				+ "where COMMAND_NAME = ? ";
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		jdbcTemplate.update(sql,
				new Object[] {
						command.getExecutor(), 			
						command.getCommand(), 			
						command.getIngoreStderr(), 							
						command.getComment(),
						command.getCommandName() 							
				});
//		System.out.println("CommandsDAOjdbcTemplateImpl.findupdateCommand  : " + command.toString()  ) ;		
	}	
	
	
	@Override
	public void deleteCommand(String commandName) {
		
		String sql = "delete from SERVERCOMMANDLINKS where COMMAND_NAME ='" + commandName	+ "'";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);		
				
		sql = "delete from COMMANDPARSERLINKS where COMMAND_NAME ='" + commandName	+ "'";
		jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);		
		
		sql = "delete from COMMANDS where COMMAND_NAME ='" + commandName	+ "'";
		System.out.println("delete deleteCommand sql: " + sql);
		jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
	}	

}
