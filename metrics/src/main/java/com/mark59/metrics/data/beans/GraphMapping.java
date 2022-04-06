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
public class GraphMapping {

	Integer		listOrder;
	String		graph;
	String		txnType;
	String		valueDerivation;
	String		uomDescription;
	String		barRangeSql; 
	String		barRangeLegend; 	
	String 		comment;
	
	public GraphMapping() {
	}

	public Integer getListOrder() {
		return listOrder;
	}

	public void setListOrder(Integer listOrder) {
		this.listOrder = listOrder;
	}

	public String getGraph() {
		return graph;
	}

	public void setGraph(String graph) {
		this.graph = graph;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getValueDerivation() {
		return valueDerivation;
	}

	public void setValueDerivation(String valueDerivation) {
		this.valueDerivation = valueDerivation;
	}

	public String getUomDescription() {
		return uomDescription;
	}

	public void setUomDescription(String uomDescription) {
		this.uomDescription = uomDescription;
	}

	public String getBarRangeSql() {
		return barRangeSql;
	}

	public void setBarRangeSql(String barRangeSql) {
		this.barRangeSql = barRangeSql;
	}

	public String getBarRangeLegend() {
		return barRangeLegend;
	}

	public void setBarRangeLegend(String barRangeLegend) {
		this.barRangeLegend = barRangeLegend;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
        return "[graph= "+ graph + 
        		", txnType= "+ txnType + 
        		", valueDerivation= "+ valueDerivation + 
        		", uomDescription= "+ uomDescription + 
        		", comment= "+ comment  +
        		"]";
	}

	
		
}
