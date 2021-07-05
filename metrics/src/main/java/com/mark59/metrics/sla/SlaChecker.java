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

package com.mark59.metrics.sla;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.mark59.metrics.data.beans.Sla;
import com.mark59.metrics.data.beans.Transaction;
import com.mark59.metrics.data.sla.dao.SlaDAO;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class SlaChecker {


	public List<SlaTransactionResult> listTransactionsWithFailedSlas(String application, List<Transaction> transactions, SlaDAO slaDAO) {
		
		List<SlaTransactionResult> transactionsWithFailedSlas = new ArrayList<SlaTransactionResult>();

		for (Transaction transaction : transactions) {
			Sla transactionSla = slaDAO.getSla(application, transaction.getTxnId());
			SlaTransactionResult slaTransactionResult = this.checkIfTransactionMeetDatabaseSLA(transaction, transactionSla);	
			
			if (!slaTransactionResult.isPassedAllSlas()){
				transactionsWithFailedSlas.add(slaTransactionResult);
			}
		}
		return transactionsWithFailedSlas;
	}	
	

	
	public List<String> checkForMissingTransactionsWithDatabaseSLAs(String application, String runTime, SlaDAO slaDAO) {

		List<String> slasWithMissingTxns = slaDAO.getSlasWithMissingTxnsInThisRun(application, runTime  );		
		return slasWithMissingTxns;
	}
	
	
	
	

	public SlaTransactionResult checkIfTransactionMeetDatabaseSLA(Transaction transaction, Sla transactionSla) {
		
		SlaTransactionResult slaTransactionResult = new SlaTransactionResult();
		slaTransactionResult.setTxnId(transaction.getTxnId() );
		
		if ( transactionSla  != null ){
			slaTransactionResult.setFoundSLAforTxnId(true);
			
			slaTransactionResult.setTxn90thResponse(transaction.getTxn90th());
			slaTransactionResult.setSla90thResponse(transactionSla.getSla90thResponse());
			slaTransactionResult.setPassed90thResponse(checkPercentileResponse(transaction.getTxnId(), transaction.getTxn90th(), transactionSla.getSla90thResponse())); 
			
			slaTransactionResult.setTxn95thResponse(transaction.getTxn95th());
			slaTransactionResult.setSla95thResponse(transactionSla.getSla95thResponse());
			slaTransactionResult.setPassed95thResponse(checkPercentileResponse(transaction.getTxnId(), transaction.getTxn95th(), transactionSla.getSla95thResponse())); 			
			
			slaTransactionResult.setTxn99thResponse(transaction.getTxn99th());
			slaTransactionResult.setSla99thResponse(transactionSla.getSla99thResponse());
			slaTransactionResult.setPassed99thResponse(checkPercentileResponse(transaction.getTxnId(), transaction.getTxn99th(), transactionSla.getSla99thResponse())); 			

			
			slaTransactionResult.setTxnFailurePercent(calculateTxnFailurePercent(transaction));
			slaTransactionResult.setSlaFailurePercent(transactionSla.getSlaFailPercent());
			slaTransactionResult.setPassedFailPercent(checkFailPercent(transaction.getTxnId(), slaTransactionResult.getTxnFailurePercent() , transactionSla.getSlaFailPercent()));

			slaTransactionResult.setTxnFailCount(transaction.getTxnFail());
			slaTransactionResult.setSlaFailCount(transactionSla.getSlaFailCount());
			slaTransactionResult.setPassedFailCount(checkFailCount(transaction.getTxnId(), transaction.getTxnFail(), transactionSla.getSlaFailCount())); 			

			slaTransactionResult.setTxnPassCount(transaction.getTxnPass());
			slaTransactionResult.setSlaPassCount(transactionSla.getSlaPassCount());
			slaTransactionResult.setSlaPassCountVariancePercent(transactionSla.getSlaPassCountVariancePercent());
			slaTransactionResult.setPassedPassCount(
					checkPassCount(transaction.getTxnId(), transaction.getTxnPass(), transactionSla.getSlaPassCount(), transactionSla.getSlaPassCountVariancePercent())); 			
						
		} else {
//    		System.out.println( "  SlaChecker: Warning  - no SLA exists for reported transaction " +  " " + transaction.getTxnId() );
			slaTransactionResult.setFoundSLAforTxnId(false);
			slaTransactionResult.setPassed90thResponse(true); 
			slaTransactionResult.setPassed95thResponse(true); 
			slaTransactionResult.setPassed99thResponse(true); 
			slaTransactionResult.setPassedFailPercent(true); 
			slaTransactionResult.setPassedFailCount(true); 
			slaTransactionResult.setPassedPassCount(true); 
		}
		
		slaTransactionResult.setPassedAllSlas(false);
		if ( slaTransactionResult.isPassed90thResponse() &&
			 slaTransactionResult.isPassed95thResponse() && 
			 slaTransactionResult.isPassed99thResponse() && 
			 slaTransactionResult.isPassedFailPercent() && 
			 slaTransactionResult.isPassedFailCount() && 
			 slaTransactionResult.isPassedPassCount() ){
			slaTransactionResult.setPassedAllSlas(true);
		}
		return slaTransactionResult;
	}	

	
	private Boolean checkPercentileResponse(String txnId, BigDecimal txnPercentileResponse, BigDecimal slaForPercentileResponse) {
		boolean passThisSla = true;
		if ( slaForPercentileResponse != null  ){
			if ( slaForPercentileResponse.doubleValue() > -0.001) {
				if ( txnPercentileResponse.doubleValue() > slaForPercentileResponse.doubleValue() ){
// 		    		System.out.println( "    " + txnId + " has failed a  Percentile Response Time SLA as recorded on the SLA database ! "  );
//	 	    		System.out.println( "                      response was " + txnPercentileResponse + " secs, SLA of " + slaForPercentileResponse);
		    		passThisSla = false;
				}
			}	
		}
		return passThisSla;
	}


	
	private BigDecimal calculateTxnFailurePercent(Transaction transaction) {
		double txnFailurePercentDbl = 0;
		if ( transaction.getTxnFail() > 0   ) {
			txnFailurePercentDbl =   (double)((double)transaction.getTxnFail() / ( transaction.getTxnFail() + transaction.getTxnPass() )*100 );  
		}
		return new BigDecimal(txnFailurePercentDbl).setScale(3, RoundingMode.HALF_UP);
	}	
	
	
	private Boolean checkFailPercent(String txnId, BigDecimal txnFailurePercent, BigDecimal slaFailPercent) {
		boolean passThisSla = true;
		if (slaFailPercent != null  ){
			if ( slaFailPercent.doubleValue() > -0.001  &&  txnFailurePercent.doubleValue() > 0   ) {
				if ( txnFailurePercent.compareTo(slaFailPercent) == 1 ){   // txnFailurePercent > slaFailPercent
//		    		System.out.println( "    " + txnId + " has failed it's % error rate SLA as recorded on the SLA database ! "  );
//		    		System.out.println( "                      error rate was " + new DecimalFormat("#.##").format(txnFailurePercent)  + "%, "
//		    							+ "( SLA % of " + slaFailPercent );
		    		passThisSla = false;
				}
			}
		}
		return passThisSla;
	}	
	
	private Boolean checkFailCount(String txnId, long txnFailCount, long slaFailCount) {
		boolean passThisSla = true;
		if ( slaFailCount > -1 ) {   	// note that 0 can be set as a Fail Count (implies that if this txn exists, any txn failure means a SLA failure will occur)   
			if ( txnFailCount >  slaFailCount ){
	    		passThisSla = false;
			}
		}
		return passThisSla;
	}
	
	
	private Boolean checkPassCount(String txnId, long txnPassCount, long slaPassCount, BigDecimal slaPassCountVariancePercent  ) {
		boolean passThisSla = true;
		double minTxnPassCount = 0;
		double maxTxnPassCount = 0;		
		
		if ( slaPassCount > -1 ) {   	// note that 0 can be set as a Pass Count (implies that if this txn exists, an SLA failure will occur)   
			
			// now check for the minimum and maximum allowed transaction counts
			
			minTxnPassCount = (double)((double)slaPassCount  - (double)slaPassCount * slaPassCountVariancePercent.doubleValue() * 0.01 );  
			maxTxnPassCount = (double)((double)slaPassCount  + (double)slaPassCount * slaPassCountVariancePercent.doubleValue() * 0.01 );  			
//			System.out.println( "checkPassCount: txn=" + txnId +  ", txnPassCount="  + txnPassCount + ", minTxnPassCount=" + minTxnPassCount +  ", maxTxnPassCount=" + maxTxnPassCount    );
			
			if ( (double)txnPassCount <  minTxnPassCount  ||  (double)txnPassCount >  maxTxnPassCount     ){
// 		    		System.out.println( "    " + txnId + " has failed it's Pass Count SLA as recorded on the SLA database ! "  );
//	 	    		System.out.println( "                      count was " + txnPassCount + " secs, SLA of " + slaPassCount + "+/-" + slaPassCountVariancePercent + "%"   );
	    		passThisSla = false;
			}

		}
		return passThisSla;
	}
	
}