/*
 *  Copyright 2019 Mark59.com
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

package com.mark59.datahunter.model;

/**
 * @author Philip Webb
 * Written: Australian Autumn 2020
 */
public class UploadIdsFile {

	String	application;
	String  lifecycle;	
	String  useability;
	String	updateOrBypassExisting;	
	
	public UploadIdsFile() {
	}

	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getLifecycle() {
		return lifecycle;
	}
	public void setLifecycle(String lifecycle) {
		this.lifecycle = lifecycle;
	}
	public String getUseability() {
		return useability;
	}
	public void setUseability(String useability) {
		this.useability = useability;
	}
	public String getUpdateOrBypassExisting() {
		return updateOrBypassExisting;
	}
	public void setUpdateOrBypassExisting(String updateOrBypassExisting) {
		this.updateOrBypassExisting = updateOrBypassExisting;
	}

	@Override
    public String toString() {
        return   "[application="+ application + 
        		", lifecycle=" + lifecycle + 
        		", useability="+ useability + 
        		", updateOrBypassExisting="+ updateOrBypassExisting + 
        		"]";
	}
		
}
