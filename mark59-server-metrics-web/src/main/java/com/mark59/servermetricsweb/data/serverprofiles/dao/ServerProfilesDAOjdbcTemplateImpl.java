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

package com.mark59.servermetricsweb.data.serverprofiles.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mark59.servermetricsweb.data.beans.ServerProfile;

/**
 * @author Philip Webb
 * Written: Australian Summer 2020  
 */
public class ServerProfilesDAOjdbcTemplateImpl implements ServerProfilesDAO 
{
	
	@Autowired  
	private DataSource dataSource;

		

	@Override
	@SuppressWarnings("rawtypes")
	public ServerProfile  findServerProfile(String serverProfileName){

		List<ServerProfile> serversList = new ArrayList<ServerProfile>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		String selectServerSQL   = "select SERVER, ALTERNATE_SERVER_ID, USERNAME, PASSWORD, PASSWORD_CIPHER, OPERATING_SYSTEM, CONNECTION_PORT, CONNECTION_TIMEOUT, COMMENT "
				+ "from SERVERPROFILES where SERVER_PROFILE_NAME = '" + serverProfileName + "'"
				+ " order by SERVER_PROFILE_NAME ";
		
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectServerSQL);
		
		if (rows.size() == 0 ){
			return null;
		}
		Map row = rows.get(0);
		
		ServerProfile server = new ServerProfile();
		server.setServerProfileName(serverProfileName); 
		server.setServer((String)row.get("SERVER")); 
		server.setAlternativeServerId((String)row.get("ALTERNATE_SERVER_ID")); 		
		server.setUsername((String)row.get("USERNAME")); 		
		server.setPassword((String)row.get("PASSWORD")); 		
		server.setPasswordCipher((String)row.get("PASSWORD_CIPHER")); 		
		server.setOperatingSystem((String)row.get("OPERATING_SYSTEM")); 		
		server.setConnectionPort((String)row.get("CONNECTION_PORT")); 		
		server.setConnectionTimeout((String)row.get("CONNECTION_TIMEOUT")); 		
		server.setComment((String)row.get("COMMENT")); 		
		serversList.add(server);
//		System.out.println("ServerProfilesDAOjdbcTemplateImpl.findServerProfile  : " + serverProfileName.toString()  ) ;		
		
		return  server;
	}

	
	@Override
	public List<ServerProfile> findServerProfiles(){
		return  findServerProfiles("","");
	}

	@Override
	public List<ServerProfile> findServerProfiles(String selectionCol, String selectionValue){

		List<ServerProfile> serversList = new ArrayList<ServerProfile>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(getServersListSelectionSQL(selectionCol, selectionValue));
		
		for (Map<String, Object> row : rows) {
			ServerProfile server = new ServerProfile();
			server.setServerProfileName((String)row.get("SERVER_PROFILE_NAME")); 
			server.setServer((String)row.get("SERVER")); 
			server.setAlternativeServerId((String)row.get("ALTERNATE_SERVER_ID")); 		
			server.setUsername((String)row.get("USERNAME")); 		
			server.setPassword((String)row.get("PASSWORD")); 		
			server.setPasswordCipher((String)row.get("PASSWORD_CIPHER")); 		
			server.setOperatingSystem((String)row.get("OPERATING_SYSTEM")); 		
			server.setConnectionPort((String)row.get("CONNECTION_PORT")); 		
			server.setConnectionTimeout((String)row.get("CONNECTION_TIMEOUT")); 
			server.setComment((String)row.get("COMMENT")); 				
			serversList.add(server);
		}	
		return  serversList;
	}
	
	private String getServersListSelectionSQL(String selectionCol, String selectionValue){	
		String serversListSelectionSQL = "select SERVER_PROFILE_NAME, SERVER, ALTERNATE_SERVER_ID, USERNAME, PASSWORD, PASSWORD_CIPHER, OPERATING_SYSTEM, CONNECTION_PORT, CONNECTION_TIMEOUT, COMMENT from SERVERPROFILES ";
		
		if (!selectionValue.isEmpty()  ) {			
			serversListSelectionSQL += "  where " + selectionCol + " like '" + selectionValue + "' ";
		} 
		serversListSelectionSQL += " order by SERVER_PROFILE_NAME ";
		return  serversListSelectionSQL;
	}
	
	
	
	@Override
	public void insertServerProfile(ServerProfile serverProfile) {
		
		String sql = "INSERT INTO SERVERPROFILES (SERVER_PROFILE_NAME, SERVER, ALTERNATE_SERVER_ID, USERNAME, PASSWORD, PASSWORD_CIPHER, OPERATING_SYSTEM, CONNECTION_PORT, CONNECTION_TIMEOUT, COMMENT ) " + 
				      " VALUES (?,?,?,?,?,?,?,?,?,? )";
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.update(sql,
				new Object[] {
						serverProfile.getServerProfileName(), 			
						serverProfile.getServer(), 			
						serverProfile.getAlternativeServerId(), 		
						serverProfile.getUsername(), 		
						serverProfile.getPassword(), 		
						serverProfile.getPasswordCipher(), 		
						serverProfile.getOperatingSystem(), 		
						serverProfile.getConnectionPort(), 		
						serverProfile.getConnectionTimeout(), 		
						serverProfile.getComment() 		
				});
	}
	
	
	@Override
	public void updateServerProfile(ServerProfile serverProfile){

		String sql = "UPDATE SERVERPROFILES set SERVER = ?, ALTERNATE_SERVER_ID = ?, USERNAME = ?, PASSWORD = ?, PASSWORD_CIPHER = ?,"
				+ " OPERATING_SYSTEM = ?, CONNECTION_PORT = ?, CONNECTION_TIMEOUT = ?, COMMENT = ? "
				+ "where SERVER_PROFILE_NAME = ? ";
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		jdbcTemplate.update(sql,
				new Object[] {
						serverProfile.getServer(), 			
						serverProfile.getAlternativeServerId(), 		
						serverProfile.getUsername(), 		
						serverProfile.getPassword(), 		
						serverProfile.getPasswordCipher(), 		
						serverProfile.getOperatingSystem(), 		
						serverProfile.getConnectionPort(), 		
						serverProfile.getConnectionTimeout(), 		
						serverProfile.getComment(), 		
						serverProfile.getServerProfileName() 						
				});
	}	
	
	
	@Override
	public void deleteServerProfile(String serverProfileName) {
		
		String sql = "delete from SERVERCOMMANDLINKS where SERVER_PROFILE_NAME ='" + serverProfileName	+ "'";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);		
		
		sql = "delete from SERVERPROFILES where SERVER_PROFILE_NAME ='" + serverProfileName	+ "'";
		jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
	}	

}
