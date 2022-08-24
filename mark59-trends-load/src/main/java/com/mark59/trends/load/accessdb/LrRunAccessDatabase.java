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

package com.mark59.trends.load.accessdb;

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

import org.apache.commons.lang3.StringUtils;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.CursorBuilder;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import com.mark59.core.utils.Mark59Constants;
import com.mark59.trends.application.AppConstantsMetrics;
import com.mark59.trends.application.UtilsTrends;
import com.mark59.trends.data.beans.DateRangeBean;
import com.mark59.trends.data.beans.EventMapping;
import com.mark59.trends.data.beans.Run;
import com.mark59.trends.data.beans.TestTransaction;
import com.mark59.trends.data.beans.Transaction;
import com.mark59.trends.data.eventMapping.dao.EventMappingDAO;
import com.mark59.trends.data.testTransactions.dao.TestTransactionsDAO;
import com.mark59.trends.load.run.EventAttributes;

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
	
	private final Database db;
	private final Map<Integer,LrEventMapBean> lrEventMapTable = new HashMap<>();

	/**
	 * Connects to the Loadrunner Access database, and stores the Event_Map table. 
	 * <b>The 'Event_Map' provides a key list of EventIDs, which map to an 'Event Type' and 'Event Name' (which is the name  matched against).
	 * <p>It is necessary use this list because it determines what table the EventIDs for a given transaction are on. Basically: 
	 * <p>'Transaction' eventsIds are on the 'Event_meter' table.
	 * <p>'SiteScope' events are on the 'Monitor_meter' table.
	 * <p>'DataPoints' events are on the  'DataPoint_meter'.
	 * <p>FYI, there are other events (such as 'Web' and 'Connections') however only DataPoint and SiteScope events are mapped by mark59.
	 * 
	 * @param mdbFileName   a Loadruner access database
	 */
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
			throw new RuntimeException(" TrendsLoad: extractEventMapTableFromMdb Error - IO on reading access db  : Event_map" );
		}
		return lrEventMapTable;
	}
	
	
	public DateRangeBean getRunDateRangeUsingLoadrunnerAccessDB(String timeZone) {
		long runStartTime;
		long runEndTime;
		int resultTableTimeZoneOffestMs;
		try {
			Table table = db.getTable("Result");
			Cursor cursor = CursorBuilder.createCursor(table);
			Byte onlyexpectedrow = 0;
			
			if ( cursor.findFirstRow(Collections.singletonMap("Result ID", onlyexpectedrow))){
				runStartTime  =  ((Integer)cursor.getCurrentRowValue(table.getColumn("Start Time"))).longValue() * 1000;
				runEndTime    =  ((Integer)cursor.getCurrentRowValue(table.getColumn("Result End Time"))).longValue() * 1000; 	
				resultTableTimeZoneOffestMs    = (Integer) cursor.getCurrentRowValue(table.getColumn("Time Zone")) *1000;
			} else {
				throw new RuntimeException("getRunDateRangeUsingLoadrunnerAccessDB: could not find expected Result ID=0 row on mdb Result Table "  );
			}
			
			// it appears the stored epoch time actually goes 1 hour ahead during Australian daylight savings, this 'hack' seems to cater for that   
			
			if (StringUtils.isBlank(timeZone)) {
				timeZone = new GregorianCalendar().getTimeZone().getID();
				System.out.println("The 'timeZone'(z) parameter was blank! Assuming a timezone id of : " + timeZone);	
			}	
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
			throw new RuntimeException("TrendsLoad: calculateAndSetRunTimesUsingLoadrunnerAccessDB Error - IO on reading access db  : Result" );
		}
		return new DateRangeBean(runStartTime, runEndTime );
	}
	
	
	/**
	 * Loads all 'Transaction' events on the LR Event_meter table into the mark59 TestTrasactions table, in a similar process to how Trasaction events are
	 * handled with the other Tools).   System Metrics and DataPoints are handled separately.
	 *    
	 * @param application  applicationID
	 * @param testTransactionsDAO  testTransactionsDAO instance
	 * @param runStartTimeEpochMsecs  the run start time
	 */
	public void loadTestTransactionForTransactionsOnlyFromLoadrunnAccessDB(String application, TestTransactionsDAO testTransactionsDAO, Long runStartTimeEpochMsecs ){

		System.out.println("Loading transactional data from loadrunner mdb Event_meter table..");
		
		List<TestTransaction> testTransactionList = new ArrayList<>();
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
			    	}
					if ( (lineCount % 10000 ) == 0 ){	System.out.print(" (" + lineCount + ").."); }
					lineCount++;
				
					LrEventMeterBean lrEventMeterBean = new LrEventMeterBean();
					lrEventMeterBean.setEventId(currentEventMeterEventId);
					
					//  The txn recored time "End Time" is the relative end time in seconds to 3 decimals of the txn.  Storing as epoch time in msecs  
					Double txnSecsFromStart =  (Double)row.get("End Time");	
					long txnEpochTimeMsecs = Double.valueOf(runStartTimeEpochMsecs + txnSecsFromStart * 1000 ).longValue();
					lrEventMeterBean.setEndTime(Long.toString(txnEpochTimeMsecs));
					
					BigDecimal rawValue  = BigDecimal.valueOf((Double) row.get("Value")).setScale(6, RoundingMode.HALF_UP);
					BigDecimal thinkTime = BigDecimal.valueOf((Double) row.get("Think Time")).setScale(6, RoundingMode.HALF_UP);
					lrEventMeterBean.setValue( rawValue.subtract(thinkTime));  			

					lrEventMeterBean.setStatus1(String.valueOf(row.get("Status1")));
					
			
					TestTransaction testTransaction = new TestTransaction();
					testTransaction.setTxnId(lrEventMapTable.get(currentEventMeterEventId).eventName);
					testTransaction.setTxnType(Mark59Constants.DatabaseTxnTypes.TRANSACTION.name() );
					testTransaction.setTxnResult(lrEventMeterBean.getValue());		
					
					if (STATUS1_FAIL.equals(lrEventMeterBean.getStatus1())){
						testTransaction.setTxnPassed("N");
					} else if (STATUS1_PASS.equals(lrEventMeterBean.getStatus1())){	
						testTransaction.setTxnPassed("Y");	
					} else if (STATUS1_STOP.equals(lrEventMeterBean.getStatus1())){	
						testTransaction.setTxnPassed(AppConstantsMetrics.TXN_STOPPPED_STATUS);	
					} else {
						testTransaction.setTxnPassed("N");
						testTransaction.setTxnId(lrEventMapTable.get(currentEventMeterEventId).eventName + "_UNKNOWN_STATUS" );
					}
					
					testTransaction.setTxnEpochTime(lrEventMeterBean.getEndTime());
			
		    		testTransaction.setApplication(application);
		    		testTransaction.setRunTime(AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED);	
		    		testTransaction.setIsCdpTxn("N");	
		    		testTransactionList.add(testTransaction);
				}

			} // end of event_meter table 
		
		} catch (IOException e) {
			System.out.println( " TrendsLoad: extractEventMapTableFromMdb Error - IO on reading access db  :"   );
			e.printStackTrace();
			throw new RuntimeException();
		}
		testTransactionsDAO.insertMultiple(testTransactionList);
		testTransactionList.clear();
		
		long endLoadms = System.currentTimeMillis(); 	    
		System.out.println(" ..(" + lineCount + ")");
		System.out.println("load Tansactional data From LoadrunnerAccessDB completed  " + new Date(endLoadms) +  ".   Load took " + (endLoadms -startLoadms)/1000 + " secs" );		
	}

	
	/**
	 * Create the mark59 system metric and dataPoint transactions for the run.
	 * <p>First, it goes though the saved LR Event_map, and examines each Event Name to see what event we want to capture for the run (as listed in the mark59 
	 * EventMapping table. In more detail:
	 * <ul>
	 * <li>For Loadrunner the 'MATCH_WHEN_LIKE' column of the mark59 EventMapping table is used to match against the LR Event Name (extracted from the LR Event Map table).
	 * <li>Also, LR Event Type (extracted from the LR Event Map table) must match match against the METRIC_SOURCE column of the mark59 EventMapping table.  This is done by:
	 * <ul>
	 * <li>LR Event type of 'SiteScope' will only match against EventMapping METRIC_SOURCE entries of 'Loadrunner_SiteScope'  
	 * <li>LR Event type of 'DataPoint' will only match against EventMapping METRIC_SOURCE entries of 'Loadrunner_DataPoint'  
	 * </ul>
	 * <li>When a match is found, other entries from the mark59 EventMapping table are used to determine the txnId in mark95 (using left/right boundaries against LR Event Name,
	 * and the mark59 transaction type (using the TXN_TYPE column).  
	 * </ul>
	 * <p><p>Then in the second part of the process, for each identified event, it goes to the LR table (Monitor_meter or DataPoint_meter) holding that Event Id,
	 *  and extract the LR data to create the mark59 transaction for that event. 
	 * 
	 * @param run  Run
	 * @param eventMappingDAO eventMappingDAO instance
	 * @param dateRangeBean  the range date of the test
	 * @param filteredDateRangeBean  filteredDateRangeBean
	 * @return eventTransactions  - system metric and dataPoint transactions
	 */
	public List<Transaction> extractSystemMetricEventsFromMDB(Run run, EventMappingDAO eventMappingDAO, DateRangeBean dateRangeBean, DateRangeBean filteredDateRangeBean ) {
		
		List<Transaction> eventTransactions = new ArrayList<>();
		List<EventAttributes> metricEventsToBeExtracted = new ArrayList<>();
		
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

		// now go thru each required system metric event id (eg an CPU or Memory metric) to get the actual metric values from the  appropriate "xxx_meter" tables in the LR mdb file
		
		for (EventAttributes eventAttributes : metricEventsToBeExtracted) {
			try {
				eventTransactions.add(extractMetricsFromLoadrunnerMdbMeterTables(run, eventAttributes, dateRangeBean, filteredDateRangeBean));
			} catch (IOException e) {
				System.out.println( " TrendsLoad: extractSystemMetricEventsFromMDB Error - IO on reading access db  :"   );
				e.printStackTrace();
				throw new RuntimeException();
			}
		}   
	
		return eventTransactions;
	}
	

	/*	
	 * See if a LR event (Id and Type) matches an entry on mark59 EventMapping table.
	 * The mark59 txnId is also determined using the left/right boundary rules on the eventMapping entry. 
	 */
	private List<EventAttributes> findMetricsToBeReportedForThisMdbEventId(LrEventMapBean lrEventMapBean, EventMappingDAO eventMappingDAO ){
		
		Integer mdbEventId = lrEventMapBean.getEventId();
		String mdbEventType = lrEventMapBean.getEventType();
		String mdbEventName = lrEventMapBean.getEventName();
//		System.out.println("            findMetricsToBeReportedForThisMdbEventId : " + lrEventMapBean.getEventId() +":"+lrEventMapBean.getEventType()+":"+lrEventMapBean.getEventName() );
		
		List<EventAttributes> reportedEventAttributes = new ArrayList<>();
		List<EventMapping> mark59MetricsEventMappings = eventMappingDAO.findEventMappingsForPerformanceTool(AppConstantsMetrics.LOADRUNNER); 				
		
		boolean lrEventNameMatched = false;
		int i = 0;
		String txnId = null;
		
		while (i < mark59MetricsEventMappings.size() && !lrEventNameMatched ){

			EventMapping mark59EventMapping = mark59MetricsEventMappings.get(i);
			
			boolean lrEventNameMatchFound = eventMappingDAO.doesLrEventMapEntryMatchThisEventMapping(mdbEventType, mdbEventName, mark59EventMapping);   
	
			
			if ( lrEventNameMatchFound ){
				txnId = UtilsTrends.deriveEventTxnIdUsingEventMappingBoundaryRules(mdbEventName, mark59EventMapping);
			}
		
			if ( StringUtils.isNotBlank(txnId)){
				lrEventNameMatched = true;
				System.out.println("      matched LrEvent Id : " + mdbEventId + " (" + mdbEventType + ") to mapping entry \"" 
						+ mark59EventMapping.getMatchWhenLike() + "\".  Mapped Txn type = " + mark59EventMapping.getTxnType() + ", Txn Id = " + txnId    );
				reportedEventAttributes.add(new EventAttributes(mdbEventId, txnId,  mark59EventMapping));
			}
			i++;
		}
		return reportedEventAttributes;
	}
	

	private Transaction extractMetricsFromLoadrunnerMdbMeterTables(Run run, EventAttributes eventAttributes, DateRangeBean dateRangeBean, DateRangeBean filteredDateRangeBean ) throws IOException {

		Integer eventId =  eventAttributes.getEventId();
		BigDecimal value = new BigDecimal("0.000000").setScale(6, RoundingMode.HALF_UP)   ;
		Integer eventInstanceID;
		Double eventSecsFromStart;
		boolean filterOutThisEvent;
		
		BigDecimal totalOfValues = new BigDecimal("0.0").setScale(6, RoundingMode.HALF_UP);

		long countPointsAtBottleneckThreshold = 0L;
		long count = 0L;
		
		BigDecimal txnMinimum = BigDecimal.valueOf(-1.0).setScale(3, RoundingMode.HALF_UP);
		BigDecimal txnMaximum = BigDecimal.valueOf(-1.0).setScale(3, RoundingMode.HALF_UP);

		Integer minEventInstanceId =  null;		
		Integer maxEventInstanceId =  null;		
		BigDecimal txnFirst = BigDecimal.valueOf(-1.0).setScale(3, RoundingMode.HALF_UP);
		BigDecimal txnLast  = BigDecimal.valueOf(-1.0).setScale(3, RoundingMode.HALF_UP);
		boolean firstTimeThru = true;	
		
		BigDecimal ninteyPercent = new BigDecimal("90.0");
		
		if  ( !AppConstantsMetrics.METRIC_SOURCE_LOADRUNNER_MONITOR_METER.equals(eventAttributes.getEventMapping().getMetricSource())  &&
			  !AppConstantsMetrics.METRIC_SOURCE_LOADRUNNER_DATAPOINT_METER.equals(eventAttributes.getEventMapping().getMetricSource()) ) {	
			throw new RuntimeException("Unexpected soure for a Loadrunner metric event. "
					+ "Expected " + AppConstantsMetrics.METRIC_SOURCE_LOADRUNNER_MONITOR_METER + " or " +  AppConstantsMetrics.METRIC_SOURCE_LOADRUNNER_DATAPOINT_METER  
					+ " but got " + eventAttributes.getEventMapping().getMetricSource());
		}
		
		String lrTableForThisEvent = AppConstantsMetrics.getToolDataTypeToSourceValueMap().get( eventAttributes.getEventMapping().getMetricSource());
		Table table = db.getTable(lrTableForThisEvent);               //  Monitor_meter or DataPoint_meter for a Loadrunner run
		Cursor cursor = CursorBuilder.createCursor(table);
		
		while (cursor.findNextRow(Collections.singletonMap("Event ID", eventId ))){

			value = BigDecimal.valueOf((Double) cursor.getCurrentRowValue(table.getColumn("Value"))).setScale(6, RoundingMode.HALF_UP);
			eventInstanceID    =  (Integer)cursor.getCurrentRowValue(table.getColumn("Event Instance ID"));
			eventSecsFromStart =  (Double)cursor.getCurrentRowValue(table.getColumn("End Time"));			
			
			filterOutThisEvent = false;
			if  (filteredDateRangeBean.isFilterApplied() ) {
				long eventEpochTimeMsecs = Double.valueOf( dateRangeBean.getRunStartTime() +  eventSecsFromStart * 1000 ).longValue();
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
					value = value.subtract( new BigDecimal("100.0")).negate();
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
		Double percentSpendAtBottleneckThreshold = ( countPointsAtBottleneckThreshold / (double) count) * 100.0;
		String percentSpendAtBottleneckThresholdStr =   df.format(percentSpendAtBottleneckThreshold);
				
		System.out.println("extracted Metric: " + eventAttributes.getTxnId() + " " + eventAttributes.getEventMapping().getTxnType()  
				+ "  eventAve = " + tnxAverage + " util% = " + tnxAverage +	", 90th% threshold = " + percentSpendAtBottleneckThresholdStr + ", count was " + count );	
		
		Transaction serverTransaction = new Transaction();
		serverTransaction.setApplication(run.getApplication());
		serverTransaction.setRunTime(run.getRunTime()); 
		serverTransaction.setTxnId(eventAttributes.getTxnId()); 
		serverTransaction.setTxnType(eventAttributes.getEventMapping().getTxnType());  
		serverTransaction.setIsCdpTxn("N");  
		serverTransaction.setTxnMinimum(txnMinimum); 		      		
		serverTransaction.setTxnAverage(tnxAverage); 
		serverTransaction.setTxnMedian(BigDecimal.valueOf(-1.0));
		serverTransaction.setTxnMaximum(txnMaximum);
		serverTransaction.setTxnStdDeviation(BigDecimal.valueOf(-1.0));

		serverTransaction.setTxn90th(BigDecimal.valueOf(-1.0));
		if (eventAttributes.getEventMapping().getIsPercentage().equals("Y")){
			serverTransaction.setTxn90th(new BigDecimal(percentSpendAtBottleneckThresholdStr));
		}	
		serverTransaction.setTxn95th(BigDecimal.valueOf(-1.0));
		serverTransaction.setTxn99th(BigDecimal.valueOf(-1.0));
		serverTransaction.setTxnPass(count);
		serverTransaction.setTxnFail((long) -1);
		serverTransaction.setTxnStop((long) -1);
		
		serverTransaction.setTxnFirst(txnFirst);
		serverTransaction.setTxnLast(txnLast);
		serverTransaction.setTxnSum(totalOfValues);		
		serverTransaction.setTxnDelay(BigDecimal.valueOf(-1.0));

		return serverTransaction;
	}

}
