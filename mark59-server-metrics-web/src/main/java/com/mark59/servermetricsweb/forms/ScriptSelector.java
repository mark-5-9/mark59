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

/**
 * @author Philip Webb
 * Written: Australian Spring 2020
 * 
 */
public class ScriptSelector {
	
	String scriptName;
	boolean scriptChecked;
	
	public ScriptSelector() {
	}
	
	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}
	
	public boolean isScriptChecked() {
		return scriptChecked;
	}

	public void setScriptChecked(boolean scriptChecked) {
		this.scriptChecked = scriptChecked;
	}

	@Override
    public String toString() {
        return   "[scriptName="+ scriptName + 
        		", scriptChecked="+ scriptChecked + 
        		"]";
	}
		
}
