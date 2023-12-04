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

package com.mark59.trends.sla;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.mark59.trends.application.AppConstantsTrends;
import com.mark59.trends.data.beans.Sla;
import com.mark59.trends.data.beans.Transaction;
import com.mark59.trends.data.sla.dao.SlaDAO;

/**
 * @author Philip Webb Written: Australian Winter 2019
 */
public class SlaChecker {

	public List<SlaTransactionResult> listCdpTaggedTransactionsWithFailedSlas(String application,
			List<Transaction> selectedUntaggedTransactions, SlaDAO slaDAO) {

		List<SlaTransactionResult> cdpTaggedTransactionsWithFailedSlas = new ArrayList<>();

		for (Transaction transaction : selectedUntaggedTransactions) {
			Sla transactionSla = slaDAO.getSla(application, transaction.getTxnId(), transaction.getIsCdpTxn());
			SlaTransactionResult slaTransactionResult = checkIfTransactionMeetDatabaseSLA(transaction, transactionSla);

			if (!slaTransactionResult.isPassedAllSlas()) {
				if ("Y".equalsIgnoreCase(transaction.getIsCdpTxn())) {
					slaTransactionResult.setTxnId(transaction.getTxnId() + AppConstantsTrends.CDP_TAG);
				}
				cdpTaggedTransactionsWithFailedSlas.add(slaTransactionResult);
			}
		}
		return cdpTaggedTransactionsWithFailedSlas;
	}

	public List<String> checkForMissingTransactionsWithDatabaseSLAs(String application, String runTime, SlaDAO slaDAO) {
		return slaDAO.getSlasWithMissingTxnsInThisRunCdpTags(application, runTime);
	}

	private SlaTransactionResult checkIfTransactionMeetDatabaseSLA(Transaction transaction, Sla transactionSla) {

		SlaTransactionResult slaTransactionResult = new SlaTransactionResult();
		slaTransactionResult.setTxnId(transaction.getTxnId()); // only for debug

		if (transactionSla != null && "Y".equals(transactionSla.getIsActive())){
			slaTransactionResult.setFoundSLAforTxnId(true);

			slaTransactionResult.setTxn90thResponse(transaction.getTxn90th());
			slaTransactionResult.setSla90thResponse(transactionSla.getSla90thResponse());
			slaTransactionResult.setPassed90thResponse(
					checkPercentileResponse(transaction.getTxn90th(), transactionSla.getSla90thResponse()));

			slaTransactionResult.setTxn95thResponse(transaction.getTxn95th());
			slaTransactionResult.setSla95thResponse(transactionSla.getSla95thResponse());
			slaTransactionResult.setPassed95thResponse(
					checkPercentileResponse(transaction.getTxn95th(), transactionSla.getSla95thResponse()));

			slaTransactionResult.setTxn99thResponse(transaction.getTxn99th());
			slaTransactionResult.setSla99thResponse(transactionSla.getSla99thResponse());
			slaTransactionResult.setPassed99thResponse(
					checkPercentileResponse(transaction.getTxn99th(), transactionSla.getSla99thResponse()));

			slaTransactionResult.setTxnFailurePercent(calculateTxnFailurePercent(transaction));
			slaTransactionResult.setSlaFailurePercent(transactionSla.getSlaFailPercent());
			slaTransactionResult.setPassedFailPercent(
					checkFailPercent(slaTransactionResult.getTxnFailurePercent(), transactionSla.getSlaFailPercent()));

			slaTransactionResult.setTxnFailCount(transaction.getTxnFail());
			slaTransactionResult.setSlaFailCount(transactionSla.getSlaFailCount());
			slaTransactionResult.setPassedFailCount(
					checkFailCount(transaction.getTxnFail(), transactionSla.getSlaFailCount()));

			slaTransactionResult.setTxnPassCount(transaction.getTxnPass());
			slaTransactionResult.setSlaPassCount(transactionSla.getSlaPassCount());
			slaTransactionResult.setSlaPassCountVariancePercent(transactionSla.getSlaPassCountVariancePercent());
			slaTransactionResult.setPassedPassCount(
					checkPassCount(transaction.getTxnPass(),transactionSla.getSlaPassCount(), transactionSla.getSlaPassCountVariancePercent()));

		} else {
//    		System.out.println( "  SlaChecker: Warning  - no active SLA exists for reported transaction " +  " " + transaction.getTxnId() );
			slaTransactionResult.setFoundSLAforTxnId(false);
			slaTransactionResult.setPassed90thResponse(true);
			slaTransactionResult.setPassed95thResponse(true);
			slaTransactionResult.setPassed99thResponse(true);
			slaTransactionResult.setPassedFailPercent(true);
			slaTransactionResult.setPassedFailCount(true);
			slaTransactionResult.setPassedPassCount(true);
		}

		slaTransactionResult.setPassedAllSlas(false);
		if (slaTransactionResult.isPassed90thResponse() && slaTransactionResult.isPassed95thResponse()
				&& slaTransactionResult.isPassed99thResponse() && slaTransactionResult.isPassedFailPercent()
				&& slaTransactionResult.isPassedFailCount() && slaTransactionResult.isPassedPassCount()) {
			slaTransactionResult.setPassedAllSlas(true);
		}
		return slaTransactionResult;
	}

	private Boolean checkPercentileResponse(BigDecimal txnPercentileResponse, BigDecimal slaForPercentileResponse) {
		boolean passThisSla = true;
		if (slaForPercentileResponse != null) {
			if (slaForPercentileResponse.doubleValue() > -0.001) {
				if (txnPercentileResponse.doubleValue() > slaForPercentileResponse.doubleValue()) {
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
		if (transaction.getTxnFail() > 0) {
			txnFailurePercentDbl = (double) transaction.getTxnFail() / (transaction.getTxnFail() + transaction.getTxnPass()) * 100;
		}
		return new BigDecimal(txnFailurePercentDbl).setScale(3, RoundingMode.HALF_UP);
	}

	private Boolean checkFailPercent(BigDecimal txnFailurePercent, BigDecimal slaFailPercent) {
		boolean passThisSla = true;
		if (slaFailPercent != null) {
			if (slaFailPercent.doubleValue() > -0.001 && txnFailurePercent.doubleValue() > 0) {
				if (txnFailurePercent.compareTo(slaFailPercent) > 0) { // txnFailurePercent > slaFailPercent
//		    		System.out.println( "    " + txnId + " has failed it's % error rate SLA as recorded on the SLA database ! "  );
//		    		System.out.println( "                      error rate was " + new DecimalFormat("#.##").format(txnFailurePercent)  + "%, "
//		    							+ "( SLA % of " + slaFailPercent );
					passThisSla = false;
				}
			}
		}
		return passThisSla;
	}

	private Boolean checkFailCount(long txnFailCount, long slaFailCount) {
		boolean passThisSla = true;
		if (slaFailCount > -1) { 
			// note that 0 can be set as a Fail Count (implies that if this txn exists, any txn failure means a SLA failure will occur)
			if (txnFailCount > slaFailCount) {
				passThisSla = false;
			}
		}
		return passThisSla;
	}

	private Boolean checkPassCount(long txnPassCount, long slaPassCount, BigDecimal slaPassCountVariancePercent) {
		boolean passThisSla = true;
		double minTxnPassCount;
		double maxTxnPassCount;

		if (slaPassCount > -1) { 
			// note that 0 can be set as a Pass Count (implies that if this txn exists, a SLA failure will occur)
			// check for the minimum and maximum allowed transaction counts ..

			minTxnPassCount = (double) slaPassCount - (double) slaPassCount * slaPassCountVariancePercent.doubleValue() * 0.01;
			maxTxnPassCount = (double) slaPassCount + (double) slaPassCount * slaPassCountVariancePercent.doubleValue() * 0.01;
//			System.out.println( "checkPassCount: txn=" + txnId +  ", txnPassCount="  + txnPassCount + ", minTxnPassCount=" + minTxnPassCount +  ", maxTxnPassCount=" + maxTxnPassCount    );

			if ((double) txnPassCount < minTxnPassCount || (double) txnPassCount > maxTxnPassCount) {
// 	    		System.out.println( "    " + txnId + " has failed it's Pass Count SLA as recorded on the SLA database ! "  );
//	    		System.out.println( "                      count was " + txnPassCount + " secs, SLA of " + slaPassCount + "+/-" + slaPassCountVariancePercent + "%"   );
				passThisSla = false;
			}
		}
		return passThisSla;
	}

}