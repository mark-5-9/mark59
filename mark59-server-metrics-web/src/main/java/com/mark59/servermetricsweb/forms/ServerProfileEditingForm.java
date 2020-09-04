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

package com.mark59.servermetricsweb.forms;

import java.util.Arrays;
import java.util.List;

import com.mark59.servermetricsweb.data.beans.ServerProfile;

/**
 * @author Philip Webb
 * Written: Australian Spring 2020
 * 
 */
public class ServerProfileEditingForm {

	ServerProfile serverProfile;
	List<CommandSelector> commandSelectors;

	public ServerProfile getServerProfile() {
		return serverProfile;
	}

	public void setServerProfile(ServerProfile serverProfile) {
		this.serverProfile = serverProfile;
	}

	public List<CommandSelector> getCommandSelectors() {
		return commandSelectors;
	}

	public void setCommandSelectors(List<CommandSelector> commandSelectors) {
		this.commandSelectors = commandSelectors;
	}

	@Override
    public String toString() {
        return   "[serverProfileName ="+ serverProfile.getServerProfileName() + 
        		", commandSelectors = "+ Arrays.toString(commandSelectors.toArray()) + 
        		"]";
	}
		
}
