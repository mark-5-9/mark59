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

package com.mark59.servermetricsweb.pojos;

import java.util.List;

/**
 * @author Philip Webb
 * Written: Australian Summer 2020
 * 
 */
public class CommandDriverResponse {

	private List<String> rawCommandResponseLines;
	private String commandLog;	
	private boolean commandFailure;

	
	
	public List<String> getRawCommandResponseLines() {
		return rawCommandResponseLines;
	}



	public void setRawCommandResponseLines(List<String> rawCommandResponseLines) {
		this.rawCommandResponseLines = rawCommandResponseLines;
	}



	public String getCommandLog() {
		return commandLog;
	}



	public void setCommandLog(String commandLog) {
		this.commandLog = commandLog;
	}



	public boolean isCommandFailure() {
		return commandFailure;
	}



	public void setCommandFailure(boolean commandFailure) {
		this.commandFailure = commandFailure;
	}



	@Override
    public String toString() {
        return   "[rawCommandResponseLines= "  + rawCommandResponseLines
        		+ ", commandLog="+ commandLog   
        		+ ", commandFailure="+ commandFailure   
        		+ "]";
	}
		
}
