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

package com.mark59.metrics.forms;

import java.util.List;

import com.mark59.metrics.data.beans.ServerProfile;

/**
 * @author Philip Webb
 * Written: Australian Spring 2020
 * 
 */
public class ServerProfileEditingForm {

	ServerProfile serverProfile;
	List<CommandSelector> commandSelectors;
	String selectedExecutorChanged;
	List<String> commandNames;
	String selectedScriptCommandName;
	String selectedScriptCommandNameChanged;
	List<CommandParameter> commandParameters;
	
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

	public String getSelectedExecutorChanged() {
		return selectedExecutorChanged;
	}

	public void setSelectedExecutorChanged(String selectedExecutorChanged) {
		this.selectedExecutorChanged = selectedExecutorChanged;
	}

	public List<String> getCommandNames() {
		return commandNames;
	}

	public void setCommandNames(List<String> commandNames) {
		this.commandNames = commandNames;
	}

	public String getSelectedScriptCommandName() {
		return selectedScriptCommandName;
	}

	public void setSelectedScriptCommandName(String selectedScriptCommandName) {
		this.selectedScriptCommandName = selectedScriptCommandName;
	}

	public String getSelectedScriptCommandNameChanged() {
		return selectedScriptCommandNameChanged;
	}

	public void setSelectedScriptCommandNameChanged(String selectedScriptCommandNameChanged) {
		this.selectedScriptCommandNameChanged = selectedScriptCommandNameChanged;
	}

	public List<CommandParameter> getCommandParameters() {
		return commandParameters;
	}

	public void setCommandParameters(List<CommandParameter> commandParameters) {
		this.commandParameters = commandParameters;
	}

	@Override 
    public String toString() {
        return   "[serverProfileName ="+ serverProfile.getServerProfileName() + 
        		", selectedExecutorChanged = "+ selectedExecutorChanged +         		
        		", commandSelectors = "+ commandSelectors + 
        		", commandNames = "+ commandNames + 
        		", selectedScriptCommandName = "+ selectedScriptCommandName + 
        		", selectedScriptCommandNameChanged = "+ selectedScriptCommandNameChanged + 
        		", commandParameters = "+ commandParameters + 
        		"]";
	}
		
}
