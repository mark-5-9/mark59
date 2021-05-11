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

package com.mark59.servermetricsweb.data.servercommandlinks.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mark59.servermetricsweb.data.beans.ServerCommandLink;

/**
 * @author Philip Webb
 * Written: Australian Summer 2020  
 */
public class ServerCommandLinksDAOjdbcTemplateImpl implements ServerCommandLinksDAO 
{
	
	@Autowired  
	private DataSource dataSource;

		

	@Override
	@SuppressWarnings("rawtypes")
	public ServerCommandLink findServerCommandLink(String commandName, String serverProfileName){

		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		String selectServerSQL   = "select SERVER_PROFILE_NAME, COMMAND_NAME  from SERVERCOMMANDLINKS"
				+ " where SERVER_PROFILE_NAME = '"  + serverProfileName  + "'"
				+ "   and COMMAND_NAME = '" + commandName + "'"
				+ " order by SERVER_PROFILE_NAME;";
		
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectServerSQL);
		
		if (rows.size() == 0 ){
			return null;
		}
		Map row = rows.get(0);
		
		ServerCommandLink serverCommandLink = new ServerCommandLink();
		serverCommandLink.setServerProfileName((String)row.get("SERVER_PROFILE_NAME"));
		serverCommandLink.setCommandName((String)row.get("COMMAND_NAME"));
		return  serverCommandLink;
	}

	
	@Override
	public List<ServerCommandLink> findServerCommandLinks(){
		return  findServerCommandLinks("","");
	}
	
	
	@Override
	public List<ServerCommandLink> findServerCommandLinksForServerProfile(String serverProfileName){
		return  findServerCommandLinks("SERVER_PROFILE_NAME", serverProfileName);
	}


	@Override
	public List<ServerCommandLink> findServerCommandLinks(String selectionCol, String selectionValue){

		List<ServerCommandLink> serverCommandLinkList = new ArrayList<ServerCommandLink>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(getServerCommandLinkListSelectionSQL(selectionCol, selectionValue));
		
		for (Map<String, Object> row : rows) {
			ServerCommandLink serverCommandLink = new ServerCommandLink();
			serverCommandLink.setServerProfileName((String)row.get("SERVER_PROFILE_NAME"));	
			serverCommandLink.setCommandName((String)row.get("COMMAND_NAME"));
			serverCommandLinkList.add(serverCommandLink);
		}
		return serverCommandLinkList;
	}
	
	
	private String getServerCommandLinkListSelectionSQL(String selectionCol, String selectionValue){	
		String commandListSelectionSQL = "select SERVER_PROFILE_NAME, COMMAND_NAME from SERVERCOMMANDLINKS ";
		
		if (!selectionValue.isEmpty()  ) {			
			commandListSelectionSQL += "  where " + selectionCol + " like '" + selectionValue + "' ";
		} 
		commandListSelectionSQL += " order by SERVER_PROFILE_NAME ";
		return  commandListSelectionSQL;
	}
	
	
	
	@Override
	public void insertServerCommandLink(ServerCommandLink serverCommandLink) {
		String sql = "INSERT INTO SERVERCOMMANDLINKS ( SERVER_PROFILE_NAME, COMMAND_NAME) " + 
				      " VALUES (?,?)";
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.update(sql,
				new Object[] {
						serverCommandLink.getServerProfileName(), 			
						serverCommandLink.getCommandName() 			
				});
	}

	
	
	@Override
	public void updateServerCommandLinksForServerProfileName(String serverProfileName, List<String> commandNames) {  
		deleteServerCommandLinksForServerProfile(serverProfileName);
		if (commandNames != null) {
			commandNames.removeAll(Arrays.asList("", null));  // don't update with blanks (like the empty selector option) 
			for (String commandName : commandNames) {
				ServerCommandLink serverCommandLink = new ServerCommandLink();
				serverCommandLink.setServerProfileName(serverProfileName);
				serverCommandLink.setCommandName(commandName);
				insertServerCommandLink(serverCommandLink);
			}
		}
	}	

	
	@Override
	public void deleteServerCommandLinksForCommandName(String commandName) {
		String sql = "delete from SERVERCOMMANDLINKS "
				+ " where COMMAND_NAME = '" + commandName + "'";
		System.out.println("delete deleteCommand sql: " + sql);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
	}

	
	@Override
	public void deleteServerCommandLink(ServerCommandLink serverCommandLink) {
		String sql = "delete from SERVERCOMMANDLINKS "
				+ " where COMMAND_NAME = '" + serverCommandLink.getCommandName() + "'"
				+ "   and SERVER_PROFILE_NAME = '"  + serverCommandLink.getServerProfileName()  + "'";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
	}

	@Override
	public void deleteServerCommandLinksForServerProfile(String serverProfileName) {
		String sql = "delete from SERVERCOMMANDLINKS "
				+ " where SERVER_PROFILE_NAME = '" + serverProfileName + "'";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
		
	}


}
