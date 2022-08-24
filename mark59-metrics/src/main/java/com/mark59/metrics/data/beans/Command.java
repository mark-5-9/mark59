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

import java.util.List;

/**
 * @author Philip Webb
 * Written: Australian Spring 2020
 * 
 */
public class Command {

	String commandName;
	String executor;
	String command;
	String ingoreStderr;
	String comment;
	List<String> paramNames;
	
	public Command() {
	}


	public String getCommandName() {
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}

	public String getExecutor() {
		return executor;
	}

	public void setExecutor(String executor) {
		this.executor = executor;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getIngoreStderr() {
		return ingoreStderr;
	}

	public void setIngoreStderr(String ingoreStderr) {
		this.ingoreStderr = ingoreStderr;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public List<String> getParamNames() {
		return paramNames;
	}

	public void setParamNames(List<String> paramNames) {
		this.paramNames = paramNames;
	}


	@Override
    public String toString() {
        return   "[commandName="+ commandName + 
        		", executor="+ executor + 
        		", command="+ command + 
        		", ingoreStderr="+ ingoreStderr + 
        		", comment="+ comment + 
        		", paramNames="+ paramNames + 
        		"]";
	}
		
}
