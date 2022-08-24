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

package com.mark59.metrics.data.servercommandlinks.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.mark59.metrics.data.beans.ServerCommandLink;

/**
 * @author Philip Webb
 * Written: Australian Summer 2020  
 */
public class ServerCommandLinksDAOjdbcTemplateImpl implements ServerCommandLinksDAO 
{
	
	@Autowired  
	private DataSource dataSource;


	@Override
	public ServerCommandLink findServerCommandLink(String commandName, String serverProfileName){
		
		String selectServerSQL   = "select SERVER_PROFILE_NAME, COMMAND_NAME  from SERVERCOMMANDLINKS"
				+ " where SERVER_PROFILE_NAME = :serverProfileName "
				+ "   and COMMAND_NAME = :commandName "
				+ " order by SERVER_PROFILE_NAME;";
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("serverProfileName", serverProfileName)
				.addValue("commandName", commandName);

//		System.out.println(" findServerCommandLink : " + selectServerSQL + " : " + serverProfileName);
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectServerSQL, sqlparameters);
		
		if (rows.size() == 0 ){
			return null;
		}
		Map<String, Object> row = rows.get(0);
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

		String sql = "select SERVER_PROFILE_NAME, COMMAND_NAME from SERVERCOMMANDLINKS ";
		
		if (!selectionValue.isEmpty()  ) {			
			sql += "  where " + selectionCol + " like :selectionValue ";
		} 
		sql += " order by SERVER_PROFILE_NAME ";
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("selectionValue", selectionValue);

//		System.out.println(" findServerProfiles : " + sql + Mark59Utils.prettyPrintMap(sqlparameters.getValues()));
		List<ServerCommandLink> serverCommandLinkList = new ArrayList<>();
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sqlparameters);
		
		for (Map<String, Object> row : rows) {
			ServerCommandLink serverCommandLink = new ServerCommandLink();
			serverCommandLink.setServerProfileName((String)row.get("SERVER_PROFILE_NAME"));	
			serverCommandLink.setCommandName((String)row.get("COMMAND_NAME"));
			serverCommandLinkList.add(serverCommandLink);
		}
		return serverCommandLinkList;
	}
	
	
	@Override
	public void insertServerCommandLink(ServerCommandLink serverCommandLink) {
		String sql = "INSERT INTO SERVERCOMMANDLINKS ( SERVER_PROFILE_NAME, COMMAND_NAME) " + 
				      " VALUES (?,?)";
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.update(sql,
				serverCommandLink.getServerProfileName(),
				serverCommandLink.getCommandName());
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
		
		String sql = "delete from SERVERCOMMANDLINKS  where COMMAND_NAME = :commandName ";

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("commandName", commandName);		

		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
	}

	
	@Override
	public void deleteServerCommandLink(ServerCommandLink serverCommandLink) {

		String sql = "delete from SERVERCOMMANDLINKS "
				+ " where COMMAND_NAME = :commandName "
				+ "   and SERVER_PROFILE_NAME = :serverProfileName ";

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("commandName", serverCommandLink.getCommandName())
				.addValue("serverProfileName", serverCommandLink.getServerProfileName());		

		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
	}

	
	@Override
	public void deleteServerCommandLinksForServerProfile(String serverProfileName) {
		
		String sql = "delete from SERVERCOMMANDLINKS where SERVER_PROFILE_NAME = :serverProfileName";
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("serverProfileName", serverProfileName);		

		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
	}

}
