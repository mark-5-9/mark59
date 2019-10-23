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

package com.mark59.servermetrics.driver.config;

import static com.mark59.servermetrics.ServerMetricsConstants.UNIX;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class ServerMetricsDriverConfig {

	private static final int DEFAULT_PORT = 22;
	private static final int DEFAULT_TIMEOUT = 60000;

	private String server;
	private String user;
	private String password;
	private String passwordCipher;
	private String alternateServerId; 
	private int connectionPort;
	private int connectionTimeOut;
	private String operatingSystem;

	
	public ServerMetricsDriverConfig(String server, String user, String password, String passwordCipher) {
		this(server, user, password, passwordCipher, "");
	}	
	
	public ServerMetricsDriverConfig(String server, String user, String password, String passwordCipher, String alternateServerId) {
		this(server, user, password, passwordCipher, alternateServerId, DEFAULT_PORT);
	}

	public ServerMetricsDriverConfig(String server, String user, String password, String passwordCipher, String alternateServerId, int connectionPort) {
		this(server, user, password, passwordCipher, alternateServerId, connectionPort, DEFAULT_TIMEOUT);
	}

	public ServerMetricsDriverConfig(String server, String user, String password, String passwordCipher, String alternateServerId, int connectionPort, int connectionTimeOut) {
		this(server, user, password, passwordCipher, alternateServerId, connectionPort, connectionTimeOut, UNIX);
	}

	public ServerMetricsDriverConfig(String server, String user, String password, String passwordCipher, String alternateServerId, int connectionPort, int connectionTimeOut, String operatingSystem) {
		this.server = server;
		this.user = user;
		this.password = password;
		this.passwordCipher = passwordCipher;
		this.alternateServerId = alternateServerId;
		this.connectionPort = connectionPort;
		this.connectionTimeOut = connectionTimeOut;
		this.operatingSystem = operatingSystem;
	}

	public String getServer() {
		return server;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}
	
	public String getPasswordCipher() {
		return passwordCipher;
	}
	
	public String getAlternateServerId() {
		return alternateServerId;
	}

	public void setAlternateServerId(String alternateServerId) {
		this.alternateServerId = alternateServerId;
	}

	public int getConnectionPort() {
		return connectionPort;
	}

	public int getConnectionTimeOut() {
		return connectionTimeOut;
	}

	public String getOperatingSystem() {
		return operatingSystem;
	}

	public void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}
	
	
	public String toString() {
		return 	"server " + server +
				",user "  + user +
				",alternateServerId " + alternateServerId + 
				",port " + connectionPort +
				",time " + connectionTimeOut +
				",os " + operatingSystem;
	}
	

}
