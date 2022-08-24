package com.mark59.datahunter.api.rest.samples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.mark59.datahunter.api.application.DataHunterConstants;
import com.mark59.datahunter.api.data.beans.Policies;
import com.mark59.datahunter.api.model.AsyncMessageaAnalyzerResult;
import com.mark59.datahunter.api.model.CountPoliciesBreakdown;
import com.mark59.datahunter.api.model.DataHunterRestApiResponsePojo;
import com.mark59.datahunter.api.rest.DataHunterRestApiClient;


/**
 * Detailed use cases and verifications for the DataHunter Rest API client and service.:
 * 
 * @author Philip Webb
 * Written: Australian Spring 2022
 *
 */
public class DataHunterRestApiClientSampleUsage {
	
	
	/**
	 * This is functionally equivalent to the DataHunterSeleniumFunctionalTest.asyncLifeCycleTestWithUseabilityUpdate() web application test 
	 * in the dataHunterFunctionalTest project (held on the mark-5-9/mark59-xtras GitHub repo), but running the REST API instead of the web 
	 * 'Asynchronous Message Analyzer' function.
	 *  
	 * @param dhApiClient DataHunterRestApiClient
	 */
	public void asyncLifeCycleTestWithUseabilityUpdate(DataHunterRestApiClient dhApiClient) {

		dhApiClient.deleteMultiplePolicies("TESTAPI_ASYNC_TOUSED", null, null);
		dhApiClient.addPolicy( new Policies("TESTAPI_ASYNC_TOUSED", "T99-testonly-01", "FIRSTONE", "UNPAIRED", "", 1460613152000L));
		dhApiClient.addPolicy( new Policies("TESTAPI_ASYNC_TOUSED", "T99-testonly-01", "between",  "UNPAIRED", "", 1460613152009L));
		dhApiClient.addPolicy( new Policies("TESTAPI_ASYNC_TOUSED", "T99-testonly-01", "LASTONE",  "UNPAIRED", "", 1460613153001L));
		dhApiClient.addPolicy( new Policies("TESTAPI_ASYNC_TOUSED", "T99-testonly-02", "FIRSTONE", "UNPAIRED", "", 1460613153000L));
		dhApiClient.addPolicy( new Policies("TESTAPI_ASYNC_TOUSED", "T99-testonly-02", "LASTONE",  "UNPAIRED", "", 1460613155001L));
		
		DataHunterRestApiResponsePojo response = dhApiClient.asyncMessageAnalyzer(DataHunterConstants.EQUALS,"TESTAPI_ASYNC_TOUSED", null, null, "USED");

		int i=0;
		List<AsyncMessageaAnalyzerResult>  asyncResults = response.getAsyncMessageaAnalyzerResults();
		System.out.println( "    asyncMessageAnalyzerPrintResults  (" + asyncResults.size() + ") - asyncLifeCycleTestWithUseabilityUpdate" );		
		System.out.println( "    -------------------------------- ");		
		for (AsyncMessageaAnalyzerResult asyncResult : asyncResults) {
			System.out.println("    " +  ++i + "   " + asyncResult);
		}
		
		assertEquals(2, asyncResults.size());		
		assertEquals("[application=TESTAPI_ASYNC_TOUSED, startsWith=null, identifier=T99-testonly-02, lifecycle=null, useability=USED, selectOrder=null], starttm= 1460613153000, endtm= 1460613155001, differencetm= 2001]", asyncResults.get(0).toString());
		assertEquals("[application=TESTAPI_ASYNC_TOUSED, startsWith=null, identifier=T99-testonly-01, lifecycle=null, useability=USED, selectOrder=null], starttm= 1460613152000, endtm= 1460613153001, differencetm= 1001]", asyncResults.get(1).toString());
		
		for (AsyncMessageaAnalyzerResult  pairedAsyncTxn : asyncResults ) {
			// example of a typical transaction name you could set (and its response time)
			System.out.println( "    Txn Name :  "  + pairedAsyncTxn.getApplication() + "_" + pairedAsyncTxn.getIdentifier() + "  Respsonse time (Assumed msecs) : "  + pairedAsyncTxn.getDifferencetm()  );				
		}
		System.out.println( "    -------------------------------- ");	
 	    // clean up     
		assertEquals(Integer.valueOf(5), dhApiClient.deleteMultiplePolicies("TESTAPI_ASYNC_TOUSED", null, null).getRowsAffected());
	}
	
	
	/**
	 * @param dhApiClient DataHunterRestApiClient
	 */
	public void basicPolicyAddPrintDeleteChecks(DataHunterRestApiClient dhApiClient){
//		System.out.println("DataHunterRestApiResponsePojo =" + response  );

		DataHunterRestApiResponsePojo response = dhApiClient.deletePolicy("testapi", "id1", "");	
		assertEquals(String.valueOf(true), response.getSuccess() ); 
		response = dhApiClient.deletePolicy("testapi", "id2", null);	
		assertEquals(String.valueOf(true), response.getSuccess() ); 
		response = dhApiClient.deletePolicy("testapi", "id3", "setepochtime");	
		assertEquals(String.valueOf(true), response.getSuccess() ); 
		response = dhApiClient.deletePolicy("testapi", "id3", "setepochtime");	
		assertEquals(String.valueOf(true), response.getSuccess() ); 
		
		response = dhApiClient.addPolicy(new Policies("testapi","id1", "", "USED", null, null));			
		assertEquals(String.valueOf(true), response.getSuccess() ); 
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		
		response = dhApiClient.printPolicy("testapi", "id1");			
		assertEquals(String.valueOf(true), response.getSuccess() ); 
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		assertsOnPolicy(new Policies("testapi","id1", "", "USED", "", null), response.getPolicies().get(0));
		
		dhApiClient.addPolicy(new Policies("testapi","id1", "duplicatedid", "USED", "", null));
		
		response = dhApiClient.printPolicy("testapi", "id1");	
		assertEquals(String.valueOf(true), response.getSuccess() ); 
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		assertEquals(1, response.getPolicies().size()); 		
		assertsOnPolicy(new Policies("testapi","id1", "", "USED", "", null), response.getPolicies().get(0));
		
		response = dhApiClient.printPolicy("testapi", "id1", "");	
		assertEquals(String.valueOf(true), response.getSuccess() ); 
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		assertsOnPolicy(new Policies("testapi","id1", "", "USED", "", null), response.getPolicies().get(0));
		
		response = dhApiClient.printPolicy("testapi", "id1", null);	
		assertEquals(String.valueOf(true), response.getSuccess() ); 
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		assertsOnPolicy(new Policies("testapi","id1", "", "USED", "", null), response.getPolicies().get(0));
		
		response = dhApiClient.printPolicy("testapi", "id1", "duplicatedid");	
		assertEquals(String.valueOf(true), response.getSuccess()); 
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		assertsOnPolicy(new Policies("testapi","id1", "duplicatedid", "USED", "", null), response.getPolicies().get(0));
		
		response = dhApiClient.printPolicy("testapi", "doesnotexist", "duplicatedid");	
		assertEquals(String.valueOf(false), response.getSuccess()); 
		assertEquals(Integer.valueOf(0), response.getRowsAffected());
		assertEquals(0, response.getPolicies().size()); 
		
		dhApiClient.addPolicy(new Policies("testapi","id2", "", "USED", "", null));	
		
		response = dhApiClient.deletePolicy("testapi", "id1", ""); 
		assertEquals(String.valueOf(true), response.getSuccess()); 
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		assertEquals(1, response.getPolicies().size()); 
		assertEquals("", response.getFailMsg()); 
		assertsOnPolicy(new Policies("testapi","id1", "", null, null, null), response.getPolicies().get(0));

		response = dhApiClient.deletePolicy("testapi", "id1","duplicatedid"); 
		assertEquals(String.valueOf(true), response.getSuccess()); 
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		assertEquals(1, response.getPolicies().size()); 
		assertEquals("", response.getFailMsg()); 
		assertsOnPolicy(new Policies("testapi","id1", "duplicatedid", null, null, null), response.getPolicies().get(0));	

		response = dhApiClient.deletePolicy("testapi", "id1","duplicatedid"); 
		assertEquals(String.valueOf(true), response.getSuccess()); 
		assertEquals(Integer.valueOf(0), response.getRowsAffected());
		assertEquals(1, response.getPolicies().size()); 
		assertEquals("No rows matching the selection.", response.getFailMsg()); 
		assertsOnPolicy(new Policies("testapi","id1", "duplicatedid", null, null, null), response.getPolicies().get(0));			
	
		response = dhApiClient.deletePolicy("testapi", "id2",""); 
		assertEquals(String.valueOf(true), response.getSuccess()); 
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		assertEquals("", response.getFailMsg()); 
		assertsOnPolicy(new Policies("testapi","id2", "", null, null, null), response.getPolicies().get(0));	
		
		response = dhApiClient.addPolicy(new Policies("testapi","id3", "setepochtime", "UNUSED", "otherstuff", 1643673346936L));			
		assertEquals(String.valueOf(true), response.getSuccess()); 
		assertEquals(1, response.getPolicies().size()); 
		assertsOnPolicy(new Policies("testapi","id3", "setepochtime", "UNUSED", "otherstuff", 1643673346936L), response.getPolicies().get(0));
		
		response = dhApiClient.printPolicy("testapi","id3", "setepochtime");			
		assertEquals(String.valueOf(true), response.getSuccess()); 
		assertEquals(1, response.getPolicies().size()); 
		assertsOnPolicy(new Policies("testapi","id3", "setepochtime", "UNUSED", "otherstuff", 1643673346936L), response.getPolicies().get(0));

		response = dhApiClient.printPolicy("testapi","id3");			
		assertEquals(String.valueOf(false), response.getSuccess()); 
		assertEquals(0, response.getPolicies().size()); 
		assertEquals("No rows matching the selection.", response.getFailMsg()); 
		
		response = dhApiClient.addPolicy(new Policies("testapi","id3", "setepochtime", "USED", "ALREADYEXISTS!!", 1643673346936L));			
		assertEquals(String.valueOf(false), response.getSuccess()); 
		assertEquals(1, response.getPolicies().size()); 
		assertsOnPolicy(new Policies("testapi","id3", "setepochtime", "UNUSED", "ALREADYEXISTS!!", 1643673346936L), response.getPolicies().get(0));
		assertTrue("error should contain application (testapi)" , response.getFailMsg().contains("testapi") ); 
		assertTrue("error should contain idenifier (id3)" , response.getFailMsg().contains("id3") ); 
		assertTrue("error should contain lifecycle (setepochtime)" , response.getFailMsg().contains("setepochtime") ); 
		
		response = dhApiClient.deletePolicy("testapi", "id3", "setepochtime");	
		assertEquals(String.valueOf(true), response.getSuccess() ); 
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
	}
	
	
	/**
	 * @param dhApiClient DataHunterRestApiClient
	 */
	public void workingWithMultiplePolicies(DataHunterRestApiClient dhApiClient){
		
		create6testPolices(dhApiClient);	
		DataHunterRestApiResponsePojo response = dhApiClient.deleteMultiplePolicies("nonexistingapp", null, null);
		assertEquals(String.valueOf(true), response.getSuccess() ); 
		assertEquals(Integer.valueOf(0), response.getRowsAffected());
		response = dhApiClient.deleteMultiplePolicies("testapi", null, "USED");
		assertEquals(Integer.valueOf(3), response.getRowsAffected());
		response = dhApiClient.deleteMultiplePolicies("testapi", null, "USED");
		assertEquals(Integer.valueOf(0), response.getRowsAffected());
		response = dhApiClient.deleteMultiplePolicies("testapi", null, "");
		assertEquals(Integer.valueOf(2), response.getRowsAffected());
		response = dhApiClient.deleteMultiplePolicies("otherapp", "", "");
		assertEquals(Integer.valueOf(1), response.getRowsAffected());

		
		create6testPolices(dhApiClient);	
		response = dhApiClient.printSelectedPolicies("testapi", null, null);
		assertEquals(Integer.valueOf(5), response.getRowsAffected());
		assertEquals(5, response.getPolicies().size());
		assertTrue(response.getPolicies().get(0).toString().startsWith("[application=testapi, identifier=im4, lifecycle=nonblanklc, useability=UNUSED, otherdata=,"));
		assertTrue(response.getPolicies().get(1).toString().startsWith("[application=testapi, identifier=im3, lifecycle=duplicatedid, useability=REUSABLE, otherdata=duplicated id,"));
		assertTrue(response.getPolicies().get(2).toString().startsWith("[application=testapi, identifier=im3, lifecycle=nonblanklc, useability=USED, otherdata=otherdata3,"));
		assertTrue(response.getPolicies().get(3).toString().startsWith("[application=testapi, identifier=im2, lifecycle=, useability=USED, otherdata=,"));
		assertTrue(response.getPolicies().get(4).toString().startsWith("[application=testapi, identifier=im1, lifecycle=, useability=USED, otherdata=,"));
		
		response = dhApiClient.printSelectedPolicies("testapi", null, "USED");
		assertEquals(3, response.getPolicies().size());		
		assertTrue(response.getPolicies().get(0).toString().startsWith("[application=testapi, identifier=im3, lifecycle=nonblanklc, useability=USED, otherdata=otherdata3,"));
		assertTrue(response.getPolicies().get(1).toString().startsWith("[application=testapi, identifier=im2, lifecycle=, useability=USED, otherdata=,"));
		assertTrue(response.getPolicies().get(2).toString().startsWith("[application=testapi, identifier=im1, lifecycle=, useability=USED, otherdata=,"));		
	
		response = dhApiClient.printSelectedPolicies("testapi", "nonblanklc", "");
		assertEquals(2, response.getPolicies().size());		
		assertTrue(response.getPolicies().get(0).toString().startsWith("[application=testapi, identifier=im4, lifecycle=nonblanklc, useability=UNUSED, otherdata=,"));
		assertTrue(response.getPolicies().get(1).toString().startsWith("[application=testapi, identifier=im3, lifecycle=nonblanklc, useability=USED, otherdata=otherdata3,"));	

		response = dhApiClient.printSelectedPolicies("testapi", "nonblanklc", "USED");
		assertEquals(1, response.getPolicies().size());		
		assertTrue(response.getPolicies().get(0).toString().startsWith("[application=testapi, identifier=im3, lifecycle=nonblanklc, useability=USED, otherdata=otherdata3,"));

		response = dhApiClient.printSelectedPolicies("doesntexist", "nonblanklc", "USED");	
		assertEquals(0, response.getPolicies().size());				
		
		
		response = dhApiClient.deleteMultiplePolicies("testapi", "nonblanklc", null);
		assertEquals(Integer.valueOf(2), response.getRowsAffected());
		response = dhApiClient.printSelectedPolicies("testapi", "", "");
		assertEquals(Integer.valueOf(3), response.getRowsAffected());
		response = dhApiClient.deleteMultiplePolicies("testapi", "", "USED");
		assertEquals(Integer.valueOf(2), response.getRowsAffected());
		response = dhApiClient.printSelectedPolicies("testapi", null, null);
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		assertTrue(response.getPolicies().get(0).toString().startsWith("[application=testapi, identifier=im3, lifecycle=duplicatedid, useability=REUSABLE, otherdata=duplicated id,"));
	}


	
	/**
	 * @param dhApiClient DataHunterRestApiClient
	 */
	public void policyCountsAndBreakdowns(DataHunterRestApiClient dhApiClient){
		create6testPolices(dhApiClient);
		
		DataHunterRestApiResponsePojo response = dhApiClient.countPolicies("testapi", "nonblanklc", "USED");
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		assertEquals("[[application=testapi, identifier=null, lifecycle=nonblanklc, useability=USED, otherdata=null, created=null, updated=null, epochtime=null]]", response.getPolicies().toString()); 
		
		response = dhApiClient.countPoliciesBreakdown(DataHunterConstants.EQUALS, "testapi", "nonblanklc", "USED");
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		assertEquals("[[application=testapi, identifier=null, lifecycle=nonblanklc, useability=USED, otherdata=EQUALS, created=null, updated=null, epochtime=null]]", response.getPolicies().toString());
		assertEquals(1, response.getCountPoliciesBreakdown().size());	
		assertEquals("[[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=USED, selectOrder=null], rowCount=1]]", response.getCountPoliciesBreakdown().toString());
	
		assertEquals(Integer.valueOf(5), dhApiClient.countPolicies("testapi", null, null).getRowsAffected());

		response = dhApiClient.countPoliciesBreakdown(DataHunterConstants.EQUALS, "testapi", null, null);
		assertEquals(4, response.getCountPoliciesBreakdown().size());		
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=, useability=USED, selectOrder=null], rowCount=2]", 				response.getCountPoliciesBreakdown().get(0).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=duplicatedid, useability=REUSABLE, selectOrder=null], rowCount=1]", response.getCountPoliciesBreakdown().get(1).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=UNUSED, selectOrder=null], rowCount=1]", 	response.getCountPoliciesBreakdown().get(2).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=USED, selectOrder=null], rowCount=1]", 		response.getCountPoliciesBreakdown().get(3).toString());
				
		assertEquals(Integer.valueOf(5), dhApiClient.countPolicies("testapi", "", "").getRowsAffected() );

		response = dhApiClient.countPoliciesBreakdown(DataHunterConstants.EQUALS, "testapi", "", "");
		assertEquals(4, response.getCountPoliciesBreakdown().size());		
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=, useability=USED, selectOrder=null], rowCount=2]", 				response.getCountPoliciesBreakdown().get(0).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=duplicatedid, useability=REUSABLE, selectOrder=null], rowCount=1]", response.getCountPoliciesBreakdown().get(1).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=UNUSED, selectOrder=null], rowCount=1]", 	response.getCountPoliciesBreakdown().get(2).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=USED, selectOrder=null], rowCount=1]", 		response.getCountPoliciesBreakdown().get(3).toString());
		
		assertEquals(Integer.valueOf(0), dhApiClient.countPolicies("nonexisting", "", "").getRowsAffected() );

		response = dhApiClient.countPoliciesBreakdown(DataHunterConstants.EQUALS, "nonexisting", "", "");
		assertEquals(0, response.getCountPoliciesBreakdown().size());		
		assertEquals("sql execution OK, but no rows matched the selection criteria.",  response.getFailMsg());		
		assertEquals(String.valueOf(true), response.getSuccess());		
		
		assertEquals(Integer.valueOf(3), dhApiClient.countPolicies("testapi", "",  "USED").getRowsAffected() );
		
		response = dhApiClient.countPoliciesBreakdown(DataHunterConstants.EQUALS, "testapi", "", "USED");
		assertEquals(2, response.getCountPoliciesBreakdown().size());		
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=, useability=USED, selectOrder=null], rowCount=2]", 				response.getCountPoliciesBreakdown().get(0).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=USED, selectOrder=null], rowCount=1]", 		response.getCountPoliciesBreakdown().get(1).toString());

		assertEquals(Integer.valueOf(0), dhApiClient.countPolicies("testapi", "nonexistingc",  "").getRowsAffected() );
		assertEquals(0, dhApiClient.countPoliciesBreakdown(DataHunterConstants.EQUALS, "testapi", "nonexistingc",  "").getCountPoliciesBreakdown().size()); 

		assertEquals(Integer.valueOf(1), dhApiClient.countPolicies("testapi", null,  "UNUSED").getRowsAffected() );

		response = dhApiClient.countPoliciesBreakdown(DataHunterConstants.EQUALS, "testapi", null,  "UNUSED");
		assertEquals(1, response.getCountPoliciesBreakdown().size());		
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=UNUSED, selectOrder=null], rowCount=1]", 	response.getCountPoliciesBreakdown().get(0).toString());
		
		assertEquals(Integer.valueOf(2), dhApiClient.countPolicies("testapi", "nonblanklc", "").getRowsAffected() );

		response = dhApiClient.countPoliciesBreakdown(DataHunterConstants.EQUALS, "testapi", "nonblanklc", "");
		assertEquals(2, response.getCountPoliciesBreakdown().size());		
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=UNUSED, selectOrder=null], rowCount=1]", 	response.getCountPoliciesBreakdown().get(0).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=USED, selectOrder=null], rowCount=1]", 		response.getCountPoliciesBreakdown().get(1).toString());
	}	
	
	
	/**
	 * Note: this method clears the DataHunter database of all existing data
	 * @param dhApiClient DataHunterRestApiClient
	 */
	public void policyCountBreakdownsUsingStartWith(DataHunterRestApiClient dhApiClient){
		clearDatabase(dhApiClient); 

		create6testPolices(dhApiClient);
		assertEquals(5, dhApiClient.countPoliciesBreakdown(DataHunterConstants.STARTS_WITH, "", null, null).getCountPoliciesBreakdown().size()); 
		
		dhApiClient.addPolicy(new Policies("test api","ex1", "", "UNUSED", "", null)); 			
		dhApiClient.addPolicy(new Policies("testaB_pi","ex2", "nonblanklc", "USED", "", null));
		dhApiClient.addPolicy(new Policies("testaC%pi:&? @=+","ex3", "lc with$char-s", "USED", "", null)); 
		dhApiClient.addPolicy(new Policies("testaC%pi:&? @=+","ex4", "lc with$char-s", "USED", "", null));
		
		DataHunterRestApiResponsePojo response = dhApiClient.countPoliciesBreakdown(DataHunterConstants.STARTS_WITH, "", null, null);		
		assertEquals(8, response.getCountPoliciesBreakdown().size()); 
		assertEquals("[application=otherapp, startsWith=null, identifier=null, lifecycle=, useability=UNUSED, selectOrder=null], rowCount=1]", 					 response.getCountPoliciesBreakdown().get(0).toString());
		assertEquals("[application=test api, startsWith=null, identifier=null, lifecycle=, useability=UNUSED, selectOrder=null], rowCount=1]", 					 response.getCountPoliciesBreakdown().get(1).toString());
		assertEquals("[application=testaB_pi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=USED, selectOrder=null], rowCount=1]", 		 response.getCountPoliciesBreakdown().get(2).toString());
		assertEquals("[application=testaC%pi:&? @=+, startsWith=null, identifier=null, lifecycle=lc with$char-s, useability=USED, selectOrder=null], rowCount=2]",response.getCountPoliciesBreakdown().get(3).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=, useability=USED, selectOrder=null], rowCount=2]", 					 response.getCountPoliciesBreakdown().get(4).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=duplicatedid, useability=REUSABLE, selectOrder=null], rowCount=1]", 	 response.getCountPoliciesBreakdown().get(5).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=UNUSED, selectOrder=null], rowCount=1]", 		 response.getCountPoliciesBreakdown().get(6).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=USED, selectOrder=null], rowCount=1]", 			 response.getCountPoliciesBreakdown().get(7).toString());

		assertEquals(7, dhApiClient.countPoliciesBreakdown(DataHunterConstants.STARTS_WITH, "test", null, null).getCountPoliciesBreakdown().size()); 
		assertEquals(4, dhApiClient.countPoliciesBreakdown(DataHunterConstants.STARTS_WITH, "testapi", null, null).getCountPoliciesBreakdown().size()); 
		assertEquals(1, dhApiClient.countPoliciesBreakdown(DataHunterConstants.STARTS_WITH, "test ", null, null).getCountPoliciesBreakdown().size()); 

		response = dhApiClient.countPoliciesBreakdown(DataHunterConstants.STARTS_WITH, "test", "lc with$char-s", null);		
		assertEquals(1, response.getCountPoliciesBreakdown().size()); 
		assertEquals("[application=testaC%pi:&? @=+, startsWith=null, identifier=null, lifecycle=lc with$char-s, useability=USED, selectOrder=null], rowCount=2]",response.getCountPoliciesBreakdown().get(0).toString());

		response = dhApiClient.countPoliciesBreakdown(DataHunterConstants.STARTS_WITH, "", null, "USED");		
		assertEquals(4, response.getCountPoliciesBreakdown().size()); 
		assertEquals("[application=testaB_pi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=USED, selectOrder=null], rowCount=1]", 		 response.getCountPoliciesBreakdown().get(0).toString());
		assertEquals("[application=testaC%pi:&? @=+, startsWith=null, identifier=null, lifecycle=lc with$char-s, useability=USED, selectOrder=null], rowCount=2]",response.getCountPoliciesBreakdown().get(1).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=, useability=USED, selectOrder=null], rowCount=2]", 					 response.getCountPoliciesBreakdown().get(2).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=USED, selectOrder=null], rowCount=1]", 			 response.getCountPoliciesBreakdown().get(3).toString());
		
		response = dhApiClient.countPoliciesBreakdown(DataHunterConstants.STARTS_WITH, "testa", "nonblanklc", "USED");		
		assertEquals(2, response.getCountPoliciesBreakdown().size()); 
		assertEquals("[application=testaB_pi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=USED, selectOrder=null], rowCount=1]", 		 response.getCountPoliciesBreakdown().get(0).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=USED, selectOrder=null], rowCount=1]", 			 response.getCountPoliciesBreakdown().get(1).toString());
		
		//clean up
		assertEquals(Integer.valueOf(1), dhApiClient.deleteMultiplePolicies("otherapp", null, null).getRowsAffected());
		assertEquals(Integer.valueOf(1), dhApiClient.deleteMultiplePolicies("test api", null, null).getRowsAffected());
		assertEquals(Integer.valueOf(1), dhApiClient.deleteMultiplePolicies("testaB_pi", null, null).getRowsAffected());
		assertEquals(Integer.valueOf(2), dhApiClient.deleteMultiplePolicies("testaC%pi:&? @=+", null, null).getRowsAffected());
		assertEquals(Integer.valueOf(5), dhApiClient.deleteMultiplePolicies("testapi", null, null).getRowsAffected());
		assertEquals(0, dhApiClient.countPoliciesBreakdown(DataHunterConstants.STARTS_WITH, "", null, null).getCountPoliciesBreakdown().size()); 
	}


	/**
	 * @param dhApiClient DataHunterRestApiClient
	 */
	public void workingWithUseStateChanges(DataHunterRestApiClient dhApiClient){
		create6testPolices(dhApiClient);
		DataHunterRestApiResponsePojo response = dhApiClient.updatePoliciesUseState("testapi", null, "USED", "UNUSED", null);
		assertEquals(Integer.valueOf(3), response.getRowsAffected());

		response = dhApiClient.useNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_MOST_RECENTLY_ADDED);
		assertEquals(String.valueOf(true), response.getSuccess()); 
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		assertsOnPolicy(new Policies("testapi","im4", "nonblanklc", "UNUSED", "", null), response.getPolicies().get(0));		

		response = dhApiClient.useNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_MOST_RECENTLY_ADDED);
		assertEquals(String.valueOf(true), response.getSuccess()); 
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		assertsOnPolicy(new Policies("testapi","im3", "nonblanklc", "UNUSED", "otherdata3", null), response.getPolicies().get(0));
	
		response = dhApiClient.useNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_MOST_RECENTLY_ADDED);
		assertEquals(String.valueOf(true), response.getSuccess()); 
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		assertsOnPolicy(new Policies("testapi","im2", "", "UNUSED", "", null), response.getPolicies().get(0));

		response = dhApiClient.useNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_MOST_RECENTLY_ADDED);
		assertEquals(String.valueOf(true), response.getSuccess()); 
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		assertsOnPolicy(new Policies("testapi","im1", "", "UNUSED", "", null), response.getPolicies().get(0));
		
		response = dhApiClient.useNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_MOST_RECENTLY_ADDED);
		assertEquals(String.valueOf(false), response.getSuccess()); 
		assertEquals(Integer.valueOf(0), response.getRowsAffected());
		assertEquals("No rows matching the selection.  Possibly we have ran out of data for application:[testapi]", response.getFailMsg()); 	
			
		response = dhApiClient.useNextPolicy("testapi", null, "REUSABLE", DataHunterConstants.SELECT_MOST_RECENTLY_ADDED);
		assertEquals(String.valueOf(true), response.getSuccess()); 
		assertEquals(Integer.valueOf(0), response.getRowsAffected());
		assertsOnPolicy(new Policies("testapi","im3", "duplicatedid", "REUSABLE", "duplicated id", null), response.getPolicies().get(0));		
		assertEquals("Policy im3 NOT updated as it is marked as REUSABLE", response.getFailMsg());		
			
		create6testPolices(dhApiClient);
		response = dhApiClient.updatePoliciesUseState("testapi", null, "", "UNUSED", null);
		assertEquals(Integer.valueOf(5), response.getRowsAffected());
		response = dhApiClient.lookupNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_OLDEST_ENTRY);
		assertsOnPolicy(new Policies("testapi","im1", "", "UNUSED", "", null), response.getPolicies().get(0));		
		response = dhApiClient.useNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_OLDEST_ENTRY);
		assertsOnPolicy(new Policies("testapi","im1", "", "UNUSED", "", null), response.getPolicies().get(0));		
		response = dhApiClient.useNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_OLDEST_ENTRY);
		assertsOnPolicy(new Policies("testapi","im2", "", "UNUSED", "", null), response.getPolicies().get(0));	
		response = dhApiClient.useNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_OLDEST_ENTRY);		
		assertsOnPolicy(new Policies("testapi","im3", "nonblanklc", "UNUSED", "otherdata3", null), response.getPolicies().get(0));	
		response = dhApiClient.lookupNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_OLDEST_ENTRY);
		assertsOnPolicy(new Policies("testapi","im3", "duplicatedid", "UNUSED", "duplicated id", null), response.getPolicies().get(0));	
		response = dhApiClient.lookupNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_OLDEST_ENTRY);
		assertsOnPolicy(new Policies("testapi","im3", "duplicatedid", "UNUSED", "duplicated id", null), response.getPolicies().get(0));	
		response = dhApiClient.useNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_OLDEST_ENTRY);
		assertsOnPolicy(new Policies("testapi","im3", "duplicatedid", "UNUSED", "duplicated id", null), response.getPolicies().get(0));			
		response = dhApiClient.useNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_OLDEST_ENTRY);
		assertsOnPolicy(new Policies("testapi","im4", "nonblanklc", "UNUSED", "", null), response.getPolicies().get(0));
		response = dhApiClient.lookupNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_OLDEST_ENTRY);
		assertEquals("No rows matching the selection.  Possibly we have ran out of data for application:[testapi]", response.getFailMsg()); 		
		response = dhApiClient.useNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_OLDEST_ENTRY);
		assertEquals("No rows matching the selection.  Possibly we have ran out of data for application:[testapi]", response.getFailMsg()); 			
		
		response = dhApiClient.updatePoliciesUseState("testapi", "im3", "USED", "UNUSED", null);
		assertEquals(Integer.valueOf(2), response.getRowsAffected());
		response = dhApiClient.useNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_OLDEST_ENTRY);		
		assertsOnPolicy(new Policies("testapi","im3", "nonblanklc", "UNUSED", "otherdata3", null), response.getPolicies().get(0));		
		response = dhApiClient.useNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_OLDEST_ENTRY);
		assertsOnPolicy(new Policies("testapi","im3", "duplicatedid", "UNUSED", "duplicated id", null), response.getPolicies().get(0));			
		response = dhApiClient.useNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_OLDEST_ENTRY);
		assertEquals("No rows matching the selection.  Possibly we have ran out of data for application:[testapi]", response.getFailMsg()); 		
	
		assertEquals(Integer.valueOf(2), dhApiClient.updatePoliciesUseState("testapi", "im3", "USED", "UNUSED", null).getRowsAffected());
		assertEquals(Integer.valueOf(1), dhApiClient.updatePoliciesUseState("testapi", "im4", "USED", "UNUSED", null).getRowsAffected());
		response = dhApiClient.useNextPolicy("testapi", "nonblanklc", "UNUSED", DataHunterConstants.SELECT_OLDEST_ENTRY);		
		assertsOnPolicy(new Policies("testapi","im3", "nonblanklc", "UNUSED", "otherdata3", null), response.getPolicies().get(0));	
		response = dhApiClient.useNextPolicy("testapi", "nonblanklc", "UNUSED", DataHunterConstants.SELECT_OLDEST_ENTRY);
		assertsOnPolicy(new Policies("testapi","im4", "nonblanklc", "UNUSED", "", null), response.getPolicies().get(0));	
		response = dhApiClient.useNextPolicy("testapi", "nonblanklc", "UNUSED", DataHunterConstants.SELECT_OLDEST_ENTRY);
		assertEquals("No rows matching the selection.  Possibly we have ran out of data for application:[testapi]", response.getFailMsg()); 		
	}	
	
	/**
	 * Note: this method clears the DataHunter database of all existing data
	 * @param dhApiClient DataHunterRestApiClient
	 */
	public void workingWithAsyncMessages(DataHunterRestApiClient dhApiClient) {
		clearDatabase(dhApiClient);
		insertPolicySets(dhApiClient, "testapi-async", "t01-", 5); 
		DataHunterRestApiResponsePojo response = dhApiClient.printSelectedPolicies("testapi-async", null, "UNPAIRED");
		assertEquals(Integer.valueOf(20), response.getRowsAffected());
		assertEquals(20, response.getPolicies().size());
				
		dhApiClient.deleteMultiplePolicies("norowsfound", null, null);
		//response = dhApiClient.asyncMessageAnalyzer(DataHunterConstants.STARTS_WITH,"TESTAPI_ASYNC_HIGH_VOL", null, "UNPAIRED", "USED");
		response = dhApiClient.asyncMessageAnalyzer(DataHunterConstants.EQUALS,"norowsfound", null, null, null);
		assertEquals(0, response.getAsyncMessageaAnalyzerResults().size());		
		assertEquals(Integer.valueOf(0), response.getRowsAffected());

		response = dhApiClient.asyncMessageAnalyzer(DataHunterConstants.EQUALS,"testapi-async", null, null, null);		
		assertEquals(5, response.getAsyncMessageaAnalyzerResults().size());	
		assertEquals(Integer.valueOf(5), response.getRowsAffected());
	
		insertPolicySets(dhApiClient, "testapi-like", "t02-", 1);
		assertEquals(Integer.valueOf(8), dhApiClient.countPoliciesBreakdown(DataHunterConstants.STARTS_WITH, "testapi-", "", "UNPAIRED").getRowsAffected());
		response = dhApiClient.asyncMessageAnalyzer(DataHunterConstants.STARTS_WITH ,"testapi-", null, null, null);			
		assertEquals(6, response.getAsyncMessageaAnalyzerResults().size());				
		assertTrue(response.getAsyncMessageaAnalyzerResults().get(0).toString().startsWith("[application=testapi-like, startsWith=null, identifier=t02-testonly-1, lifecycle=null, useability=UNPAIRED"));
		assertTrue(response.getAsyncMessageaAnalyzerResults().get(1).toString().startsWith("[application=testapi-async, startsWith=null, identifier=t01-testonly-5, lifecycle=null, useability=UNPAIRED"));
		assertTrue(response.getAsyncMessageaAnalyzerResults().get(2).toString().startsWith("[application=testapi-async, startsWith=null, identifier=t01-testonly-4, lifecycle=null, useability=UNPAIRED"));
		assertTrue(response.getAsyncMessageaAnalyzerResults().get(3).toString().startsWith("[application=testapi-async, startsWith=null, identifier=t01-testonly-3, lifecycle=null, useability=UNPAIRED"));
		assertTrue(response.getAsyncMessageaAnalyzerResults().get(4).toString().startsWith("[application=testapi-async, startsWith=null, identifier=t01-testonly-2, lifecycle=null, useability=UNPAIRED"));
		assertTrue(response.getAsyncMessageaAnalyzerResults().get(5).toString().startsWith("[application=testapi-async, startsWith=null, identifier=t01-testonly-1, lifecycle=null, useability=UNPAIRED"));

		response = dhApiClient.asyncMessageAnalyzer(DataHunterConstants.EQUALS ,"testapi-async", "t01-testonly-4", null, "USED");			
		assertEquals(1, response.getAsyncMessageaAnalyzerResults().size());				
		assertTrue(response.getAsyncMessageaAnalyzerResults().get(0).toString().startsWith("[application=testapi-async, startsWith=null, identifier=t01-testonly-4, lifecycle=null, useability=USED"));
		assertEquals(3, dhApiClient.printSelectedPolicies("testapi-async", null, "USED").getPolicies().size());
		response = dhApiClient.asyncMessageAnalyzer(DataHunterConstants.STARTS_WITH ,"testapi-async", "t01-testonly-3", null, "USED");			
		assertEquals(1, response.getAsyncMessageaAnalyzerResults().size());				
		assertTrue(response.getAsyncMessageaAnalyzerResults().get(0).toString().startsWith("[application=testapi-async, startsWith=null, identifier=t01-testonly-3, lifecycle=null, useability=USED"));
		assertEquals(6, dhApiClient.printSelectedPolicies("testapi-async", null, "USED").getPolicies().size());

		response = dhApiClient.asyncMessageAnalyzer(DataHunterConstants.EQUALS ,"testapi-async", "t01-someother-5", null, "USED");			
		assertEquals(0, response.getAsyncMessageaAnalyzerResults().size());				
	}


	private void clearDatabase(DataHunterRestApiClient dhApiClient) {
		DataHunterRestApiResponsePojo response = dhApiClient.countPoliciesBreakdown(DataHunterConstants.STARTS_WITH, "", null, null);		
		List<CountPoliciesBreakdown> countPoliciesBreakdownList = response.getCountPoliciesBreakdown();
		for (CountPoliciesBreakdown cpb : countPoliciesBreakdownList) {
			dhApiClient.deleteMultiplePolicies(cpb.getApplication(), cpb.getLifecycle(), cpb.getUseability()); 
		}
		assertEquals(0, dhApiClient.countPoliciesBreakdown(DataHunterConstants.STARTS_WITH, "", null, null).getCountPoliciesBreakdown().size());
	}	
	
	
	private void insertPolicySets(DataHunterRestApiClient dhApiClient, String application, String idPrefex, int numPoliciesSetsToBeCreate) {
		dhApiClient.deleteMultiplePolicies(application, null, null);
		for (int i = 1; i <= numPoliciesSetsToBeCreate; i++) {
			dhApiClient.addPolicy( new Policies(application, idPrefex+"testonly-" + i, "FIRSTONE", "UNPAIRED", "", null));
			dhApiClient.addPolicy( new Policies(application, idPrefex+"testonly-" + i, "between",  "UNPAIRED", "", null));
			dhApiClient.addPolicy( new Policies(application, idPrefex+"someother-"+ i, "other",    "UNPAIRED", "", null));			
		} 
		for (int i = 1; i <= numPoliciesSetsToBeCreate; i++) { 
			try {Thread.sleep(2);} catch (Exception e){} // ensure a time gap
			dhApiClient.addPolicy( new Policies(application, idPrefex+"testonly-" + i, "LASTONE",  "UNPAIRED", "", null));		
		} 		
	}
	
	
	private void create6testPolices(DataHunterRestApiClient dhApiClient) {
		dhApiClient.deleteMultiplePolicies("testapi", null, null);
		dhApiClient.deleteMultiplePolicies("otherapp", null, null);
		dhApiClient.addPolicy(new Policies("testapi","im1", "", "USED", "", null));	
		try {Thread.sleep(2);} catch (Exception e){}   // guarantee no two rows have matching 'created' (for sorts)
		dhApiClient.addPolicy(new Policies("testapi","im2", "", "USED", null, null));
		try {Thread.sleep(2);} catch (Exception e){}
		dhApiClient.addPolicy(new Policies("testapi","im3", "nonblanklc", "USED", "otherdata3", null));	
		try {Thread.sleep(2);} catch (Exception e){}
		dhApiClient.addPolicy(new Policies("testapi","im3", "duplicatedid", "REUSABLE", "duplicated id", null));
		try {Thread.sleep(2);} catch (Exception e){}
		dhApiClient.addPolicy(new Policies("testapi","im4", "nonblanklc", "UNUSED", "", null));	
		try {Thread.sleep(2);} catch (Exception e){}
		dhApiClient.addPolicy(new Policies("otherapp","io1", "", "UNUSED", null, null));
	}	
	
	
	private void assertsOnPolicy(Policies expectedPolicy, Policies actualPolicy) {
		assertEquals(expectedPolicy.getApplication(), actualPolicy.getApplication()); 
		assertEquals(expectedPolicy.getIdentifier(), actualPolicy.getIdentifier()); 
		assertEquals(expectedPolicy.getLifecycle(), actualPolicy.getLifecycle()); 
		assertEquals(expectedPolicy.getOtherdata(), actualPolicy.getOtherdata());
		if (expectedPolicy.getEpochtime() != null) {
			assertEquals(expectedPolicy.getOtherdata(), actualPolicy.getOtherdata()); 
		}
	}

	
	/**
	 * runs each of the sample use cases against a local dataHunter instance
	 * @param args none required
	 */
	public static void main(String[] args) {
		System.out.println("running DataHunterRestApiClientSampleUsage ..");
		DataHunterRestApiClient dhApiClient = new DataHunterRestApiClient("http://localhost:8081/mark59-datahunter"  );
		DataHunterRestApiClientSampleUsage sample = new DataHunterRestApiClientSampleUsage();
		sample.basicPolicyAddPrintDeleteChecks(dhApiClient);
		sample.workingWithMultiplePolicies(dhApiClient);
		sample.policyCountsAndBreakdowns(dhApiClient);
		sample.policyCountBreakdownsUsingStartWith(dhApiClient);  // this method clears the database !!
		sample.workingWithUseStateChanges(dhApiClient);
		sample.workingWithAsyncMessages(dhApiClient);
		sample.asyncLifeCycleTestWithUseabilityUpdate(dhApiClient);	
		System.out.println("completed DataHunterRestApiClientSampleUsage run");
	}

}
