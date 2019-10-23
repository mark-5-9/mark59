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

package com.mark59.metrics.metricSla;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mark59.metrics.application.AppConstants;
import com.mark59.metrics.data.beans.MetricSla;
import com.mark59.metrics.data.metricSla.dao.MetricSlaDAO;
import com.mark59.metrics.data.transaction.dao.TransactionDAO;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class MetricSlaChecker {

	
	public List<MetricSlaResult> listFailedMetricSLAs(String application, String runTime, String metricTxnType, MetricSlaDAO metricSlaDAO, TransactionDAO transactionDAO) {
		
		List<MetricSlaResult> metricSlaResults = new ArrayList<MetricSlaResult>();
		
		
		List<MetricSla> metricSlas = new ArrayList<MetricSla>();
		if (metricTxnType == null) {
			metricSlas = metricSlaDAO.getMetricSlaList(application);
		} else {
			metricSlas = metricSlaDAO.getMetricSlaList(application, metricTxnType);
		}
		
		Map<String,String> valueDerivatonToSourceField = AppConstants.getValueDerivatonToSourceFieldMap();
		
		for (MetricSla metricSla : metricSlas) {
			//lookup using Value Derivation to get the field on the transaction which store the value for sla
			String transactionField = valueDerivatonToSourceField.get(metricSla.getValueDerivation());
			
//			System.out.println();
//			System.out.println("metricSla name:type:deriv = " + metricSla.getMetricName() + ":" + metricSla.getMetricTxnType() + ":"  + metricSla.getValueDerivation() + ", transactionField = " +  transactionField  );
			
			Object txnValueObj = transactionDAO.getTransactionValue(application,  metricSla.getMetricTxnType(), runTime, metricSla.getMetricName(), transactionField ); 
			
			if ( txnValueObj == null ){
				String messageText = "Metric SLA Failed Warning  : no metric has been found but was expected for " + metricSla.getMetricTxnType() + " " +  metricSla.getValueDerivation() + " on " + metricSla.getMetricName();   
				MetricSlaResult metricSlaResult = new MetricSlaResult(metricSla.getMetricName(), metricTxnType,	metricSla.getValueDerivation(),SlaResultTypeEnum.MISSING_SLA_TRANSACTION, messageText );
				metricSlaResults.add(metricSlaResult);
			} else {
				
				BigDecimal txnValue;
				if ( txnValueObj instanceof BigDecimal){
					txnValue = (BigDecimal) txnValueObj;
				} else {
					txnValue = new BigDecimal((Long)txnValueObj);
				}
				
				if ( txnValue.doubleValue() < metricSla.getSlaMin().doubleValue()  || 	txnValue.doubleValue() > metricSla.getSlaMax().doubleValue() ){
					String messageText = "Metric SLA Failed Warning  : metric out of expected range for " +  metricSla.getMetricTxnType() + " " +  metricSla.getValueDerivation() + " on " + metricSla.getMetricName()
							+ ".  Range is set as " + metricSla.getSlaMin().doubleValue() + " to " +  metricSla.getSlaMax().doubleValue()+ ", actual was " +  txnValue.doubleValue();
					MetricSlaResult metricSlaResult = new MetricSlaResult(metricSla.getMetricName(), metricTxnType,	metricSla.getValueDerivation(), SlaResultTypeEnum.FAILED_SLA, messageText );
					metricSlaResults.add(metricSlaResult);
				}	
			}
			
		}
		return metricSlaResults;
	}

	
	
	

}
