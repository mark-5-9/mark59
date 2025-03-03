package com.mark59.datahunter.api.rest.samples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.mark59.datahunter.api.application.DataHunterConstants;
import com.mark59.datahunter.api.data.beans.Policies;
import com.mark59.datahunter.api.model.AsyncMessageaAnalyzerResult;
import com.mark59.datahunter.api.model.CountPoliciesBreakdown;
import com.mark59.datahunter.api.model.DataHunterRestApiResponsePojo;
import com.mark59.datahunter.api.model.PolicySelectionFilter;
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
	 * Example of using the the asyncMessageAnalyzer method of the REST API (the api equivalent of the 
	 * 'Asynchronous Message Analyzer' function in the DataHunter UI.
	 * 
	 * For an example of how to create JMeter transactions for the matched rows (in a script that does not
	 * require the use of selenium), refer to the sample script DataHunterLifecyclePvtNoSeleniumUsesRestApi
	 * in the mark59-scripting-samples project.
	 * 
	 * @param dhApiClient DataHunterRestApiClient
	 */
	public void asyncLifeCycleTestWithUseabilityUpdate(DataHunterRestApiClient dhApiClient) {
		System.out.println("	>> asyncLifeCycleTestWithUseabilityUpdate");

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
		System.out.println("	<< asyncLifeCycleTestWithUseabilityUpdate");
	}
	
	
	/**
	 * //clears testapi-..  rows
	 * @param dhApiClient DataHunterRestApiClient
	 */
	public void basicPolicyAddPrintUpdateDeleteChecks(DataHunterRestApiClient dhApiClient){
		System.out.println("	>> basicPolicyAddPrintDeleteChecks");
		
		DataHunterRestApiResponsePojo response = dhApiClient.deletePolicy("testapi", "id1", "");	
		assertEquals("Have you remembered to start the DataHunter under test?",   String.valueOf(true), response.getSuccess() ); 

		clearDatabase(dhApiClient, "testapi-");  
		
		response = dhApiClient.addPolicy(new Policies("testapi","  id1  ", "", "USED", null, null));			
		assertEquals(String.valueOf(true), response.getSuccess() ); 
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		
		response = dhApiClient.printPolicy("testapi", "id1");			
		assertEquals(String.valueOf(true), response.getSuccess() ); 
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		assertsOnPolicy(new Policies("testapi","id1", "", "USED", "", null), response.getPolicies().get(0));
		
		dhApiClient.addPolicy(new Policies(" testapi "," id1  ", "duplicatedid", "USED", "", null));
		
		response = dhApiClient.printPolicy("   testapi   ", "   id1 ");	
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
	
		dhApiClient.addPolicy(new Policies("testapi","id2", "", "USED", "", null));	

		response = dhApiClient.deletePolicy("testapi", "id2",""); 
		assertEquals(String.valueOf(true), response.getSuccess()); 
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		assertEquals("", response.getFailMsg()); 
		assertsOnPolicy(new Policies("testapi","id2", "", null, null, null), response.getPolicies().get(0));	
		
		response = dhApiClient.addPolicy(new Policies(" testapi  "," id3  ", " setepochtime   ", "UNUSED", "otherstuff ", 1643673346936L));			
		assertEquals(String.valueOf(true), response.getSuccess()); 
		assertEquals(1, response.getPolicies().size()); 
		assertsOnPolicy(new Policies("testapi","id3", "setepochtime", "UNUSED", "otherstuff ", 1643673346936L), response.getPolicies().get(0));
		
		response = dhApiClient.printPolicy("testapi","id3", "setepochtime");			
		assertEquals(String.valueOf(true), response.getSuccess()); 
		assertEquals(1, response.getPolicies().size()); 
		assertsOnPolicy(new Policies("testapi","id3", "setepochtime", "UNUSED", "otherstuff ", 1643673346936L), response.getPolicies().get(0));
		
		Policies updatePolicy = new Policies("testapi","id3", "setepochtime", "REUSABLE", "updateother", 123L); 
		response = dhApiClient.updatePolicy(updatePolicy);
		assertEquals(response.getFailMsg(), String.valueOf(true), response.getSuccess()); 
		assertEquals(1, response.getPolicies().size()); 
		assertsOnPolicy(updatePolicy, response.getPolicies().get(0));
		response = dhApiClient.printPolicy("testapi","id3", "setepochtime");			
		assertEquals(String.valueOf(true), response.getSuccess()); 
		assertEquals(1, response.getPolicies().size()); 
		assertsOnPolicy(updatePolicy, response.getPolicies().get(0));

		Policies updatePolicyCurrentTime = new Policies("testapi","id3", "setepochtime", "USED", "", null); //will use currenttime 
		response = dhApiClient.updatePolicy(updatePolicyCurrentTime);
		assertEquals(String.valueOf(true), response.getSuccess()); 
		assertEquals(1, response.getPolicies().size()); 
		// lets get the updated policy..
		response = dhApiClient.printPolicy("testapi","id3", "setepochtime");			
		assertEquals(String.valueOf(true), response.getSuccess()); 
		assertEquals(1, response.getPolicies().size()); 
		//we expect the epochtime should be a real epochtime
		Long epoch = response.getPolicies().get(0).getEpochtime();
		Long current = System.currentTimeMillis();
		assertTrue("epoch should be before = currenttime", epoch <= current);  
		assertTrue("epoch should but not a lot before", (epoch + 60000) > System.currentTimeMillis());  
		
		Policies updatePolicyToNumericEpochREUSABLE  = new Policies("testapi","id3", "setepochtime", "REUSABLE", "", 6666L);
		response = dhApiClient.updatePolicy(updatePolicyToNumericEpochREUSABLE);
		assertEquals(String.valueOf(true), response.getSuccess());
		assertEquals(1, response.getPolicies().size());
		assertsOnPolicy(updatePolicyToNumericEpochREUSABLE, response.getPolicies().get(0));
		
		response = dhApiClient.updatePolicy(new Policies("testapi","completejunk", "idontexist", "REUSABLE", "updateother", 123L));
		assertEquals(String.valueOf(false), response.getSuccess()); 
		assertEquals(Integer.valueOf(0), response.getRowsAffected()); 
		assertEquals("Policy does not exist.", response.getFailMsg()); 
		
		
		response = dhApiClient.printPolicy("testapi","id3");			
		assertEquals(String.valueOf(false), response.getSuccess()); 
		assertEquals(0, response.getPolicies().size()); 
		assertEquals("No rows matching the selection.", response.getFailMsg()); 
		
		response = dhApiClient.addPolicy(new Policies("testapi","id3", "setepochtime", "USED", "ALREADYEXISTS!!", 1643673346936L));			
		assertEquals(String.valueOf(false), response.getSuccess()); 
		assertEquals(1, response.getPolicies().size()); 
		assertsOnPolicy(new Policies("testapi","id3", "setepochtime", "USED", "ALREADYEXISTS!!", 1643673346936L), response.getPolicies().get(0));
		assertTrue("error should contain application (testapi)" , response.getFailMsg().contains("testapi") ); 
		assertTrue("error should contain idenifier (id3)" , response.getFailMsg().contains("id3") ); 
		assertTrue("error should contain lifecycle (setepochtime)" , response.getFailMsg().contains("setepochtime") ); 
		
		response = dhApiClient.deletePolicy("testapi", "id3", "setepochtime");	
		assertEquals(String.valueOf(true), response.getSuccess() ); 
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		System.out.println("	<< basicPolicyAddPrintDeleteChecks");
	}
	
	
	/**
	 * @param dhApiClient DataHunterRestApiClient
	 */
	public void workingWithMultiplePolicies(DataHunterRestApiClient dhApiClient){
		System.out.println("	>> workingWithMultiplePolicies");
		
		create6testPolices(dhApiClient);	
		DataHunterRestApiResponsePojo response = dhApiClient.deleteMultiplePolicies("nonexistingapp", null, null);
		assertEquals(String.valueOf(true), response.getSuccess() ); 
		assertEquals(Integer.valueOf(0), response.getRowsAffected());
		response = dhApiClient.deleteMultiplePolicies("testapi", null, "USED");
		assertEquals("response = " + response, Integer.valueOf(3), response.getRowsAffected());
		response = dhApiClient.deleteMultiplePolicies("testapi", null, "USED");
		assertEquals("response = " + response, Integer.valueOf(0), response.getRowsAffected());
		response = dhApiClient.deleteMultiplePolicies("testapi", null, "");
		assertEquals("response = " + response, Integer.valueOf(2), response.getRowsAffected());
		response = dhApiClient.deleteMultiplePolicies("otherapp", "", "");
		assertEquals("response = " + response, Integer.valueOf(1), response.getRowsAffected());

		
		create6testPolices(dhApiClient);	
		response = dhApiClient.printSelectedPolicies(" testapi  ", null, null);
		assertEquals(Integer.valueOf(5), response.getRowsAffected());
		assertEquals(5, response.getPolicies().size());
		assertTrue(response.getPolicies().get(0).toString().startsWith("[application=testapi, identifier=im1, lifecycle=, useability=USED, otherdata=,"));
		assertTrue(response.getPolicies().get(1).toString().startsWith("[application=testapi, identifier=im2, lifecycle=, useability=USED, otherdata=pi,"));
		assertTrue(response.getPolicies().get(2).toString().startsWith("[application=testapi, identifier=im3, lifecycle=duplicatedid, useability=REUSABLE, otherdata=duplicated id,"));
		assertTrue(response.getPolicies().get(3).toString().startsWith("[application=testapi, identifier=im3, lifecycle=nonblanklc, useability=USED, otherdata=otherdata3,"));
		assertTrue(response.getPolicies().get(4).toString().startsWith("[application=testapi, identifier=im4, lifecycle=nonblanklc, useability=UNUSED, otherdata=,"));
		
		response = dhApiClient.printSelectedPolicies("testapi", null, "USED");
		assertEquals(3, response.getPolicies().size());		
		assertTrue(response.getPolicies().get(0).toString().startsWith("[application=testapi, identifier=im1, lifecycle=, useability=USED, otherdata=,"));		
		assertTrue(response.getPolicies().get(1).toString().startsWith("[application=testapi, identifier=im2, lifecycle=, useability=USED, otherdata=pi,"));
		assertTrue(response.getPolicies().get(2).toString().startsWith("[application=testapi, identifier=im3, lifecycle=nonblanklc, useability=USED, otherdata=otherdata3,"));
	
		response = dhApiClient.printSelectedPolicies("testapi", " nonblanklc", "");
		assertEquals(2, response.getPolicies().size());		
		assertTrue(response.getPolicies().get(0).toString().startsWith("[application=testapi, identifier=im3, lifecycle=nonblanklc, useability=USED, otherdata=otherdata3,"));	
		assertTrue(response.getPolicies().get(1).toString().startsWith("[application=testapi, identifier=im4, lifecycle=nonblanklc, useability=UNUSED, otherdata=,"));

		response = dhApiClient.printSelectedPolicies(" testapi ", " nonblanklc ", "USED");
		assertEquals(1, response.getPolicies().size());		
		assertTrue(response.getPolicies().get(0).toString().startsWith("[application=testapi, identifier=im3, lifecycle=nonblanklc, useability=USED, otherdata=otherdata3,"));

		response = dhApiClient.printSelectedPolicies("doesntexist", "nonblanklc", "USED");	
		assertEquals(0, response.getPolicies().size());		
		
		PolicySelectionFilter psc = new PolicySelectionFilter();
		psc.setApplication("testapi");
		psc.setLifecycle(null);
		psc.setUseability("");
		psc.setOtherdataSelected(true);
		psc.setOtherdata("%e%");
		psc.setCreatedSelected(true);
		psc.setCreatedFrom("2023-01-01 15:59:59.469937");
		psc.setCreatedTo("2099-12-31 23:59:59.999999");
		// 'updated fields should be ignored
		psc.setEpochtimeSelected(true);
		psc.setEpochtimeFrom("66");
		psc.setEpochtimeTo("4102444799999");
		psc.setSelectOrder("OTHERDATA");
		psc.setOrderDirection("DESCENDING");
		psc.setLimit("25");
		response = dhApiClient.printSelectedPolicies(psc);
		assertEquals("response="+response, Integer.valueOf(2), response.getRowsAffected());
		assertEquals("response="+response, 2, response.getPolicies().size());
		assertTrue(response.getPolicies().get(0).toString().startsWith("[application=testapi, identifier=im3, lifecycle=nonblanklc, useability=USED, otherdata=otherdata3,"));
		assertTrue(response.getPolicies().get(1).toString().startsWith("[application=testapi, identifier=im3, lifecycle=duplicatedid, useability=REUSABLE, otherdata=duplicated id,"));
		
		psc.setApplication("testapi");		
		psc.setLifecycle("duplicatedid");
		psc.setUseability("REUSABLE");
		psc.setOtherdataSelected(true);
		psc.setOtherdata("%duplicated%");
		psc.setCreatedSelected(true);
		psc.setCreatedFrom("2023-01-01 15:59:59.469937");
		psc.setCreatedTo("2099-12-31 23:59:59.999999");
		psc.setUpdatedSelected(true);
		psc.setUpdatedFrom("2023-01-01 15:59:59.469937");
		psc.setUpdatedTo("2099-12-31 23:59:59.999999");		
		psc.setEpochtimeSelected(true);
		psc.setEpochtimeFrom("66");
		psc.setEpochtimeTo("4102444799999");
		psc.setSelectOrder("OTHERDATA");
		psc.setOrderDirection("DESCENDING");
		psc.setLimit("250");
		response = dhApiClient.printSelectedPolicies(psc);
		assertEquals("response="+response, Integer.valueOf(1), response.getRowsAffected());
		assertEquals("response="+response, 1, response.getPolicies().size());
		assertTrue(response.getPolicies().get(0).toString().startsWith("[application=testapi, identifier=im3, lifecycle=duplicatedid, useability=REUSABLE, otherdata=duplicated id,"));

		psc.setApplication("testapi");		
		psc.setLifecycle("i should not exist!");
		response = dhApiClient.printSelectedPolicies(psc);
		assertEquals(Integer.valueOf(0), response.getRowsAffected());
		assertEquals(0, response.getPolicies().size());		

		// id  selectors
		
		psc = new PolicySelectionFilter();
		psc.setApplication("testapi");
		psc.setLifecycle(null);
		psc.setUseability("");
		psc.setIdentifierListSelected(true);
		psc.setIdentifierList("im2,im3");
		psc.setOtherdataSelected(false);
		psc.setOtherdataSelected(false);
		psc.setOtherdata("nothing!");
		psc.setCreatedSelected(false);
		psc.setEpochtimeSelected(false);
		psc.setSelectOrder("OTHERDATA");
		psc.setOrderDirection("DESCENDING");
		psc.setLimit("25");
		response = dhApiClient.printSelectedPolicies(psc);
		assertEquals("response="+response, Integer.valueOf(3), response.getRowsAffected());
		assertEquals("response="+response, 3, response.getPolicies().size());
		assertTrue(response.getPolicies().get(0).toString().startsWith("[application=testapi, identifier=im2, lifecycle=, useability=USED, otherdata=pi,"));
		assertTrue(response.getPolicies().get(1).toString().startsWith("[application=testapi, identifier=im3, lifecycle=nonblanklc, useability=USED, otherdata=otherdata3,"));
		assertTrue(response.getPolicies().get(2).toString().startsWith("[application=testapi, identifier=im3, lifecycle=duplicatedid, useability=REUSABLE, otherdata=duplicated id,"));
		
		psc.setIdentifierLikeSelected(true);
		psc.setIdentifierLike("%im3%");		
		psc.setIdentifierListSelected(false);
		//psc.setIdentifierList("nothing");
		response = dhApiClient.printSelectedPolicies(psc);
		assertEquals("response="+response, Integer.valueOf(2), response.getRowsAffected());
		assertEquals("response="+response, 2, response.getPolicies().size());
		assertTrue(response.getPolicies().get(0).toString().startsWith("[application=testapi, identifier=im3, lifecycle=nonblanklc, useability=USED, otherdata=otherdata3,"));
		assertTrue(response.getPolicies().get(1).toString().startsWith("[application=testapi, identifier=im3, lifecycle=duplicatedid, useability=REUSABLE, otherdata=duplicated id,"));
		
		psc.setIdentifierLikeSelected(true);
		psc.setIdentifierLike("%im%");		
		psc.setIdentifierListSelected(true);
		psc.setIdentifierList("im2");
		response = dhApiClient.printSelectedPolicies(psc);
		assertEquals("response="+response, Integer.valueOf(1), response.getRowsAffected());
		assertEquals("response="+response, 1, response.getPolicies().size());
		assertTrue(response.getPolicies().get(0).toString().startsWith("[application=testapi, identifier=im2, lifecycle=, useability=USED, otherdata=pi,"));	
		
		
		// deletes
		response = dhApiClient.deleteMultiplePolicies(" testapi ", "nonblanklc ", null);
		assertEquals(Integer.valueOf(2), response.getRowsAffected());
		
		response = dhApiClient.printSelectedPolicies("testapi", "", "");
		assertEquals(Integer.valueOf(3), response.getRowsAffected());
		
		//actual required values to delete im2
		PolicySelectionFilter psfd = new PolicySelectionFilter();
		psfd.setApplication("testapi");
		psfd.setLifecycle(null);
		psfd.setUseability("");
		psfd.setOtherdataSelected(true);
		psfd.setCreatedSelected(false);
		// created and updated fields ignored
		psfd.setEpochtimeSelected(true);
		psfd.setEpochtimeFrom("314158");
		
		// lets check the optional filters work
		psfd.setOtherdata("%pi%");		
		psfd.setEpochtimeTo("314158");
		response = dhApiClient.deleteMultiplePolicies(psfd);
		assertEquals(Integer.valueOf(0), response.getRowsAffected());
		psfd.setOtherdata("%nothing%");		
		psfd.setEpochtimeTo("314160");
		response = dhApiClient.deleteMultiplePolicies(psfd);
		assertEquals(Integer.valueOf(0), response.getRowsAffected());		
		// now delete im2
		psfd.setOtherdata("%pi%");		
		psfd.setEpochtimeTo("314160");
		response = dhApiClient.deleteMultiplePolicies(psfd);
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		
		response = dhApiClient.deleteMultiplePolicies("testapi", "", "USED");
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		
		response = dhApiClient.printSelectedPolicies("testapi", null, null);
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		assertTrue(response.getPolicies().get(0).toString().startsWith("[application=testapi, identifier=im3, lifecycle=duplicatedid, useability=REUSABLE, otherdata=duplicated id,"));
		
		//only im3 left
		psfd = new PolicySelectionFilter();
		psfd.setApplication("testapi");
		psfd.setLifecycle(null);
		psfd.setUseability("");
		psfd.setIdentifierLikeSelected(true);
		psfd.setIdentifierLike("%im3%");
		psfd.setIdentifierListSelected(true);
		psfd.setIdentifierList("im3");
		response = dhApiClient.deleteMultiplePolicies("testapi", null, null);
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		response = dhApiClient.printSelectedPolicies("testapi", null, null);
		assertEquals(Integer.valueOf(0), Integer.valueOf(response.getPolicies().size()));

		System.out.println("	<< workingWithMultiplePolicies");
	}

	
	/**
	 * @param dhApiClient DataHunterRestApiClient
	 */
	public void policyCountsAndBreakdowns(DataHunterRestApiClient dhApiClient){
		System.out.println("	>> policyCountsAndBreakdowns");
		create6testPolices(dhApiClient);
		
		DataHunterRestApiResponsePojo response = dhApiClient.countPolicies("testapi", "nonblanklc", "USED");
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		assertEquals("[[application=testapi, identifier=null, lifecycle=nonblanklc, useability=USED, otherdata=null, created=null, updated=null, epochtime=null]]", response.getPolicies().toString()); 
		
		response = dhApiClient.countPolicies("testapi", "nonblanklc", "JUNKUSE");
		assertEquals(String.valueOf(false), response.getSuccess());
		assertEquals("useability must be one of [REUSABLE, UNPAIRED, UNUSED, USED], but was 'JUNKUSE'.", response.getFailMsg());
		
		response = dhApiClient.countPolicies("testapi", "nonblanklc", ""); // all useabilty
		assertEquals(String.valueOf(true), response.getSuccess());
		assertEquals(Integer.valueOf(2), response.getRowsAffected());
		assertEquals("", response.getFailMsg());

		response = dhApiClient.policiesBreakdown(DataHunterConstants.EQUALS, " testapi ", " nonblanklc ", "USED");
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		assertEquals("[[application= testapi , identifier=null, lifecycle= nonblanklc , useability=USED, otherdata=EQUALS, created=null, updated=null, epochtime=null]]", 
				response.getPolicies().toString());
		assertEquals(1, response.getCountPoliciesBreakdown().size());	
		assertEquals("[[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=USED, selectOrder=null], rowCount=1, isReusableIndexed=N, holeCount=0]]",
				response.getCountPoliciesBreakdown().toString());
	
		assertEquals(Integer.valueOf(5), dhApiClient.countPolicies("testapi", null, null).getRowsAffected());

		response = dhApiClient.policiesBreakdown(DataHunterConstants.EQUALS, "testapi", null, null);
		assertEquals(4, response.getCountPoliciesBreakdown().size());		
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=, useability=USED, selectOrder=null], rowCount=2, isReusableIndexed=N, holeCount=0]",
				response.getCountPoliciesBreakdown().get(0).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=duplicatedid, useability=REUSABLE, selectOrder=null], rowCount=1, isReusableIndexed=N, holeCount=0]", 
				response.getCountPoliciesBreakdown().get(1).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=UNUSED, selectOrder=null], rowCount=1, isReusableIndexed=N, holeCount=0]", 	
				response.getCountPoliciesBreakdown().get(2).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=USED, selectOrder=null], rowCount=1, isReusableIndexed=N, holeCount=0]", 		
				response.getCountPoliciesBreakdown().get(3).toString());
				
		assertEquals(Integer.valueOf(5), dhApiClient.countPolicies("testapi", "", "").getRowsAffected() );

		response = dhApiClient.policiesBreakdown(DataHunterConstants.EQUALS, "testapi", "", "");
		assertEquals(4, response.getCountPoliciesBreakdown().size());		
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=, useability=USED, selectOrder=null], rowCount=2, isReusableIndexed=N, holeCount=0]",
				response.getCountPoliciesBreakdown().get(0).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=duplicatedid, useability=REUSABLE, selectOrder=null], rowCount=1, isReusableIndexed=N, holeCount=0]", 
				response.getCountPoliciesBreakdown().get(1).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=UNUSED, selectOrder=null], rowCount=1, isReusableIndexed=N, holeCount=0]", 	
				response.getCountPoliciesBreakdown().get(2).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=USED, selectOrder=null], rowCount=1, isReusableIndexed=N, holeCount=0]", 		
				response.getCountPoliciesBreakdown().get(3).toString());
		
		assertEquals(Integer.valueOf(0), dhApiClient.countPolicies("nonexisting", "", "").getRowsAffected() );

		response = dhApiClient.policiesBreakdown(DataHunterConstants.EQUALS, "nonexisting", "", "");
		assertEquals(0, response.getCountPoliciesBreakdown().size());		
		assertEquals("sql execution OK, but no rows matched the selection criteria.",  response.getFailMsg());		
		assertEquals(String.valueOf(true), response.getSuccess());		
		
		assertEquals(Integer.valueOf(3), dhApiClient.countPolicies("testapi", "",  "USED").getRowsAffected() );
		
		response = dhApiClient.policiesBreakdown(DataHunterConstants.EQUALS, "testapi", "", "USED");
		assertEquals(2, response.getCountPoliciesBreakdown().size());		
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=, useability=USED, selectOrder=null], rowCount=2, isReusableIndexed=N, holeCount=0]", 				
				response.getCountPoliciesBreakdown().get(0).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=USED, selectOrder=null], rowCount=1, isReusableIndexed=N, holeCount=0]", 		
				response.getCountPoliciesBreakdown().get(1).toString());

		assertEquals(Integer.valueOf(0), dhApiClient.countPolicies("testapi", "nonexistingc",  "").getRowsAffected() );
		assertEquals(0, dhApiClient.policiesBreakdown(DataHunterConstants.EQUALS, "testapi", "nonexistingc",  "").getCountPoliciesBreakdown().size()); 

		assertEquals(Integer.valueOf(1), dhApiClient.countPolicies("testapi", null,  "UNUSED").getRowsAffected() );

		response = dhApiClient.policiesBreakdown(DataHunterConstants.EQUALS, "testapi", null,  "UNUSED");
		assertEquals(1, response.getCountPoliciesBreakdown().size());		
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=UNUSED, selectOrder=null], rowCount=1, isReusableIndexed=N, holeCount=0]", 	
				response.getCountPoliciesBreakdown().get(0).toString());
		
		assertEquals(Integer.valueOf(2), dhApiClient.countPolicies("testapi", "nonblanklc", "").getRowsAffected() );

		response = dhApiClient.policiesBreakdown(DataHunterConstants.EQUALS, "testapi", "nonblanklc", "");
		assertEquals(2, response.getCountPoliciesBreakdown().size());		
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=UNUSED, selectOrder=null], rowCount=1, isReusableIndexed=N, holeCount=0]", 	
				response.getCountPoliciesBreakdown().get(0).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=USED, selectOrder=null], rowCount=1, isReusableIndexed=N, holeCount=0]", 		
				response.getCountPoliciesBreakdown().get(1).toString());
		System.out.println("	<< policyCountsAndBreakdowns");
	}	
	
	
	/**
	 * Note: this method clears the DataHunter database of all existing data
	 * @param dhApiClient DataHunterRestApiClient
	 */
	public void policyCountBreakdownsUsingStartWith(DataHunterRestApiClient dhApiClient){
		System.out.println("	>> policyCountBreakdownsUsingStartWith  (DATABASE WILL BE CLEARED)");
		clearDatabase(dhApiClient); 
		create6testPolices(dhApiClient);
		assertEquals(5, dhApiClient.policiesBreakdown(DataHunterConstants.STARTS_WITH, "", null, null).getCountPoliciesBreakdown().size()); 
		
		dhApiClient.addPolicy(new Policies("test api","ex1", "", "UNUSED", "", null)); 			
		dhApiClient.addPolicy(new Policies("testaB_pi","ex2", "nonblanklc", "USED", "", null));
		dhApiClient.addPolicy(new Policies("testaC%pi:&? @=+","ex3", "lc with$char-s", "USED", "", null)); 
		dhApiClient.addPolicy(new Policies("testaC%pi:&? @=+","ex4", "lc with$char-s", "USED", "", null));
		
		DataHunterRestApiResponsePojo response = dhApiClient.policiesBreakdown(DataHunterConstants.STARTS_WITH, "", null, null);		
		assertEquals(8, response.getCountPoliciesBreakdown().size()); 
		assertEquals("[application=otherapp, startsWith=null, identifier=null, lifecycle=, useability=UNUSED, selectOrder=null], rowCount=1]", 					 response.getCountPoliciesBreakdown().get(0).toString());
		assertEquals("[application=test api, startsWith=null, identifier=null, lifecycle=, useability=UNUSED, selectOrder=null], rowCount=1]", 					 response.getCountPoliciesBreakdown().get(1).toString());
		assertEquals("[application=testaB_pi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=USED, selectOrder=null], rowCount=1]", 		 response.getCountPoliciesBreakdown().get(2).toString());
		assertEquals("[application=testaC%pi:&? @=+, startsWith=null, identifier=null, lifecycle=lc with$char-s, useability=USED, selectOrder=null], rowCount=2]",response.getCountPoliciesBreakdown().get(3).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=, useability=USED, selectOrder=null], rowCount=2]", 					 response.getCountPoliciesBreakdown().get(4).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=duplicatedid, useability=REUSABLE, selectOrder=null], rowCount=1]", 	 response.getCountPoliciesBreakdown().get(5).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=UNUSED, selectOrder=null], rowCount=1]", 		 response.getCountPoliciesBreakdown().get(6).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=USED, selectOrder=null], rowCount=1]", 			 response.getCountPoliciesBreakdown().get(7).toString());

		assertEquals(7, dhApiClient.policiesBreakdown(DataHunterConstants.STARTS_WITH, "test", null, null).getCountPoliciesBreakdown().size()); 
		assertEquals(4, dhApiClient.policiesBreakdown(DataHunterConstants.STARTS_WITH, " testapi", null, null).getCountPoliciesBreakdown().size()); 
		assertEquals(1, dhApiClient.policiesBreakdown(DataHunterConstants.STARTS_WITH, " test ", null, null).getCountPoliciesBreakdown().size()); 

		response = dhApiClient.policiesBreakdown(DataHunterConstants.STARTS_WITH, "test", "lc with$char-s", null);		
		assertEquals(1, response.getCountPoliciesBreakdown().size()); 
		assertEquals("[application=testaC%pi:&? @=+, startsWith=null, identifier=null, lifecycle=lc with$char-s, useability=USED, selectOrder=null], rowCount=2]",response.getCountPoliciesBreakdown().get(0).toString());

		response = dhApiClient.policiesBreakdown(DataHunterConstants.STARTS_WITH, "", null, "USED");		
		assertEquals(4, response.getCountPoliciesBreakdown().size()); 
		assertEquals("[application=testaB_pi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=USED, selectOrder=null], rowCount=1]", 		 response.getCountPoliciesBreakdown().get(0).toString());
		assertEquals("[application=testaC%pi:&? @=+, startsWith=null, identifier=null, lifecycle=lc with$char-s, useability=USED, selectOrder=null], rowCount=2]",response.getCountPoliciesBreakdown().get(1).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=, useability=USED, selectOrder=null], rowCount=2]", 					 response.getCountPoliciesBreakdown().get(2).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=USED, selectOrder=null], rowCount=1]", 			 response.getCountPoliciesBreakdown().get(3).toString());
		
		response = dhApiClient.policiesBreakdown(DataHunterConstants.STARTS_WITH, "testa", "nonblanklc", "USED");		
		assertEquals(2, response.getCountPoliciesBreakdown().size()); 
		assertEquals("[application=testaB_pi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=USED, selectOrder=null], rowCount=1]", 		 response.getCountPoliciesBreakdown().get(0).toString());
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=nonblanklc, useability=USED, selectOrder=null], rowCount=1]", 			 response.getCountPoliciesBreakdown().get(1).toString());
		
		//clean up
		assertEquals(Integer.valueOf(1), dhApiClient.deleteMultiplePolicies("otherapp", null, null).getRowsAffected());
		assertEquals(Integer.valueOf(1), dhApiClient.deleteMultiplePolicies("test api", null, null).getRowsAffected());
		assertEquals(Integer.valueOf(1), dhApiClient.deleteMultiplePolicies("testaB_pi", null, null).getRowsAffected());
		assertEquals(Integer.valueOf(2), dhApiClient.deleteMultiplePolicies("testaC%pi:&? @=+", null, null).getRowsAffected());
		assertEquals(Integer.valueOf(5), dhApiClient.deleteMultiplePolicies("testapi", null, null).getRowsAffected());
		assertEquals(0, dhApiClient.policiesBreakdown(DataHunterConstants.STARTS_WITH, "", null, null).getCountPoliciesBreakdown().size());
		System.out.println("	<< policyCountBreakdownsUsingStartWith");		
	}


	/**
	 * @param dhApiClient DataHunterRestApiClient
	 */
	public void workingWithUseStateChanges(DataHunterRestApiClient dhApiClient){
		System.out.println("	>> workingWithUseStateChanges");			
		create6testPolices(dhApiClient);
		
		DataHunterRestApiResponsePojo response = dhApiClient.updatePoliciesUseState("testapi", null, null, "USED", "UNPAIRED", null);
		assertEquals(Integer.valueOf(3), response.getRowsAffected());
		response = dhApiClient.updatePoliciesUseState("testapi", "", "", "UNPAIRED", "UNUSED", null);
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
		assertsOnPolicy(new Policies("testapi","im2", "", "UNUSED", "pi", 314159L), response.getPolicies().get(0));

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
		assertEquals(Integer.valueOf(1), response.getRowsAffected());
		assertsOnPolicy(new Policies("testapi","im3", "duplicatedid", "REUSABLE", "duplicated id", null), response.getPolicies().get(0));		
		assertEquals("", response.getFailMsg()); // should of been changed to USED:
		assertsOnPolicy(new Policies("testapi","im3", "duplicatedid", "USED", "duplicated id", null), dhApiClient.printPolicy("testapi","im3","duplicatedid" ).getPolicies().get(0));
			
		create6testPolices(dhApiClient);
		response = dhApiClient.updatePoliciesUseState("testapi", null, null, "", "UNUSED", null);
		assertEquals(Integer.valueOf(5), response.getRowsAffected());
		response = dhApiClient.lookupNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_OLDEST_ENTRY);
		assertsOnPolicy(new Policies("testapi","im1", "", "UNUSED", "", null), response.getPolicies().get(0));		
		response = dhApiClient.useNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_OLDEST_ENTRY);
		assertsOnPolicy(new Policies("testapi","im1", "", "UNUSED", "", null), response.getPolicies().get(0));		
		response = dhApiClient.useNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_OLDEST_ENTRY);
		assertsOnPolicy(new Policies("testapi","im2", "", "UNUSED", "pi", 314159L), response.getPolicies().get(0));	
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
		
		response = dhApiClient.updatePoliciesUseState("testapi", "im3", "USED", "UNUSED", "1234");
		assertEquals(Integer.valueOf(2), response.getRowsAffected());
		response = dhApiClient.useNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_OLDEST_ENTRY);		
		assertsOnPolicy(new Policies("testapi","im3", "nonblanklc", "UNUSED", "otherdata3", 1234L), response.getPolicies().get(0));		
		response = dhApiClient.useNextPolicy("testapi", null, "UNUSED", DataHunterConstants.SELECT_OLDEST_ENTRY);
		assertsOnPolicy(new Policies("testapi","im3", "duplicatedid", "UNUSED", "duplicated id", 1234L), response.getPolicies().get(0));			
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
		System.out.println("	<< workingWithUseStateChanges");			
	}	
	
	/**
	 * @param dhApiClient DataHunterRestApiClient
	 */
	public void workingWithAsyncMessages(DataHunterRestApiClient dhApiClient) {
		System.out.println("	>> workingWithAsyncMessages (applications starting with testapi- will be CLEARED)");			
		clearDatabase(dhApiClient, "testapi-");
		insertPolicySets(dhApiClient, "testapi-async", "t01-", 5); 
		DataHunterRestApiResponsePojo response = dhApiClient.printSelectedPolicies("testapi-async", null, "UNPAIRED");
		assertEquals(Integer.valueOf(20), response.getRowsAffected());
		assertEquals(20, response.getPolicies().size());
				
		dhApiClient.deleteMultiplePolicies("norowsfound", null, null);
		//response = dhApiClient.asyncMessageAnalyzer(DataHunterConstants.STARTS_WITH,"TESTAPI_ASYNC_HIGH_VOL", null, "UNPAIRED", "USED");
		response = dhApiClient.asyncMessageAnalyzer(DataHunterConstants.EQUALS,"norowsfound", null, null, null);
		assertEquals(("response = " + response), 0, response.getAsyncMessageaAnalyzerResults().size());		
		assertEquals(Integer.valueOf(0), response.getRowsAffected());

		response = dhApiClient.asyncMessageAnalyzer(DataHunterConstants.EQUALS,"testapi-async", null, null, null);		
		assertEquals(5, response.getAsyncMessageaAnalyzerResults().size());	
		assertEquals(Integer.valueOf(5), response.getRowsAffected());
	
		insertPolicySets(dhApiClient, "testapi-like", "t02-", 1);
		assertEquals(Integer.valueOf(8), dhApiClient.policiesBreakdown(DataHunterConstants.STARTS_WITH, "testapi-", "", "UNPAIRED").getRowsAffected());
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
		System.out.println("	<< workingWithAsyncMessages");				
	}

	
	/**
	 * clears all "testapi..."
	 * @param dhApiClient DataHunterRestApiClient
	 */
	public void workingWithIndexedRenewableDataUsage(DataHunterRestApiClient dhApiClient){
		System.out.println("	>> workingWithIndexedRenewableData");
		clearDatabase(dhApiClient, "testapi");
		create6testPolices(dhApiClient);  // 'background'
		create5IndexedRenewablePolices(dhApiClient);
		
		DataHunterRestApiResponsePojo response = dhApiClient.policiesBreakdown(DataHunterConstants.EQUALS, "testapi", "ixrew", null); //all REUSABLE
		assertEquals(1, response.getCountPoliciesBreakdown().size());		
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=ixrew, useability=REUSABLE, selectOrder=null], rowCount=6, isReusableIndexed=Y, holeCount=0]",
				response.getCountPoliciesBreakdown().get(0).toString());
	
		response = dhApiClient.printSelectedPolicies("testapi", "ixrew", "REUSABLE");
		assertEquals(6, response.getPolicies().size());		
		assertTrue(response.getPolicies().get(0).toString().startsWith("[application=testapi, identifier=0000000000_IX, lifecycle=ixrew, useability=REUSABLE, otherdata=5,"));		
		assertTrue(response.getPolicies().get(1).toString().startsWith("[application=testapi, identifier=0000000001, lifecycle=ixrew, useability=REUSABLE, otherdata=mydata1,"));		
		assertTrue(response.getPolicies().get(2).toString().startsWith("[application=testapi, identifier=0000000002, lifecycle=ixrew, useability=REUSABLE, otherdata=mydata2,"));		
		assertTrue(response.getPolicies().get(3).toString().startsWith("[application=testapi, identifier=0000000003, lifecycle=ixrew, useability=REUSABLE, otherdata=mydata3,"));		
		assertTrue(response.getPolicies().get(4).toString().startsWith("[application=testapi, identifier=0000000004, lifecycle=ixrew, useability=REUSABLE, otherdata=mydata4,"));		
		assertTrue(response.getPolicies().get(5).toString().startsWith("[application=testapi, identifier=0000000005, lifecycle=ixrew, useability=REUSABLE, otherdata=mydata5,"));		
		
		for (int i = 0; i < 10; i++) {
			response = dhApiClient.lookupNextPolicy("testapi", "ixrew", "REUSABLE", DataHunterConstants.SELECT_RANDOM_ENTRY);
			Policies randPolicy =  response.getPolicies().get(0);
			assertTrue(randPolicy.getApplication().equals("testapi"));
			assertTrue(randPolicy.getIdentifier().startsWith("000000000") && randPolicy.getIdentifier().substring(9).matches("[1-5]") && randPolicy.getIdentifier().length()==10);
			assertTrue(randPolicy.getLifecycle().equals("ixrew"));
			assertTrue(randPolicy.getUseability().equals("REUSABLE"));
			assertTrue(randPolicy.getOtherdata().startsWith("mydata") && randPolicy.getOtherdata().substring(6).equals(randPolicy.getIdentifier().substring(9))
					&& randPolicy.getOtherdata().length()==7);			
		}
		response = dhApiClient.lookupNextPolicy("testapi", "ixrew", "REUSABLE", DataHunterConstants.SELECT_OLDEST_ENTRY);
		assertTrue(response.getPolicies().get(0).toString().startsWith("[application=testapi, identifier=0000000001, lifecycle=ixrew, useability=REUSABLE, otherdata=mydata1,"));		
		response = dhApiClient.lookupNextPolicy("testapi", "ixrew", "REUSABLE", DataHunterConstants.SELECT_MOST_RECENTLY_ADDED);
		assertTrue(response.getPolicies().get(0).toString().startsWith("[application=testapi, identifier=0000000005, lifecycle=ixrew, useability=REUSABLE, otherdata=mydata5,"));			
		System.out.println("	<< workingWithIndexedRenewableData");	
	}	

	
	/**
	 * clears all "testapi..."
	 * @param dhApiClient DataHunterRestApiClient
	 */
	public void workingWithIndexedRenewableDataReindexing(DataHunterRestApiClient dhApiClient){
		System.out.println("	>> workingWithIndexedRenewableDataReindexing");
		clearDatabase(dhApiClient, "testapi");
		create17IndexedRenewablePolices(dhApiClient);
		
		DataHunterRestApiResponsePojo response = dhApiClient.policiesBreakdown(DataHunterConstants.EQUALS, "testapi", "ixrew", null); //all REUSABLE
		assertEquals(1, response.getCountPoliciesBreakdown().size());		
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=ixrew, useability=REUSABLE, selectOrder=null], rowCount=18, isReusableIndexed=Y, holeCount=3]",
				response.getCountPoliciesBreakdown().get(0).toString());
		
		response = dhApiClient.reindexReusableIndexedPolicies("testapi", "ixrew");
		assertEquals(1, response.getPolicies().size());	
		assertEquals("testapi", ((Policies)response.getPolicies().get(0)).getApplication());	
		assertEquals("ixrew",   ((Policies)response.getPolicies().get(0)).getLifecycle());	
		assertTrue(Boolean.valueOf(response.getSuccess()));
		assertEquals("OK", response.getFailMsg());
		assertEquals(7, response.getRowsAffected().intValue());
		
		response = dhApiClient.policiesBreakdown(DataHunterConstants.EQUALS, "testapi", "ixrew", null); //all REUSABLE
		assertEquals("[application=testapi, startsWith=null, identifier=null, lifecycle=ixrew, useability=REUSABLE, selectOrder=null], rowCount=18, isReusableIndexed=Y, holeCount=0]",
				response.getCountPoliciesBreakdown().get(0).toString());
		
		response = dhApiClient.printSelectedPolicies("testapi", "ixrew", "REUSABLE");
		assertEquals(18, response.getPolicies().size());		
		assertTrue(response.getPolicies().get(0).toString().startsWith( "[application=testapi, identifier=0000000000_IX, lifecycle=ixrew, useability=REUSABLE, otherdata=17,"));		
		assertTrue(response.getPolicies().get(1).toString().startsWith( "[application=testapi, identifier=0000000001, lifecycle=ixrew, useability=REUSABLE, otherdata=1 MARY STREET CYGNET TAS 7111,"));		
		assertTrue(response.getPolicies().get(2).toString().startsWith( "[application=testapi, identifier=0000000002, lifecycle=ixrew, useability=REUSABLE, otherdata=2 MARY STREET CYGNET TAS 7111,"));
		assertTrue(response.getPolicies().get(3).toString().startsWith( "[application=testapi, identifier=0000000003, lifecycle=ixrew, useability=REUSABLE, otherdata=3 MARY STREET CYGNET TAS 7111,"));		
		assertTrue(response.getPolicies().get(4).toString().startsWith( "[application=testapi, identifier=0000000004, lifecycle=ixrew, useability=REUSABLE, otherdata=4 MARY STREET CYGNET TAS 7111,"));		
		assertTrue(response.getPolicies().get(5).toString().startsWith( "[application=testapi, identifier=0000000005, lifecycle=ixrew, useability=REUSABLE, otherdata=5 MARY STREET CYGNET TAS 7111,"));		
		assertTrue(response.getPolicies().get(6).toString().startsWith( "[application=testapi, identifier=0000000006, lifecycle=ixrew, useability=REUSABLE, otherdata=6 MARY STREET CYGNET TAS 7111,"));		
		assertTrue(response.getPolicies().get(7).toString().startsWith( "[application=testapi, identifier=0000000007, lifecycle=ixrew, useability=REUSABLE, otherdata=7 MARY STREET CYGNET TAS 7111,"));		
		assertTrue(response.getPolicies().get(8).toString().startsWith( "[application=testapi, identifier=0000000008, lifecycle=ixrew, useability=REUSABLE, otherdata=5a ??? ,"));		
		assertTrue(response.getPolicies().get(9).toString().startsWith( "[application=testapi, identifier=0000000009, lifecycle=ixrew, useability=REUSABLE, otherdata=9 MARY STREET CYGNET TAS 7111,"));		
		assertTrue(response.getPolicies().get(10).toString().startsWith("[application=testapi, identifier=0000000010, lifecycle=ixrew, useability=REUSABLE, otherdata=n10,"));		
		assertTrue(response.getPolicies().get(11).toString().startsWith("[application=testapi, identifier=0000000011, lifecycle=ixrew, useability=REUSABLE, otherdata=n11,"));		
		assertTrue(response.getPolicies().get(12).toString().startsWith("[application=testapi, identifier=0000000012, lifecycle=ixrew, useability=REUSABLE, otherdata=b itme,"));		
		assertTrue(response.getPolicies().get(13).toString().startsWith("[application=testapi, identifier=0000000013, lifecycle=ixrew, useability=REUSABLE, otherdata=1_,"));		
		assertTrue(response.getPolicies().get(14).toString().startsWith("[application=testapi, identifier=0000000014, lifecycle=ixrew, useability=REUSABLE, otherdata=short 4,"));		
		assertTrue(response.getPolicies().get(15).toString().startsWith("[application=testapi, identifier=0000000015, lifecycle=ixrew, useability=REUSABLE, otherdata=77 out of range,"));		
		assertTrue(response.getPolicies().get(16).toString().startsWith("[application=testapi, identifier=0000000016, lifecycle=ixrew, useability=REUSABLE, otherdata=junk 888,"));		
		assertTrue(response.getPolicies().get(17).toString().startsWith("[application=testapi, identifier=0000000017, lifecycle=ixrew, useability=REUSABLE, otherdata=a1,"));		
		System.out.println("	<< workingWithIndexedRenewableDataReindexing");	
	}	
	
	

	private void clearDatabase(DataHunterRestApiClient dhApiClient) {
		clearDatabase(dhApiClient, "");
	}	

	
	private void clearDatabase(DataHunterRestApiClient dhApiClient, String applicationStartsWith ) {
		DataHunterRestApiResponsePojo response = dhApiClient.policiesBreakdown(DataHunterConstants.STARTS_WITH, applicationStartsWith, null, null);		
		List<CountPoliciesBreakdown> countPoliciesBreakdownList = response.getCountPoliciesBreakdown();
		for (CountPoliciesBreakdown cpb : countPoliciesBreakdownList) {
			dhApiClient.deleteMultiplePolicies(cpb.getApplication(), cpb.getLifecycle(), cpb.getUseability()); 
		}
		assertEquals(0, dhApiClient.policiesBreakdown(DataHunterConstants.STARTS_WITH, applicationStartsWith, null, null).getCountPoliciesBreakdown().size());
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
		dhApiClient.addPolicy(new Policies("testapi","im2", "", "USED", "pi", 314159L));
		try {Thread.sleep(2);} catch (Exception e){}
		dhApiClient.addPolicy(new Policies("testapi","im3", "nonblanklc", "USED", "otherdata3", null));	
		try {Thread.sleep(2);} catch (Exception e){}
		dhApiClient.addPolicy(new Policies("testapi","im3", "duplicatedid", "REUSABLE", "duplicated id", null));
		try {Thread.sleep(2);} catch (Exception e){}
		dhApiClient.addPolicy(new Policies("testapi","im4", "nonblanklc", "UNUSED", "", null));	
		try {Thread.sleep(2);} catch (Exception e){}
		dhApiClient.addPolicy(new Policies("otherapp","io1", "", "UNUSED", null, null));
	}	
	
	
	private void create5IndexedRenewablePolices(DataHunterRestApiClient dhApiClient) {
		dhApiClient.addPolicy(new Policies("testapi","0000000000_IX", "ixrew", "REUSABLE", "0", null));
		dhApiClient.addPolicy(new Policies("testapi","0000000001", "ixrew", "REUSABLE", "mydata1", 314159L));	
		dhApiClient.addPolicy(new Policies("testapi","0000000002", "ixrew", "REUSABLE", "mydata2", 314159L));
		dhApiClient.addPolicy(new Policies("testapi","0000000003", "ixrew", "REUSABLE", "mydata3", 314159L));	
		dhApiClient.addPolicy(new Policies("testapi","0000000004", "ixrew", "REUSABLE", "mydata4", 444444L));
		dhApiClient.addPolicy(new Policies("testapi","0000000005", "ixrew", "REUSABLE", "mydata5", 555555L));	
	}		
	

	private void create17IndexedRenewablePolices(DataHunterRestApiClient dhApiClient) {
		dhApiClient.addPolicy(new Policies("testapi","0000000001", 	"ixrew", "REUSABLE", "1 MARY STREET CYGNET TAS 7111", 	1737420344260L));	
		dhApiClient.addPolicy(new Policies("testapi","0000000002", 	"ixrew", "REUSABLE", "2 MARY STREET CYGNET TAS 7111", 	1737420344260L));
		dhApiClient.addPolicy(new Policies("testapi","0000000003", 	"ixrew", "REUSABLE", "3 MARY STREET CYGNET TAS 7111", 	1737420344260L));	
		dhApiClient.addPolicy(new Policies("testapi","0000000004", 	"ixrew", "REUSABLE", "4 MARY STREET CYGNET TAS 7111", 	1737420344260L));
		dhApiClient.addPolicy(new Policies("testapi","0000000005", 	"ixrew", "REUSABLE", "5 MARY STREET CYGNET TAS 7111", 	1737420344260L));	
		dhApiClient.addPolicy(new Policies("testapi","0000000005a",	"ixrew", "REUSABLE", "5a ??? ", 						1739148988148L));
		dhApiClient.addPolicy(new Policies("testapi","0000000006", 	"ixrew", "REUSABLE", "6 MARY STREET CYGNET TAS 7111", 	1737420344260L));	
		dhApiClient.addPolicy(new Policies("testapi","0000000007", 	"ixrew", "REUSABLE", "7 MARY STREET CYGNET TAS 7111", 	1737420344260L));	
		dhApiClient.addPolicy(new Policies("testapi","0000000009", 	"ixrew", "REUSABLE", "9 MARY STREET CYGNET TAS 7111", 	1737420344260L));	
		dhApiClient.addPolicy(new Policies("testapi","000000000b",	"ixrew", "REUSABLE", "b itme",							1739227319646L));	
		dhApiClient.addPolicy(new Policies("testapi","0000000010",	"ixrew", "REUSABLE", "n10",								1739226864037L));	
		dhApiClient.addPolicy(new Policies("testapi","0000000011",	"ixrew", "REUSABLE", "n11",								1739226874309L));	
		dhApiClient.addPolicy(new Policies("testapi","000000001_",	"ixrew", "REUSABLE", "1_",								1739227113451L));	
		dhApiClient.addPolicy(new Policies("testapi","000000004",	"ixrew", "REUSABLE", "short 4",							1739238242901L));	
		dhApiClient.addPolicy(new Policies("testapi","0000000077",	"ixrew", "REUSABLE", "77 out of range",					1739226655535L));	
		dhApiClient.addPolicy(new Policies("testapi","00000000888",	"ixrew", "REUSABLE", "junk 888",						1739148834690L));	
		dhApiClient.addPolicy(new Policies("testapi","00000000a1",	"ixrew", "REUSABLE", "a1",								1739226956885L));	

		dhApiClient.addPolicy(new Policies("testapi","0000000000_IX","ixrew","REUSABLE", "13", 1738616298460L));
	}		
		
	
	private void assertsOnPolicy(Policies expectedPolicy, Policies actualPolicy) {
		assertEquals(expectedPolicy.getApplication(), actualPolicy.getApplication()); 
		assertEquals(expectedPolicy.getIdentifier(), actualPolicy.getIdentifier()); 
		assertEquals(expectedPolicy.getLifecycle(), actualPolicy.getLifecycle()); 
		assertEquals(expectedPolicy.getUseability(), actualPolicy.getUseability()); 
		assertEquals(expectedPolicy.getOtherdata(), actualPolicy.getOtherdata());
		if (expectedPolicy.getEpochtime() != null) {
			assertEquals(expectedPolicy.getEpochtime(), actualPolicy.getEpochtime()); 
		}
	}

	
	/**
	 * Runs each of the sample use cases against a local dataHunter instance
	 * tests build/clear application id  testapi- 
	 * @param args none required
	 */
	public static void main(String[] args) {
		System.out.println("running DataHunterRestApiClientSampleUsage ..");
		DataHunterRestApiClient dhApiClient = new DataHunterRestApiClient("http://localhost:8081/mark59-datahunter"  );
		DataHunterRestApiClientSampleUsage sample = new DataHunterRestApiClientSampleUsage();
		sample.basicPolicyAddPrintUpdateDeleteChecks(dhApiClient); 
		sample.workingWithMultiplePolicies(dhApiClient);
		sample.policyCountsAndBreakdowns(dhApiClient);
		sample.workingWithUseStateChanges(dhApiClient);
		sample.asyncLifeCycleTestWithUseabilityUpdate(dhApiClient);	
		sample.workingWithAsyncMessages(dhApiClient); //clears testapi-..
		sample.workingWithIndexedRenewableDataUsage(dhApiClient); //clears testapi..
		sample.workingWithIndexedRenewableDataReindexing(dhApiClient); //clears testapi..
		// sample.policyCountBreakdownsUsingStartWith(dhApiClient);	      // clears the database !!
		System.out.println("completed DataHunterRestApiClientSampleUsage ok");
	}

}
