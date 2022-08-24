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

package com.mark59.results.splitter;

/**
 * The datatypes available for output onto a JMeter results file.
 * Note - this enum has been copied from the mark59 core library com.mark59.core.utils.Mark59Constants.JMeterFileDatatypes
 * 
 * <p>Any update to the core version may need to be reflected here.  
 * 
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public enum JMeterFileDatatypes {

	DATAPOINT("DATAPOINT", true), CPU_UTIL("CPU_UTIL", true), MEMORY("MEMORY", true), 
	TRANSACTION("", false),	CDP("CDP", false), PARENT("PARENT", false);
	
	private final String datatypeText;
	private final boolean metricDataType;
	
	JMeterFileDatatypes(String datatypeText, boolean metricDataType) {
		this.datatypeText = datatypeText;
		this.metricDataType = metricDataType;
	}
	public String getDatatypeText() {
		return datatypeText;
	}
	public boolean isMetricDataType() {
		return this.metricDataType;
	}    	
}