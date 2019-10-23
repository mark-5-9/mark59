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

package com.mark59.datahunter.functionalTest.dsl;

import java.sql.Timestamp;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class Policies {

	String			application;
	String			identifier;
	String			lifecycle;
	String			useability;	
	String			otherdata;
	Timestamp		created;
	Timestamp		updated;	
	Long			epochtime;
	
	public Policies() {
	}
	
	
	/**
	 * convenience constructor mainly intended for use during internal testing. 
	 */
	public Policies( String	application, String	identifier, String lifecycle, String useability, String	otherdata, 	Long epochtime ) {	
		this.application = application;
		this.identifier = identifier;
		this.lifecycle = lifecycle;
		this.useability = useability;
		this.otherdata = otherdata;
		this.epochtime = epochtime;
	}

	
	
	
	public String getApplication() {
		return application;
	}


	public void setApplication(String application) {
		this.application = application;
	}


	public String getIdentifier() {
		return identifier;
	}


	public void setIdentifier(String identifier) {
		this.identifier = identifier;
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


	public String getOtherdata() {
		return otherdata;
	}


	public void setOtherdata(String otherdata) {
		this.otherdata = otherdata;
	}


	public Timestamp getCreated() {
		return created;
	}


	public void setCreated(Timestamp created) {
		this.created = created;
	}
 
	public Timestamp getUpdated() {
		return updated;
	}


	public void setUpdated(Timestamp updated) {
		this.updated = updated;
	}


	public Long getEpochtime() {
		return epochtime;
	}


	public void setEpochtime(Long epochtime) {
		this.epochtime = epochtime;
	}


	@Override
    public String toString() {
        return   "[application="+ application + 
        		", identifier="+ identifier + 
        		", lifecycle="+ lifecycle + 
        		", useability="+ useability + 
        		", otherdata="+ otherdata  +
        		", created="+ created  +
        		", updated="+ updated  +        		
        		", epochtime="+ epochtime  +
        		"]";
	}
		
}
