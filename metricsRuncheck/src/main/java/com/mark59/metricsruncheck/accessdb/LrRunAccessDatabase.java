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

package com.mark59.metricsruncheck.accessdb;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.CursorBuilder;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import com.mark59.metrics.application.AppConstants;
import com.mark59.metrics.application.Utils;
import com.mark59.metrics.data.beans.DateRangeBean;
import com.mark59.metrics.data.beans.EventMapping;
import com.mark59.metrics.data.beans.Run;
import com.mark59.metrics.data.beans.TestTransaction;
import com.mark59.metrics.data.beans.Transaction;
import com.mark59.metrics.data.eventMapping.dao.EventMappingDAO;
import com.mark59.metrics.data.testTransactions.dao.TestTransactionsDAO;
import com.mark59.metricsruncheck.run.EventAttributes;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class LrRunAccessDatabase {

	// see LR table 'Transaction End Status'
	private final static String STATUS1_FAIL = "0";
	private final static String STATUS1_PASS = "1"; 
	private final static String STATUS1_STOP = "2"; 
	
	private final static String EVENT_TYPE_TRANSACTION = "Transaction"; 
	
	private Database db;
	private Map<Integer,LrEventMapBean> lrEventMapTable = new HashMap<Integer,LrEventMapBean>(); 

	public LrRunAccessDatabase(String mdbFileName){
		try {
			this.db = DatabaseBuilder.open(new File(mdbFileName));
		} catch (IOException e) {
    		System.out.println( " LrRunAccessDatabase: Error - IO on reading opening access db file  " + mdbFileName );
			e.printStackTrace();
			throw new RuntimeException();
		}
		lrEventMapTable.putAll(extractEventMapTableFromMdb());
	}
	
	
	private Map<Integer,LrEventMapBean> extractEventMapTableFromMdb() {
		
		try {
			Table table = db.getTable("Event_map");

			for (Row row : table) {
				
				LrEventMapBean lrEventMapBean = new LrEventMapBean(); 
				lrEventMapBean.setEventId((Integer)row.get("Event ID"));
				lrEventMapBean.setEventName((String)row.get("Event Name"));
				lrEventMapBean.setEventType((String)row.get("Event Type"));

				lrEventMapTable.put(lrEventMapBean.getEventId(),  lrEventMapBean);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(" Runcheck: extractEventMapTableFromMdb Error - IO on reading access db  : Event_map" );
		}
		return lrEventMapTable;
	}
	
	
	public DateRangeBean getRunDateRangeUsingLoadrunnerAccessDB(String timeZone) {
		long runStartTime;
		long runEndTime;;
		int resultTableTimeZoneOffestMs;
		try {
			Table table = db.getTable("Result");
			Cursor cursor = CursorBuilder.createCursor(table);
			Byte onlyexpectedrow = 0;
			
			if ( cursor.findFirstRow(Collections.singletonMap("Result ID", onlyexpectedrow))){
				runStartTime  =  ((Integer)cursor.getCurrentRowValue(table.getColumn("Start Time"))).longValue() * 1000;
				runEndTime    =  ((Integer)cursor.getCurrentRowValue(table.getColumn("Result End Time"))).longValue() * 1000; 	
				resultTableTimeZoneOffestMs    =  ((Integer)cursor.getCurrentRowValue(table.getColumn("Time Zone"))).intValue()*1000;								
			} else {
				throw new RuntimeException("getRunDateRangeUsingLoadrunnerAccessDB: could not find expected Result ID=0 row on mdb Result Table "  );
			}
			
			// it appears the stored epoch time actually goes 1 hour ahead during Australian daylight savings, this 'hack' seems to cater for that   
			
			Date runStartTimeDate = new Date(runStartTime);
			GregorianCalendar runStartTimeCal = new GregorianCalendar();
			runStartTimeCal.setTimeZone(TimeZone.getTimeZone(timeZone));
			runStartTimeCal.setTime(runStartTimeDate);
			 
			TimeZone mTimeZone = runStartTimeCal.getTimeZone();  
			int gmtOffsetMs =  mTimeZone.getRawOffset() + (mTimeZone.inDaylightTime(runStartTimeDate) ? mTimeZone.getDSTSavings() : 0); 
			
			if (resultTableTimeZoneOffestMs + gmtOffsetMs != 0 ) {
				System.out.println("Run Times adjusted for recorded TimeZone GMT Offset of " + resultTableTimeZoneOffestMs*-1 + ",  actual TimeZone GMT Offest is " + gmtOffsetMs + " (msecs)" );
				System.out.println("    Timezone set as : " + mTimeZone.getID());				
				int mAdjustment =   resultTableTimeZoneOffestMs + gmtOffsetMs;
				runStartTime = runStartTime - mAdjustment;
				runEndTime   = runEndTime   - mAdjustment;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Runcheck: calculateAndSetRunTimesUsingLoadrunnerAccessDB Error - IO on reading access db  : Result" );
		}
		return new DateRangeBean(runStartTime, runEndTime );
	}
	
	
	public void loadTestTransactionForTransactionsOnlyFromLoadrunnAccessDB(String application, TestTransactionsDAO testTransactionsDAO, Long runStartTimeEpochMsecs ){

		System.out.println("Loading transactional data from loadrunner mdb Event_meter table..");
		
		List<TestTransaction> testTransactionList = new ArrayList<TestTransaction>();
		int lineCount = 0; 
		
		long startLoadms = System.currentTimeMillis(); 
		System.out.println("load Tansactional data From LoadrunnerAccessDB: starts at " + new Date(startLoadms));		
		
		
		try {
			Table table = db.getTable("Event_meter");

			for (Row row : table) {
			
				Integer currentEventMeterEventId =  (Integer)row.get("Event ID");
				// System.out.println("storeTransactionalData: currentEventMeterEventId = " + currentEventMeterEventId  );
				
				if (lrEventMapTable.containsKey(currentEventMeterEventId) &&
					EVENT_TYPE_TRANSACTION.equals( lrEventMapTable.get(currentEventMeterEventId).eventType )){
				
			    	if ( (lineCount % 100 )   == 0 ){
			    		testTransactionsDAO.insertMultiple(testTransactionList);
			    		testTransactionList.clear();
			    	};
			    	if ( (lineCount % 10000 ) == 0 ){	System.out.print(" (" + lineCount + ").."); };
			    	lineCount++;
				
					LrEventMeterBean lrEventMeterBean = new LrEventMeterBean();
					lrEventMeterBean.setEventId(currentEventMeterEventId);
					
					//  The txn recored time "End Time" is the relative end time in seconds to 3 decimals of the txn.  Storing as epoch time in msecs  
					Double txnSecsFromStart =  (Double)row.get("End Time");	
					Long txnEpochTimeMsecs = new Double( runStartTimeEpochMsecs + txnSecsFromStart * 1000 ).longValue();
					lrEventMeterBean.setEndTime(txnEpochTimeMsecs.toString()); 
					
					BigDecimal rawValue  = new BigDecimal((Double)row.get("Value"     )).setScale(6, RoundingMode.HALF_UP);
					BigDecimal thinkTime = new BigDecimal((Double)row.get("Think Time")).setScale(6, RoundingMode.HALF_UP);					
					lrEventMeterBean.setValue( rawValue.subtract(thinkTime));  			

					lrEventMeterBean.setStatus1(String.valueOf((Short)row.get("Status1")));
					
			
					TestTransaction testTransaction = new TestTransaction();
					testTransaction.setTxnId(lrEventMapTable.get(currentEventMeterEventId).eventName);
					testTransaction.setTxnType(AppConstants.MAPPED_DATA_TYPES.TRANSACTION.name() );
					testTransaction.setTxnResult(lrEventMeterBean.getValue());		
					
					if (STATUS1_FAIL.equals(lrEventMeterBean.getStatus1())){
						testTransaction.setTxnPassed("N");
					} else if (STATUS1_PASS.equals(lrEventMeterBean.getStatus1())){	
						testTransaction.setTxnPassed("Y");	
					} else if (STATUS1_STOP.equals(lrEventMeterBean.getStatus1())){	
						testTransaction.setTxnPassed(AppConstants.TXN_STOPPPED_STATUS);	
					} else {
						testTransaction.setTxnPassed("N");
						testTransaction.setTxnId(lrEventMapTable.get(currentEventMeterEventId).eventName + "_UNKNOWN_STATUS" );
					}
					
					testTransaction.setTxnEpochTime(lrEventMeterBean.getEndTime());
			
		    		testTransaction.setApplication(application);
		    		testTransaction.setRunTime(AppConstants.RUN_TIME_YET_TO_BE_CALCULATED);	
		    		testTransactionList.add(testTransaction);
				}

			} // end of event_meter table 
		
		} catch (IOException e) {
			System.out.println( " Runcheck: extractEventMapTableFromMdb Error - IO on reading access db  :"   );
			e.printStackTrace();
			throw new RuntimeException();
		}
		testTransactionsDAO.insertMultiple(testTransactionList);
		testTransactionList.clear();
		
		long endLoadms = System.currentTimeMillis(); 	    
		System.out.println(" ..(" + lineCount + ")");
		System.out.println("load Tansactional data From LoadrunnerAccessDB completed  " + new Date(endLoadms) +  ".   Load took " + (endLoadms -startLoadms)/1000 + " secs" );		
	}
	

	
	public List<Transaction> extractSystemMetricEventsFromMDB(Run run, EventMappingDAO eventMappingDAO, DateRangeBean dateRangeBean, DateRangeBean filteredDateRangeBean ) {
		
		List<Transaction> eventTransactions = new ArrayList<Transaction>();  
		List<EventAttributes> metricEventsToBeExtracted = new ArrayList<EventAttributes>();
		
		System.out.println("---------------------------------------------------------------------------------------- " );
		System.out.println("Print out for table Event_map  ( Event ID :  Event Type :  Event Name ) " );

		for (LrEventMapBean lrEventMapBean : lrEventMapTable.values()) {
		 
				System.out.println("  " + lrEventMapBean.getEventId() 
								+ " : " + lrEventMapBean.getEventType().replaceAll("Error", "Err")       // if 'error' events get mapped, renamed to prevent a log parser causing red condition !
								+ " : " + lrEventMapBean.getEventName().replaceAll("Error", "Err"));
				
				if ( ! EVENT_TYPE_TRANSACTION.equals(lrEventMapBean.getEventType())){   //Transactions handled separately
					metricEventsToBeExtracted.addAll(findMetricsToBeReportedForThisMdbEventId(lrEventMapBean, eventMappingDAO) );
				}

		}	
		System.out.println("-------------------  " + metricEventsToBeExtracted.size()); 

		// now go thru each required system metric event id (eg an idle or utilisation percentage ) to get the actual metric values from the  appropriate "xxx_meter" tables in the LR mdb file
		
		for (EventAttributes eventAttributes : metricEventsToBeExtracted) {
			try {
				eventTransactions.add(extractMetricsFromLoadrunnerMdbMeterTables(run, eventAttributes, dateRangeBean, filteredDateRangeBean));
			} catch (IOException e) {
				System.out.println( " Runcheck: extractSystemMetricEventsFromMDB Error - IO on reading access db  :"   );
				e.printStackTrace();
				throw new RuntimeException();
			}
		}   
	
		return eventTransactions;
	}
	

	/**	
	 * 	For Loadrunner we only match against the Event Mapping Reference Table, we only use the 'MATCH_WHEN_LIKE' column of the event table.
	 * 	So for example a  MATCH_WHEN_LIKE value of 'DataPoint:myfavouritedp' would match an entry on the LR Event Map Table of Event Type = 'DataPoint' and Event Name = 'myfavouritedp' 
	 * 	The METRIC_SOURCE column is later used to tell what LR table to find the recored events on (either DataPoint_meteror Monitor_Metor)  
	 */
	private List<EventAttributes> findMetricsToBeReportedForThisMdbEventId(LrEventMapBean lrEventMapBean, EventMappingDAO eventMappingDAO ){
		
		Integer mdbEventId = lrEventMapBean.getEventId();
		String mdbEventType = lrEventMapBean.getEventType();
		String mdbEventName = lrEventMapBean.getEventName();
//		System.out.println("            findMetricsToBeReportedForThisMdbEventId : " + lrEventMapBean.getEventId() +":"+lrEventMapBean.getEventType()+":"+lrEventMapBean.getEventName() );
		
		List<EventAttributes> reportedEventAttributes = new ArrayList<EventAttributes>();		
		List<EventMapping> lrMetricsEventMappings = eventMappingDAO.findEventMappingsForPerformanceTool(AppConstants.LOADRUNNER); 				
		
		boolean lrEventNameMatched = false;
		int i = 0;
		String txnId = null;
		
		while (i < lrMetricsEventMappings.size() && !lrEventNameMatched ){

			EventMapping eventMapping = lrMetricsEventMappings.get(i);
			
			boolean lrEventNameMatchFound = eventMappingDAO.doesLrEventMapEntryMatchThisEventMapping(mdbEventType, mdbEventName, eventMapping);   
	
			
			if ( lrEventNameMatchFound ){
				txnId = Utils.deriveEventTxnIdUsingEventMappingBoundaryRules(mdbEventName, eventMapping);
			}
		
			if ( StringUtils.isNotBlank(txnId)){
				lrEventNameMatched = true;
				System.out.println("      matched LrEvent Id : " + mdbEventId + " (" + mdbEventType + ") to mapping entry \"" 
						+ eventMapping.getMatchWhenLike() + "\".  Mapped Txn type = " + eventMapping.getTxnType() + ", Txn Id = " + txnId    );
				reportedEventAttributes.add(new EventAttributes(mdbEventId, txnId,  eventMapping));
			}
			i++;
		}
		return reportedEventAttributes;
	}
	

	private Transaction extractMetricsFromLoadrunnerMdbMeterTables(Run run, EventAttributes eventAttributes, DateRangeBean dateRangeBean, DateRangeBean filteredDateRangeBean ) throws IOException {

		Integer eventId =  eventAttributes.getEventId();
		BigDecimal value = new BigDecimal(0.000000).setScale(6, RoundingMode.HALF_UP)   ;
		Integer eventInstanceID = new Integer(0);
		Double eventSecsFromStart = new Double(0.0);
		boolean filterOutThisEvent = false;
		
		BigDecimal totalOfValues = new BigDecimal(0.0).setScale(6, RoundingMode.HALF_UP);

		Long countPointsAtBottleneckThreshold = new Long(0);
		Long count = new Long(0);	
		
		BigDecimal txnMinimum =  new BigDecimal(-1.0).setScale(3, BigDecimal.ROUND_HALF_UP);
		BigDecimal txnMaximum =  new BigDecimal(-1.0).setScale(3, BigDecimal.ROUND_HALF_UP);				

		Integer minEventInstanceId =  null;		
		Integer maxEventInstanceId =  null;		
		BigDecimal txnFirst =  new BigDecimal(-1.0).setScale(3, BigDecimal.ROUND_HALF_UP);		
		BigDecimal txnLast  =  new BigDecimal(-1.0).setScale(3, BigDecimal.ROUND_HALF_UP);		
		boolean firstTimeThru = true;	
		
		BigDecimal ninteyPercent = new BigDecimal(90.0);
		
		if  ( !AppConstants.METRIC_SOURCE_LOADRUNNER_MONITOR_METER.equals(eventAttributes.getEventMapping().getMetricSource())  &&
			  !AppConstants.METRIC_SOURCE_LOADRUNNER_DATAPOINT_METER.equals(eventAttributes.getEventMapping().getMetricSource()) ) {	
			throw new RuntimeException("Unexpected soure for a Loadrunner metric event. "
					+ "Expected " + AppConstants.METRIC_SOURCE_LOADRUNNER_MONITOR_METER + " or " +  AppConstants.METRIC_SOURCE_LOADRUNNER_DATAPOINT_METER  
					+ " but got " + eventAttributes.getEventMapping().getMetricSource());
		}
		
		String lrTableForThisEvent = AppConstants.getToolDataTypeToSourceValueMap().get( eventAttributes.getEventMapping().getMetricSource());
		Table table = db.getTable(lrTableForThisEvent);               //  Monitor_meter or DataPoint_meter for a Loadrunner run
		Cursor cursor = CursorBuilder.createCursor(table);
		
		while (cursor.findNextRow(Collections.singletonMap("Event ID", eventId ))){

			value =  new BigDecimal((Double)cursor.getCurrentRowValue(table.getColumn("Value"))).setScale(6, RoundingMode.HALF_UP);
			eventInstanceID    =  (Integer)cursor.getCurrentRowValue(table.getColumn("Event Instance ID"));
			eventSecsFromStart =  (Double)cursor.getCurrentRowValue(table.getColumn("End Time"));			
			
			filterOutThisEvent = false;
			if  (filteredDateRangeBean.isFilterApplied() ) {
				Long eventEpochTimeMsecs = new Double( dateRangeBean.getRunStartTime() +  eventSecsFromStart * 1000 ).longValue();
				if ( eventEpochTimeMsecs < filteredDateRangeBean.getRunStartTime() || eventEpochTimeMsecs > filteredDateRangeBean.getRunEndTime() ) {
					filterOutThisEvent = true;
				}
			}
			
			if (!filterOutThisEvent) {
			
				if (firstTimeThru) {
					txnMinimum = value;
					txnMaximum = value;
					minEventInstanceId = eventInstanceID;
					maxEventInstanceId = eventInstanceID;
					txnFirst = value;
					txnLast = value;
					firstTimeThru = false;
				}
		
				if ( eventInstanceID <  minEventInstanceId  ){
					txnFirst = value;
				}
				if ( eventInstanceID >  maxEventInstanceId  ){
					txnLast = value;
				}			
	
				if ( value.compareTo(txnMinimum) < 0  ){			
					txnMinimum = value;
				}
				if ( value.compareTo(txnMinimum) > 0  ){				
					txnMaximum = value;
				}			
				
				// if the metric value is a %idle, we need to invert it to turn it into a %utilisation ..
				if (eventAttributes.getEventMapping().getIsInvertedPercentage().equals("Y") ){
					value = value.subtract( new BigDecimal(100.0)).negate();
				}
				
				//calculation of server utilisation average takes the total of all points captured, then divides that by number of points (also may be useful for certain Datapoints) 
				totalOfValues =  totalOfValues.add(value);
	
				//calculation of time spent over 90% of metric (or <10% inverted percentage like idle).  Takes the number of points at >90%  and uses total number of point to get % 'bottlenecked'
				if (eventAttributes.getEventMapping().getIsPercentage().equals("Y") &&  value.compareTo(ninteyPercent) > 0 ){
					countPointsAtBottleneckThreshold = countPointsAtBottleneckThreshold + 1;
				}
		
				count = count +1;
				
//				System.out.println("value = " + value + ",  count = " + count  );
			}  //filter 
		}
		
		//average
		BigDecimal tnxAverage =   totalOfValues.divide(new BigDecimal(count), 3, RoundingMode.HALF_UP  );
		
		DecimalFormat df = new DecimalFormat("#.00");
		
		//time above 90% threshold
		Double percentSpendAtBottleneckThreshold = ( countPointsAtBottleneckThreshold / count.doubleValue()  ) * 100.0;
		String percentSpendAtBottleneckThresholdStr =   df.format(percentSpendAtBottleneckThreshold);
				
		System.out.println("extracted Metric: " + eventAttributes.getTxnId() + " " + eventAttributes.getEventMapping().getTxnType()  
				+ "  eventAve = " + tnxAverage + " util% = " + tnxAverage +	", 90th% threshold = " + percentSpendAtBottleneckThresholdStr + ", count was " + count );	
		
		Transaction serverTransaction = new Transaction();
		serverTransaction.setApplication(run.getApplication());
		serverTransaction.setRunTime(run.getRunTime()); 
		serverTransaction.setTxnId(eventAttributes.getTxnId()); 
		serverTransaction.setTxnType(eventAttributes.getEventMapping().getTxnType());  
		serverTransaction.setTxnAverage(tnxAverage); 
		serverTransaction.setTxnMinimum(txnMinimum); 		      		
		serverTransaction.setTxnMaximum(txnMaximum);
		serverTransaction.setTxnStdDeviation(new BigDecimal(-1.0));

		serverTransaction.setTxn90th(new BigDecimal(-1.0));
		if (eventAttributes.getEventMapping().getIsPercentage().equals("Y")){
			serverTransaction.setTxn90th(new BigDecimal(percentSpendAtBottleneckThresholdStr));
		}		
		serverTransaction.setTxnPass(count);
		serverTransaction.setTxnFail(new Long(-1).longValue() );
		serverTransaction.setTxnStop(new Long(-1).longValue() );
		
		serverTransaction.setTxnFirst(txnFirst);
		serverTransaction.setTxnLast(txnLast);
		serverTransaction.setTxnSum(totalOfValues);		

		return serverTransaction;
	}

}
