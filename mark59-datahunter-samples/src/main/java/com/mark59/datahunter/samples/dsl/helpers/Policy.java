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

package com.mark59.datahunter.samples.dsl.helpers;

/**
 * @author Philip Webb
 * Written: Australian Spring 2021
 */
public class Policy {

	String	application;
	String	identifier;
	String	lifecycle;
	String	useability;	
	String	otherdata;
	String	created;
	String	updated;	
	String	epochtime;
	
	public Policy() {
	}
	
	/**
	 * convenience constructors mainly intended for use during internal testing. 
	 */
	public Policy( String	application, String	identifier, String lifecycle, String useability, String	otherdata, 	String epochtime ) {	
		this.application = application;
		this.identifier = identifier;
		this.lifecycle = lifecycle;
		this.useability = useability;
		this.otherdata = otherdata;
		this.epochtime = epochtime;
	}

	public Policy( String	application, String	identifier, String lifecycle, String useability, String	otherdata) {	
		this.application = application;
		this.identifier = identifier;
		this.lifecycle = lifecycle;
		this.useability = useability;
		this.otherdata = otherdata;
		this.epochtime = "";
	}
	
	public Policy( String	application, String	identifier, String lifecycle, String useability) {	
		this.application = application;
		this.identifier = identifier;
		this.lifecycle = lifecycle;
		this.useability = useability;
		this.otherdata = "";
		this.epochtime = "";
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
	public String getCreated() {
		return created;
	}
	public void setCreated(String created) {
		this.created = created;
	}
	public String getUpdated() {
		return updated;
	}
	public void setUpdated(String updated) {
		this.updated = updated;
	}
	public String getEpochtime() {
		return epochtime;
	}
	public void setEpochtime(String epochtime) {
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
