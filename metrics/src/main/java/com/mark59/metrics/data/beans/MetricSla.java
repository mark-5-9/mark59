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

import java.math.BigDecimal;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class MetricSla {
	
	String		application;
	String		metricName;
	String		metricNameURLencoded;	
	String		metricTxnType;
	String		valueDerivation;
	BigDecimal	slaMin;
	BigDecimal	slaMax;
	String		isActive;		
	String 		comment;
	String		originalMetricName;	
	
	
	public MetricSla() {
	}
	
	public MetricSla(MetricSla copy) {
		this.application = copy.application;
		this.metricName = copy.metricName;
		this.metricNameURLencoded = copy.metricNameURLencoded;
		this.metricTxnType = copy.metricTxnType;
		this.valueDerivation = copy.valueDerivation;
		this.slaMin = copy.slaMin;
		this.slaMax = copy.slaMax;
		this.isActive = copy.isActive;
		this.comment = copy.comment;
		this.originalMetricName = copy.originalMetricName;
	}
	

	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getMetricName() {
		return metricName;
	}
	public String getMetricNameURLencoded() {
		return metricNameURLencoded;
	}
	public void setMetricNameURLencoded(String metricNameURLencoded) {
		this.metricNameURLencoded = metricNameURLencoded;
	}
	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}
	public String getMetricTxnType() {
		return metricTxnType;
	}
	public void setMetricTxnType(String metricTxnType) {
		this.metricTxnType = metricTxnType;
	}
	public String getValueDerivation() {
		return valueDerivation;
	}
	public void setValueDerivation(String valueDerivation) {
		this.valueDerivation = valueDerivation;
	}
	public BigDecimal getSlaMin() {
		return slaMin;
	}
	public void setSlaMin(BigDecimal slaMin) {
		this.slaMin = slaMin;
	}
	public BigDecimal getSlaMax() {
		return slaMax;
	}
	public void setSlaMax(BigDecimal slaMax) {
		this.slaMax = slaMax;
	}
	public String getIsActive() {
		return isActive;
	}
	public void setIsActive(String isActive) {
		this.isActive = isActive;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getOriginalMetricName() {
		return originalMetricName;
	}
	public void setOriginalMetricName(String originalMetricName) {
		this.originalMetricName = originalMetricName;
	}
	

}
