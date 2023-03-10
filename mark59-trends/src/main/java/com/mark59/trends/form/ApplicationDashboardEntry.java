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

package com.mark59.trends.form;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class ApplicationDashboardEntry {
	
	String	application;
	String	active;
	String	comment;
	String	sinceLastRun;
	String	slaTransactionResultIcon;
	String	slaMetricsResultIcon;
	String  slaSummaryIcon;

	
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getActive() {
		return active;
	}
	public void setActive(String active) {
		this.active = active;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getSinceLastRun() {
		return sinceLastRun;
	}
	public void setSinceLastRun(String sinceLastRun) {
		this.sinceLastRun = sinceLastRun;
	}
	public String getSlaTransactionResultIcon() {
		return slaTransactionResultIcon;
	}
	public void setSlaTransactionResultIcon(String slaTransactionResultIcon) {
		this.slaTransactionResultIcon = slaTransactionResultIcon;
	}
	public String getSlaMetricsResultIcon() {
		return slaMetricsResultIcon;
	}
	public void setSlaMetricsResultIcon(String slaMetricsResultIcon) {
		this.slaMetricsResultIcon = slaMetricsResultIcon;
	}
	public String getSlaSummaryIcon() {
		return slaSummaryIcon;
	}
	public void setSlaSummaryIcon(String slaSummaryIcon) {
		this.slaSummaryIcon = slaSummaryIcon;
	}
	
	@Override
	public String toString(){
		return "ApplicationDashboardEntry: "
				+ " application= "+application 
				+ " active="+active 
				+ " comment="+comment 
				+ " sinceLastRun="+sinceLastRun 
				+ " slaTransactionResultIcon="+slaTransactionResultIcon 
				+ " slaMetricsResultIcon="+slaMetricsResultIcon 
				+ " slaSummaryIcon="+slaSummaryIcon 
				;
	}
}
