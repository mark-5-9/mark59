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

package com.mark59.metrics.graphic.data;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.mark59.metrics.application.UtilsMetrics;
import com.mark59.metrics.data.beans.Datapoint;
import com.mark59.metrics.data.beans.GraphMapping;
import com.mark59.metrics.data.transaction.dao.TransactionDAO;


/**
 * Originally written as a servlet using Goggle Graphics.  The underlying graphic software being used has since been changed to Vis.
 * 
 * See comments on the @createCsvData method for the details.
 * 
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class VisGraphicDataProduction  implements VisGraphicDataProductionInterface{

  @Autowired
  TransactionDAO transactionDAO;
  
  /**
   * <p>Returns the data points that are going to to be plotted on the graph as a list of 3D Cartesian coordinates. 
   * (the returned String has is these the coordinates points comma-limited, with line feed between each point).
   * 
   *  <p>This method effectively takes the list of Runs and Transaction Ids that are to be plotted, and combines the two to 
   *  create the x-y grid, with the z (vertical) axis being the value of the request metric. The x axis is date, 
   *  y axis is transaction id.  It also passes the axis labels for plotting. 
   *  
   *  <p>The ordering of the points in the list is relevant.  It needs to align with whatever x,y axis names are passed into
   *  the js load tool "visGraphLoad.js" (options xAxisNames, yAxisNames) in order for vis.js to draw the graph correctly.
   * 
   * @param application   application 
   * @param graph  graph (as per graphmapping table)
   * @param graphMappingUomDescription  usually expected to be getUomDescription
   * @param runDatesToGraph   (run dates to graph)
   * @param orderedTxnsToGraphIdList transaction ids to graph - ordering is not important  
   * @return data points being graphed 
   */
public String createDataPoints(String application, GraphMapping graphMapping, String runDatesToGraph, List<String> orderedTxnsToGraphIdList){	  

	ArrayList<Object[]> graphDataRows = generateDataPointCoordinates(application, graphMapping.getGraph(), runDatesToGraph, orderedTxnsToGraphIdList);

	StringBuilder graphDataPoints = new StringBuilder();
	
	// column descriptions (translates on graph to x axis, y axis, UOM label).
	graphDataPoints.append("\"run\",\" \",\"" + graphMapping.getUomDescription() + "\"\n") ;

	// output the values, using integer x,y values (possibly a bit OTT with the rounding, but might stop a quirky x.999999 type issue)
	// System.out.println("VisGraphicDataProduction: number of dataPoints = " + graphDataRows.size()     );
	
	for (Object[] dataRow : graphDataRows) {
		
		int plotPointx = (Integer)dataRow[0];		
		int plotPointy = (Integer)dataRow[1];	
		
		Object plotPointz =  Double.valueOf(dataRow[2].toString()); 
		if ((Double)plotPointz % 1 == 0 ) {  // its an integer!
			plotPointz = Integer.valueOf((int)Math.round(((Double)plotPointz).doubleValue()));
		} 		
		graphDataPoints.append(plotPointx + ", " + plotPointy + ", " + plotPointz + "\n");		
	}
	return graphDataPoints.toString();
  }

  

  private ArrayList<Object[]> generateDataPointCoordinates(String application, String graph, String runDatesToGraph, List<String> orderedTxnsToGraphIdList){

	ArrayList<String> masterRunsList  =  new ArrayList<String>(UtilsMetrics.commaDelimStringToStringList(runDatesToGraph));
	ArrayList<String> missingRunsList =  new ArrayList<String>(UtilsMetrics.commaDelimStringToStringList(runDatesToGraph));	
	
	ArrayList<Datapoint> datapoints   = new ArrayList<Datapoint>();

	if (masterRunsList.size() > 0 ) {
		datapoints = (ArrayList<Datapoint>)transactionDAO.findDatapointsToGraph(application, graph, runDatesToGraph, orderedTxnsToGraphIdList);
	}
//	System.out.println("orderedTxnsToGraphIdList : " + orderedTxnsToGraphIdList );
//	System.out.println("datapoints : " + datapoints);

	// Create a data table.
    ArrayList<Object[]> dataRows = new ArrayList<Object[]>();
    
 	String runTime  	= null;
	String txnId    	= null;
	BigDecimal datapointValue  = null;
	Datapoint datapoint = null;
    
    // Fill the data table.
    	
	int dateX = -1;
	String previousRunTime = null;
	int masterTxnsIndex = 0;
	boolean skipGetNext = false;
	boolean stillGotDataToProcess = true;
	
	int datapointIx = 0;
	int numDatapoints = datapoints.size();

	if (numDatapoints == 0 ){
		stillGotDataToProcess = false;
	} else {
		datapoint = datapoints.get(0); 
//    		System.out.println(" first datapoint to process : " + datapoint.toString()  );
	}
	
    	
	while (stillGotDataToProcess) {

		runTime = datapoint.getRunTime();
		txnId = datapoint.getTxnId();
		datapointValue = datapoint.getValue();

		if (!runTime.equals(previousRunTime)) {
//			System.out.println("change of run from " + previousRunTime + " to " + runTime);
			
			/* need to allow for non-existence of txns on the (prev) run that where at the end of the master txn list (i.e. the LHS of the graph) */
			while (masterTxnsIndex < orderedTxnsToGraphIdList.size() && !(previousRunTime == null)){
//				System.out.println("!! about to mark missed txns (on the lhs of graph) for " + orderedTxnsToGraphIdList.get(masterTxnsIndex) );
				dataRows.add(new Object[]{dateX, masterTxnsIndex, -1});
				
				masterTxnsIndex++;					
			}
		
			// remove the processed run from Missing Runs List
			missingRunsList.remove(runTime);
			
			previousRunTime = runTime;
			dateX = masterRunsList.indexOf(runTime);
			masterTxnsIndex = 0;
//		    System.out.println( "   processing " + runTime + " at index " + dateX  ) ;	
		}
		

		if (!(masterTxnsIndex >= orderedTxnsToGraphIdList.size())) {
//				System.out.println(" at : txnId =  " + txnId + ", trxnMasterIndex=" + masterTxnsIndex + ", masterTxnIdList=" + orderedTxnsToGraphIdList.get(masterTxnsIndex));
		}
		
		if (masterTxnsIndex >= orderedTxnsToGraphIdList.size()) {

			/* we have gone thru all the master txn list .. so any more* txns in this run should be skipped */
			
//				System.out.println(" !! at end of master list, skipping " + txnId);

		} else if (orderedTxnsToGraphIdList.get(masterTxnsIndex).compareTo(txnId) < 0) {

			/* implies a transaction(s) in the master txn list (those txns to be displayed on X axis) was not in this run.  Mark with neg number.
			 * See notes in TransactionDAOjdbcTemplateImpl.returnListOfSelectedTransactionIds(..) re ordering. */
				
//				System.out.println("!! about to mark missed txns for " + orderedTxnsToGraphIdList.get(masterTxnsIndex) + " ("+ txnId + ")");

			dataRows.add(new Object[]{dateX, masterTxnsIndex, -1});				

			skipGetNext = true;
			masterTxnsIndex++;

		} else if (orderedTxnsToGraphIdList.get(masterTxnsIndex).compareTo(txnId) > 0) {

	  		/* implies a transaction that is in this run does not exist in the master list (those txns to be displayed on X axis) ... skip over it */
//		  		System.out.println("!! skipping " + txnId + " (at master list element " + orderedTxnsToGraphIdList.get(masterTxnsIndex) + ")" ); 					
			
			
		} else {

			/* match found */
			
//				System.out.println("... match to txn found for datapoint rt=" + runTime + ", txnId=" + txnId + ", datapointValue=" + datapointValue + "(" + orderedTxnsToGraphIdList.get(masterTxnsIndex) + ")");
							
			dataRows.add(new Object[]{dateX, masterTxnsIndex, datapointValue});					
			masterTxnsIndex++;
		}

		if (skipGetNext) {
			skipGetNext = false;
//				System.out.println("!! for next loop, staying with " + txnId);
		} else {
			datapointIx++;
			if (datapointIx >= numDatapoints) {
				stillGotDataToProcess = false;
//					System.out.println(" no more datapoints ....");
			} else {
				datapoint = datapoints.get(datapointIx);
//					System.out.println(" next datapoint to process : " + datapoint.toString()  );
			}
		}

	} // end loop stillGotDataToProcess
	
	/* Need to allow for non-existence of txns on the earliest run that were at the end of the master txn list (i.e. at the back LHS of the graph) */
	while (masterTxnsIndex < orderedTxnsToGraphIdList.size() && !(previousRunTime == null)){
//		System.out.println("!! about to mark missed txns for the earliest run (on the lhs of graph) for " + orderedTxnsToGraphIdList.get(masterTxnsIndex) );
		dataRows.add(new Object[]{dateX, masterTxnsIndex, -1});				
		masterTxnsIndex++;					
	}	
	
	/* Finally, for runs that had no datapoints, mark all transactions as missing for the run */
	for (String missingRunTime : missingRunsList) {
		int missingRunIndex = masterRunsList.indexOf(missingRunTime);  
		for (masterTxnsIndex = 0; masterTxnsIndex < orderedTxnsToGraphIdList.size(); masterTxnsIndex++) {
//			System.out.println( " adding missing run : " + missingRunIndex + ", " + masterTxnsIndex + ", -1" ) ;						
			dataRows.add(new Object[]{missingRunIndex, masterTxnsIndex, -1});				
		}
	}  

    return dataRows;
  }


}
