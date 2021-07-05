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

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class EventMapping {

	String		txnType;
	String		metricSource;
	String		matchWhenLike;
	String		matchWhenLikeURLencoded;	
	String		targetNameLB;
	String		targetNameRB;
	String		isPercentage;	
	String		isInvertedPercentage;
	String		performanceTool;		
	String 		comment;
	
	public EventMapping() {
	}

	public String getTxnType() {
		return txnType;
	}
	public void setTxnType(String txnype) {
		this.txnType = txnype;
	}
	public String getMetricSource() {
		return metricSource;
	}
	public void setMetricSource(String metricSource) {
		this.metricSource = metricSource;
	}
	public String getMatchWhenLike() {
		return matchWhenLike;
	}
	public void setMatchWhenLike(String matchWhenLike) {
		this.matchWhenLike = matchWhenLike;
	}
	public String getMatchWhenLikeURLencoded() {
		return matchWhenLikeURLencoded;
	}
	public void setMatchWhenLikeURLencoded(String matchWhenLikeURLencoded) {
		this.matchWhenLikeURLencoded = matchWhenLikeURLencoded;
	}
	public String getTargetNameLB() {
		return targetNameLB;
	}
	public void setTargetNameLB(String targetNameLB) {
		this.targetNameLB = targetNameLB;
	}
	public String getTargetNameRB() {
		return targetNameRB;
	}
	public void setTargetNameRB(String targetNameRB) {
		this.targetNameRB = targetNameRB;
	}
	public String getIsPercentage() {
		return isPercentage;
	}
	public void setIsPercentage(String isPercentage) {
		this.isPercentage = isPercentage;
	}
	public String getIsInvertedPercentage() {
		return isInvertedPercentage;
	}
	public void setIsInvertedPercentage(String isInvertedPercentage) {
		this.isInvertedPercentage = isInvertedPercentage;
	}
	public String getPerformanceTool() {
		return performanceTool;
	}
	public void setPerformanceTool(String performanceTool) {
		this.performanceTool = performanceTool;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
    public String toString() {
        return   "[txnType="+ txnType + 
        		", metricSource="+metricSource + 
        		", matchWhenLike="+matchWhenLike + 
        		", targetNameLB="+targetNameLB + 
        		", targetNameRB="+targetNameRB  +
        		", performanceTool="+performanceTool  +        		
        		"]";
	}
}
