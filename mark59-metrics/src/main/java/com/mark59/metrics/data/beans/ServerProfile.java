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

package com.mark59.metrics.data.beans;

import java.util.Map;

/**
 * @author Philip Webb
 * Written: Australian Spring 2020
 */
public class ServerProfile {

	String	serverProfileName;
	String  executor;
	String	server;
	String	alternativeServerId;	
	String	username;
	String	password;	
	String	passwordCipher;	
	String	connectionPort;
	String	connectionTimeout;
	String	comment;
	Map<String,String> parameters;
	
	public ServerProfile() {
	}

	public String getServerProfileName() {
		return serverProfileName;
	}

	public void setServerProfileName(String serverProfileName) {
		this.serverProfileName = serverProfileName;
	}
	
	public String getExecutor() {
		return executor;
	}

	public void setExecutor(String executor) {
		this.executor = executor;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getAlternativeServerId() {
		return alternativeServerId;
	}

	public void setAlternativeServerId(String alternativeServerId) {
		this.alternativeServerId = alternativeServerId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordCipher() {
		return passwordCipher;
	}

	public void setPasswordCipher(String passwordCipher) {
		this.passwordCipher = passwordCipher;
	}


	public String getConnectionPort() {
		return connectionPort;
	}

	public void setConnectionPort(String connectionPort) {
		this.connectionPort = connectionPort;
	}

	public String getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(String connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	@Override
    public String toString() {
        return   "[serverProfileName="+ serverProfileName + 
        		", executor="+ executor + 
        		", server="+ server + 
        		", alternativeServerId="+ alternativeServerId + 
        		", username="+ username + 
//        		", password="+ password + 
//        		", passwordCipher="+ passwordCipher + 
        		", connectionPort="+ connectionPort  +
        		", connectionTimeout="+ connectionTimeout  +
        		", comment="+ comment  +
        		", parameters="+ parameters  +
        		"]";
	}

		
}
