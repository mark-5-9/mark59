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

package com.mark59.datahunter.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mark59.datahunter.application.DataHunterConstants;
import com.mark59.datahunter.application.DataHunterUtils;
import com.mark59.datahunter.application.SqlWithParms;
import com.mark59.datahunter.data.beans.Policies;
import com.mark59.datahunter.data.policies.dao.PoliciesDAO;
import com.mark59.datahunter.model.AsyncMessageaAnalyzerRequest;
import com.mark59.datahunter.model.AsyncMessageaAnalyzerResult;
import com.mark59.datahunter.model.CountPoliciesBreakdown;
import com.mark59.datahunter.model.DataHunterRestApiResponsePojo;
import com.mark59.datahunter.model.PolicySelectionCriteria;
import com.mark59.datahunter.model.UpdateUseStateAndEpochTime;


/**
 *  The DataHunter REST Service Controller.
 *  <p>Invokes DataHunter database functions, to satisfy requests from DataHunter REST Api Clients.
 *  <p>A Client program has been written to assist with calls to this REST service: <code>com.mark59.datahunter.restapi.DataHunterRestApiClient</code> in the 
 *  mark59-datahunter-api mark59 project. 
 *  <p>To use <code>DataHunterRestApiClient</code>, the target jar file for the mark59-datahunter-api project needs to be 
 *  on the class path.  Eg: for a script running in a JMeter instance DataHunterRestApiClient.jar should be placed in JMeter's lib/ext directory.        
 *  <p>A few general observations about the DataHunter REST Service:
 *  <ul>
 *  <li>The operations performed by the REST service align as closely as possible to the actions that are available using the DataHunter web 
 *  application. In fact apart from the odd extra validation required on the REST service, the behavior of the REST service should be the same as 
 *  the corresponding DataHunter web application pages. 
 *  <li>Where the REST operation does not require the <code>policies</code> list in the response to be populated with a result, it is populated with a single
 *  policy entry which aligns as much as possible with any selection criteria passed from the request (just informational to assist with debugging)
 *  <li>For detailed examples of the usage of this service, see class <code>DataHunterRestApiClientSampleUsage</code> in the mark59-datahunter-api project    
 *  </ul>  
 *   
 * @author Philip Webb
 * Written: Australian Summer 2021/22  
 */
@RestController
@RequestMapping("/api")
public class DataHunterRestController {
	
	@Autowired
	PoliciesDAO policiesDAO;	


	/**
	 * Add an Item to DataHunter  
	 * <br>The Item key is application|identifier|lifecycle, and must be unique
	 * 
	 * @param application application
	 * @param identifier identifier
	 * @param lifecycle lifecycle
	 * @param useability {@link DataHunterConstants#USEABILITY_LIST}, 
	 * @param otherdata (optional) otherdata  set to blank if not passed
	 * @param epochtime (Long) the current time is used if is not passed a numeric value
	 * @return ResponseEntity (ok)  
	 */
	@GetMapping(path = "/addPolicy")
	public ResponseEntity<Object> addPolicy(@RequestParam String application, @RequestParam String identifier, @RequestParam String lifecycle, 
			@RequestParam String useability, @RequestParam(required=false) String otherdata, @RequestParam(required=false) String epochtime){

		Policies policies = new Policies();
		policies.setApplication(application);
		policies.setIdentifier(identifier);
		policies.setLifecycle(lifecycle);
		policies.setUseability(useability);
		
		if (DataHunterUtils.isEmpty(otherdata)){
			policies.setOtherdata("");
		} else {
			policies.setOtherdata(otherdata);
		}
		
		if (!StringUtils.isNumeric(epochtime)){
			policies.setEpochtime(System.currentTimeMillis());
		} else {
			policies.setEpochtime(Long.parseLong(epochtime)  );
		}
		
		SqlWithParms sqlWithParms = policiesDAO.constructInsertDataSql(policies);
		int rowsAffected;

		DataHunterRestApiResponsePojo repsonse = new DataHunterRestApiResponsePojo();
		repsonse.setPolicies(Collections.singletonList(policies));

		try {
			rowsAffected = policiesDAO.runDatabaseUpdateSql(sqlWithParms);
		} catch (Exception e) {
			repsonse.setSuccess(String.valueOf(false));			
			repsonse.setFailMsg("sql exception caught: " + e.getMessage());
			repsonse.setRowsAffected(-1);
			return ResponseEntity.ok(repsonse);	
		}	
		repsonse.setRowsAffected(rowsAffected);
		// System.out.println("RestController getSqlparameters: " + DataHunterUtils.prettyPrintMap(sqlWithParms.getSqlparameters().getValues()));
		// System.out.println("RestController getSql : " + sqlWithParms.getSql());

		if (rowsAffected == 1 ){
			repsonse.setSuccess(String.valueOf(true));			
			repsonse.setFailMsg("");
		} else {
			repsonse.setSuccess(String.valueOf(false));	
			repsonse.setFailMsg("sql execution : Error.  1 row should of been affected, but sql result indicates " + rowsAffected + " rows affected?" );
		}
		return ResponseEntity.ok(repsonse);	
	}

	
	/**
	 * Count Items matching the selection criteria 
	 * 
	 * @param application  application
	 * @param lifecycle    (optional) blank to select all lifecycle values matching the other criteria
	 * @param useability   {@link DataHunterConstants#USEABILITY_LIST}, (optional) blank to select all useability values matching the other criteria
	 * @return ResponseEntity (ok) indicates the count of policies satisfying selection criteria
	 */
	@GetMapping(path = "/countPolicies")
	public ResponseEntity<Object> countPolicies(@RequestParam String application, @RequestParam(required=false) String lifecycle, 
			@RequestParam(required=false) String useability) {
	
		PolicySelectionCriteria policySelectionCriteria = new PolicySelectionCriteria();
		policySelectionCriteria.setApplication(application);
		policySelectionCriteria.setLifecycle(lifecycle);
		policySelectionCriteria.setUseability(useability);
		
		policySelectionCriteria.setSelectClause(" count(*)  as counter ");
		policySelectionCriteria.setSelectOrder(DataHunterConstants.SELECT_UNORDERED);
		SqlWithParms sqlWithParms = policiesDAO.constructSelectPoliciesSql(policySelectionCriteria);
		int rowsAffected = policiesDAO.runCountSql(sqlWithParms);
		
		DataHunterRestApiResponsePojo repsonse = new DataHunterRestApiResponsePojo();
		repsonse.setPolicies(Collections.singletonList(new Policies(application,null,lifecycle, useability, null, null)));
		repsonse.setRowsAffected(rowsAffected);
		repsonse.setSuccess(String.valueOf(true));			
		repsonse.setFailMsg("");
		return ResponseEntity.ok(repsonse);	
	}
	
	
	/**
	 * Breakdown counts by application, lifecycle and useability for Items matching the selection criteria. 
	 * <br>The breakdown will appear in the <b>countPoliciesBreakdown</b> element of the response 
	 *  
	 * @param applicationStartsWithOrEquals  must be "EQUALS" or "STARTS_WITH" (applied to application selection)
	 * @param application  application, or partial application id if using a applicationStartsWithOrEquals param of "STARTS_WITH"
	 * @param lifecycle    blank to select all lifecycle values matching the other criteria
	 * @param useability   {@link DataHunterConstants#USEABILITY_LIST}, blank to select all useability values matching the other criteria
	 * @return ResponseEntity (ok) containing breakdown counts by application, lifecycle and useability for Items matching the selection criteria.
	 * The breakdown will appear in the <b>countPoliciesBreakdown</b> element of the response, and the <b>policies</b> element of the response
	 * is used to indicate the selection request (note: just to provide visibility of all request params in the response, 
	 * the OtherData fields is populated with the applicationStartsWithOrEquals param)   
	 */
	@GetMapping(path = "/countPoliciesBreakdown")
	public ResponseEntity<Object> countPoliciesBreakdown(@RequestParam String applicationStartsWithOrEquals, @RequestParam String application, 
			@RequestParam(required=false) String lifecycle,	@RequestParam(required=false) String useability) {

		DataHunterRestApiResponsePojo repsonse = new DataHunterRestApiResponsePojo();
		// fudge: the 'otherData' field is populated with the applicationStartsWithOrEquals param
		repsonse.setPolicies(Collections.singletonList(new Policies(application,null,lifecycle, useability, applicationStartsWithOrEquals, null)));
	
		if ( ! (DataHunterConstants.EQUALS.equalsIgnoreCase(applicationStartsWithOrEquals)
				|| DataHunterConstants.STARTS_WITH.equalsIgnoreCase(applicationStartsWithOrEquals))) {
			repsonse.setRowsAffected(0);
			repsonse.setSuccess(String.valueOf(false));		
			repsonse.setFailMsg("applicationStartsWithOrEquals was '" + applicationStartsWithOrEquals + "' - must be either 'EQUALS' or 'STARTS_WITH'");
			return ResponseEntity.ok(repsonse);				
		}
		
		PolicySelectionCriteria policySelectionCriteria = new PolicySelectionCriteria();
		policySelectionCriteria.setApplicationStartsWithOrEquals(applicationStartsWithOrEquals);
		policySelectionCriteria.setApplication(application);
		policySelectionCriteria.setLifecycle(lifecycle);
		policySelectionCriteria.setUseability(useability);
		
		SqlWithParms sqlWithParms = policiesDAO.constructCountPoliciesBreakdownSql(policySelectionCriteria);
		List<CountPoliciesBreakdown> countPoliciesBreakdownList = policiesDAO.runCountPoliciesBreakdownSql(sqlWithParms);
		int rowsAffected = countPoliciesBreakdownList.size();

		repsonse.setCountPoliciesBreakdown(countPoliciesBreakdownList);
		repsonse.setRowsAffected(rowsAffected);
		repsonse.setSuccess(String.valueOf(true));			
		
		if (rowsAffected == 0 ){
			repsonse.setFailMsg("sql execution OK, but no rows matched the selection criteria.");
		} else {
			repsonse.setFailMsg("" );
		}
		return ResponseEntity.ok(repsonse);	
	}

	
	/**
	 * Retrieve the Item matching the application/id/lifecycle 
	 * 
	 * @param application  application
	 * @param identifier   identifier
	 * @param lifecycle    lifecycle (optional)  a value of blank is assumed if not passed as a parameter (in which case the Item 
	 * returned must have a blank lifecycle (NOT all/any items with this application,identifier)    
	 * @return ResponseEntity (ok)  if exists returns the item with matching application,identifier
	 */
	@GetMapping(path = "/printPolicy")
	public ResponseEntity<Object> printPolicy(@RequestParam String application, @RequestParam String identifier, @RequestParam(required=false) String lifecycle){
	
		PolicySelectionCriteria policySelectionCriteria = new PolicySelectionCriteria();
		policySelectionCriteria.setApplication(application);
		policySelectionCriteria.setIdentifier(identifier);
		policySelectionCriteria.setLifecycle(lifecycle);
		policySelectionCriteria.setSelectClause(" application, identifier, lifecycle, useability,otherdata, created, updated, epochtime ");

		SqlWithParms sqlWithParms = policiesDAO.constructSelectPolicySql(policySelectionCriteria);
		List<Policies> policiesList = policiesDAO.runSelectPolicieSql(sqlWithParms);
		
		DataHunterRestApiResponsePojo repsonse = new DataHunterRestApiResponsePojo();
		repsonse.setPolicies(policiesList);
		repsonse.setRowsAffected(policiesList.size());
		
		if (policiesList.size() == 1 ){
			repsonse.setSuccess(String.valueOf(true));			
			repsonse.setFailMsg("");
		} else if (policiesList.size() == 0){
			repsonse.setSuccess(String.valueOf(false));			
			repsonse.setFailMsg("No rows matching the selection.");
		} else {
			repsonse.setSuccess(String.valueOf(false));	
			repsonse.setFailMsg("sql execution : Error.  1 row should of been affected, but sql result indicates " + policiesList.size() + " rows affected?" );
		}
		return ResponseEntity.ok(repsonse);	
	}
	
	
	/**
	 * Retrieve selected Items 
	 * 
	 * @param application  application
	 * @param lifecycle    blank to select all lifecycle values matching the other criteria
	 * @param useability   {@link DataHunterConstants#USEABILITY_LIST}, blank to select all useability values matching the other criteria
	 * @return ResponseEntity (ok)  list of items matching the above criteria
	 */
	@GetMapping(path = "/printSelectedPolicies")
	public ResponseEntity<Object> printSelectedPolicies(@RequestParam String application, @RequestParam(required=false) String lifecycle, 
			@RequestParam(required=false) String useability) {
	
		PolicySelectionCriteria policySelectionCriteria = new PolicySelectionCriteria();
		policySelectionCriteria.setApplication(application);
		policySelectionCriteria.setLifecycle(lifecycle);
		policySelectionCriteria.setUseability(useability);
		
		policySelectionCriteria.setSelectClause(" application, identifier, lifecycle, useability,otherdata, created, updated, epochtime ");
		SqlWithParms sqlWithParms = policiesDAO.constructSelectPoliciesSql(policySelectionCriteria);
		
		List<Policies> policiesList = policiesDAO.runSelectPolicieSql(sqlWithParms);
		
		DataHunterRestApiResponsePojo repsonse = new DataHunterRestApiResponsePojo();
		repsonse.setPolicies(policiesList);
		repsonse.setRowsAffected(policiesList.size());
		repsonse.setSuccess(String.valueOf(true));			
		repsonse.setFailMsg("");

		return ResponseEntity.ok(repsonse);	
	}
	
	
	/**
	 * Delete an Item 
	 * <br>If exists delete Item with matching application,identifier,lifecycle
	 * 
	 * @param application application
	 * @param identifier  identifier
	 * @param lifecycle   lifecycle
	 * @return ResponseEntity (ok) indicates if a Item was deleted or not (rowsAffected count) 
	 */
	@GetMapping(path = "/deletePolicy")
	public ResponseEntity<Object> deletePolicy(@RequestParam String application, @RequestParam String identifier, String lifecycle){
	
		PolicySelectionCriteria policySelectionCriteria = new PolicySelectionCriteria();
		policySelectionCriteria.setApplication(application);
		policySelectionCriteria.setIdentifier(identifier);
		policySelectionCriteria.setLifecycle(lifecycle);		
		
		SqlWithParms sqlWithParms = policiesDAO.constructDeletePoliciesSql(policySelectionCriteria);
		
		DataHunterRestApiResponsePojo repsonse = new DataHunterRestApiResponsePojo();
		repsonse.setPolicies(Collections.singletonList(new Policies(application,identifier,lifecycle, null, null, null)));
		int rowsAffected;
		
		try {
			rowsAffected = policiesDAO.runDatabaseUpdateSql(sqlWithParms);
		} catch (Exception e) {
			repsonse.setSuccess(String.valueOf(false));			
			repsonse.setFailMsg("sql exception caught: " + e.getMessage() + "[" + policySelectionCriteria + "]"  );
			repsonse.setRowsAffected(-1);
			return ResponseEntity.ok(repsonse);		
		}	

		repsonse.setRowsAffected(rowsAffected);

		if (rowsAffected == 1 ){
			repsonse.setSuccess(String.valueOf(true));	
			repsonse.setFailMsg("");
		} else if (rowsAffected == 0){
			repsonse.setSuccess(String.valueOf(true));	
			repsonse.setFailMsg("No rows matching the selection.");
		} else {
			repsonse.setSuccess(String.valueOf(false));			
			repsonse.setFailMsg("sql execution : Error. Mulitple rows ( "  + rowsAffected + " ) have been affected (deleted)" );
		}
		return ResponseEntity.ok(repsonse);	
	}

	
	/**
	 * Delete multiple Items
	 *  
	 * @param application  application
	 * @param lifecycle    blank to delete all lifecycle values matching the other criteria
	 * @param useability   {@link DataHunterConstants#USEABILITY_LIST}, blank to delete all useability values matching the other criteria
	 * @return ResponseEntity (ok) indicates number of rows deleted
	 */
	@GetMapping(path = "/deleteMultiplePolicies")
	public ResponseEntity<Object> deleteMultiplePolicies(@RequestParam String application, @RequestParam(required=false) String lifecycle, 
			@RequestParam(required=false) String useability) {
	
		PolicySelectionCriteria policySelectionCriteria = new PolicySelectionCriteria();
		policySelectionCriteria.setApplication(application);
		policySelectionCriteria.setLifecycle(lifecycle);
		policySelectionCriteria.setUseability(useability);
	
		SqlWithParms sqlWithParms = policiesDAO.constructDeleteMultiplePoliciesSql(policySelectionCriteria);
		int rowsAffected = policiesDAO.runDatabaseUpdateSql(sqlWithParms);
		
		DataHunterRestApiResponsePojo repsonse = new DataHunterRestApiResponsePojo();
		repsonse.setPolicies(Collections.singletonList(new Policies(application,null,lifecycle, useability, null, null)));
		repsonse.setSuccess(String.valueOf(true));			
		repsonse.setRowsAffected(rowsAffected);
		
		if (rowsAffected == 0 ){
			repsonse.setFailMsg("sql execution OK, but no rows matched the selection criteria.");
		} else {
			repsonse.setFailMsg("");
		}
		return ResponseEntity.ok(repsonse);	
	}
	

	/**
	 * Use Next Item
	 * 
	 * <br>Updates the 'next' Item (determined by the selection criteria) to a useability of 'USED' 
	 * (unless a REUSABLE item, in which the Item is left as REUSABLE).     
	 * 
	 * @param application application 
	 * @param lifecycle   blank to delete all lifecycle values matching the other criteria 
	 * @param useability  {@link DataHunterConstants#USEABILITY_LIST}
	 * @param selectOrder {@link DataHunterConstants#GET_NEXT_POLICY_SELECTOR}
	 * @return ResponseEntity (ok) if fetched, next Item will be the only the element in the policies list  
	 */
	@GetMapping(path = "/useNextPolicy")
	public ResponseEntity<Object> useNextPolicy(@RequestParam String application, @RequestParam(required=false) String lifecycle, 
			@RequestParam String useability, @RequestParam String selectOrder ){
		return nextPolicy(DataHunterConstants.USE, application, lifecycle, useability, selectOrder );
	}
	
	
	/**
	 * 	Lookup Next Item
	 * 
	 *  <br><br>As for {@link #useNextPolicy}, except the Item not updated..
	 * 
	 * @param application application 
	 * @param lifecycle   blank to delete all lifecycle values matching the other criteria 
	 * @param useability  {@link DataHunterConstants#USEABILITY_LIST}
	 * @param selectOrder {@link DataHunterConstants#GET_NEXT_POLICY_SELECTOR}
	 * @return ResponseEntity (ok) if fetched, next Item will be the only the element in the policies list  
	 */
	@GetMapping(path = "/lookupNextPolicy")
	public ResponseEntity<Object> lookupNextPolicy(@RequestParam String application, @RequestParam(required=false) String lifecycle, 
			@RequestParam String useability, @RequestParam String selectOrder ){
		return nextPolicy(DataHunterConstants.LOOKUP, application, lifecycle, useability, selectOrder);
	}	
	
	
	/**
	 * Next Policy (lookup or use)
	 * 
	 * <br><br>As per {@link #useNextPolicy} or {@link #lookupNextPolicy}.  Which of these actions is invoked depends upon the value of 
	 * the lookupOrUse parameter:
	 * <br><br>'USE' invokes {@link #useNextPolicy}, 'LOOKUP' invokes {@link #lookupNextPolicy} (also the default action for 
	 * other values).   
	 * 
	 * @param lookupOrUse 'USE' or 'LOOKUP' (other values behave as if a 'LOOKUP').  
	 * @param application application 
	 * @param lifecycle   blank to delete all lifecycle values matching the other criteria 
	 * @param useability  {@link DataHunterConstants#USEABILITY_LIST}
	 * @param selectOrder {@link DataHunterConstants#GET_NEXT_POLICY_SELECTOR}
	 * @return ResponseEntity (ok) if fetched, next Item will be the only the element in the policies list  
	 * 
	 */
	@GetMapping(path = "/nextPolicy")
	public ResponseEntity<Object> nextPolicy(String lookupOrUse, String application, String lifecycle, String useability, String selectOrder ){

		PolicySelectionCriteria policySelectionCriteria = new PolicySelectionCriteria();
		policySelectionCriteria.setApplication(application);
		policySelectionCriteria.setLifecycle(lifecycle);
		policySelectionCriteria.setUseability(useability);
		policySelectionCriteria.setSelectClause(
				" application, identifier, lifecycle, useability, otherdata, created, updated, epochtime ");
		policySelectionCriteria.setSelectOrder(selectOrder);

		List<Policies> policiesList;
		SqlWithParms selectSqlWithParms = policiesDAO.constructSelectPoliciesSql(policySelectionCriteria);

		DataHunterRestApiResponsePojo response = new DataHunterRestApiResponsePojo();
		response.setPolicies(Collections.singletonList( // just setting for debug purposes on failure 
				new Policies(application,null, lifecycle, useability, "(lookup=" + lookupOrUse + " selectOrder="+ selectOrder +""  , null)));
		
		synchronized (this) {
		
			try {
				policiesList = policiesDAO.runSelectPolicieSql(selectSqlWithParms);
			} catch (Exception e) {
				response.setSuccess(String.valueOf(false));			
				response.setFailMsg("sql exception caught: " + e.getMessage() + "[" + response.getPolicies() + "]"  );
				response.setRowsAffected(-1);
				return ResponseEntity.ok(response);		
			}
	
			response.setRowsAffected(policiesList.size());
	
			if (policiesList.size() == 0) {
				response.setSuccess(String.valueOf(false));	
				response.setFailMsg("No rows matching the selection.  Possibly we have ran out of data for application:["
								+ policySelectionCriteria.getApplication() + "]");
				return ResponseEntity.ok(response);		
	
			} else if (policiesList.size() > 1) {
				response.setPolicies(policiesList);
				response.setSuccess(String.valueOf(false));	
				response.setFailMsg("sql execution : Error.  1 row should of been selected, but sql result indicates "
								+ policiesList.size() + " rows selected? (review the policies list) ");
				return ResponseEntity.ok(response);	
			}
	
			response.setPolicies(policiesList);
			Policies nextPolicy = policiesList.get(0);
			
			if (DataHunterConstants.USE.equalsIgnoreCase(lookupOrUse)){
				
				if (DataHunterConstants.REUSABLE.equalsIgnoreCase(nextPolicy.getUseability())) {
					response.setSuccess(String.valueOf(true));
					response.setRowsAffected(0);			
					response.setFailMsg("Policy " + nextPolicy.getIdentifier()+ " NOT updated as it is marked as REUSABLE");
					return ResponseEntity.ok(response);	
				}
				
				response = updateNextPolicy(response, nextPolicy);

			} else { // a lookup
				response.setSuccess(String.valueOf(true));
				response.setFailMsg("OK (no update)");
			}
				
		} //sync block
		return ResponseEntity.ok(response);	
	}


	/**
	 * Change the Use State for an Item, or for multiple Items in an Application
	 * 
	 * @param application application
	 * @param identifier (optional) blank to select all identifier values matching the other criteria (application, useability)
	 * @param useability {@link DataHunterConstants#USEABILITY_LIST}(optional) blank to select all useability values matching the other criteria
	 *  (the parameters above)
	 * @param toUseability {@link DataHunterConstants#USEABILITY_LIST}. Items matching the selection criteria (the parameters above), will have 
	 *  useability changed to this value  
	 * @param toEpochTime (optional, numeric) current epochtime is used if no, blank or non-numeric value passed   
	 * @return ResponseEntity (ok) indicates number of rows updated
	 */
	@GetMapping(path = "/updatePoliciesUseState")
	public ResponseEntity<Object> updatePoliciesUseState(@RequestParam String application, @RequestParam(required=false) String identifier, 
			@RequestParam(required=false) String useability, @RequestParam String toUseability, @RequestParam(required=false) String toEpochTime){
	
		UpdateUseStateAndEpochTime updateUseStateAndEpochTime = new UpdateUseStateAndEpochTime();
		updateUseStateAndEpochTime.setApplication(application);
		updateUseStateAndEpochTime.setIdentifier(identifier);
		updateUseStateAndEpochTime.setUseability(useability);
		updateUseStateAndEpochTime.setToUseability(toUseability);
		
		if (StringUtils.isNumeric(toEpochTime)) {
			updateUseStateAndEpochTime.setToEpochTime(Long.parseLong(toEpochTime.trim()));
		} else {
			updateUseStateAndEpochTime.setToEpochTime(null);		
		}
		SqlWithParms sqlWithParms = policiesDAO.constructUpdatePoliciesUseStateSql(updateUseStateAndEpochTime);

		DataHunterRestApiResponsePojo repsonse = new DataHunterRestApiResponsePojo();
		int rowsAffected;
		try {
			rowsAffected = policiesDAO.runDatabaseUpdateSql(sqlWithParms);
		} catch (Exception e) {
			repsonse.setSuccess(String.valueOf(false));			
			repsonse.setFailMsg("sql exception caught: " + e.getMessage() + "[" + updateUseStateAndEpochTime + "]"  );
			repsonse.setRowsAffected(-1);
			return ResponseEntity.ok(repsonse);		
		}	
		
		repsonse.setPolicies(Collections.singletonList(new Policies(application,identifier,null, useability, null, null)));
		repsonse.setSuccess(String.valueOf(true));			
		repsonse.setRowsAffected(rowsAffected);

		if (rowsAffected == 0 ){
			repsonse.setFailMsg("sql execution OK, but no rows matched the selection criteria.  Nothing was updated on the database");			
		} else if (rowsAffected == 1 ){  
			repsonse.setFailMsg("");
		} else {
			repsonse.setFailMsg("sql execution OK.  Note muliple rows where updated by this query");
		}	
		return ResponseEntity.ok(repsonse);	
	}
	
	
	/**
	 * Asynchronous Message Analyzer
	 * 
	 * <p>Provides a timing calculation between a set of Items that match the input criteria.  It is designed to assist with timing 
	 * asynchronous events during a performance test.  For further details please refer to the Mark59 user guide, and sample 
	 * usages from <code>DataHunterRestApiClientSampleUsage</code> in the mark59-datahunter-api project.
	 * 
	 * @param applicationStartsWithOrEquals  must be "EQUALS" or "STARTS_WITH" (applied to application selection)
	 * @param application application
	 * @param identifier identifier leave blank to select all identifier values matching the other criteria (application, useability)
	 * @param useability {@link DataHunterConstants#USEABILITY_LIST} leave blank to select all useability values matching the other criteria
	 *  (the parameters above).  Note that the value 'UNPAIRED' was specifically created for use in this function (but you have the option to use other values).  
	 * @param toUseability {@link DataHunterConstants#USEABILITY_LIST}. 'Matched' Items satisfying the selection criteria (the parameters above), will have
	 *  useability changed to this value 
	 * @return  ResponseEntity (ok)  The getAsyncMessageaAnalyzerResults list in the response provides the results, including the max time difference
	 *  between each set of matched rows.  
	 */
	@GetMapping(path = "/asyncMessageAnalyzer")
	public ResponseEntity<Object> asyncMessageAnalyzer(@RequestParam String applicationStartsWithOrEquals, @RequestParam String application, 
			@RequestParam(required=false) String identifier, @RequestParam(required=false) String useability, @RequestParam(required=false) String toUseability){
	
		DataHunterRestApiResponsePojo repsonse = new DataHunterRestApiResponsePojo();
		// fudge: the 'otherData' field is populated with the applicationStartsWithOrEquals param
		repsonse.setPolicies(Collections.singletonList(new Policies(application,identifier,null, useability, applicationStartsWithOrEquals, null)));
		
		if ( ! (DataHunterConstants.EQUALS.equalsIgnoreCase(applicationStartsWithOrEquals) 
				|| DataHunterConstants.STARTS_WITH.equalsIgnoreCase(applicationStartsWithOrEquals))) {
			repsonse.setRowsAffected(0);
			repsonse.setSuccess(String.valueOf(false));			
			repsonse.setFailMsg("applicationStartsWithOrEquals was '" + applicationStartsWithOrEquals + "' - must be either 'EQUALS' or 'STARTS_WITH'");
			return ResponseEntity.ok(repsonse);				
		}
		
		AsyncMessageaAnalyzerRequest asyncMessageaAnalyzerRequest = new AsyncMessageaAnalyzerRequest();
		asyncMessageaAnalyzerRequest.setApplicationStartsWithOrEquals(applicationStartsWithOrEquals);
		asyncMessageaAnalyzerRequest.setApplication(application);
		asyncMessageaAnalyzerRequest.setIdentifier(identifier);
		asyncMessageaAnalyzerRequest.setUseability(useability);
		asyncMessageaAnalyzerRequest.setToUseability(toUseability);

		SqlWithParms analyzerSqlWithParms = policiesDAO.constructAsyncMessageaAnalyzerSql(asyncMessageaAnalyzerRequest);
		List<AsyncMessageaAnalyzerResult> asyncMessageaAnalyzerResultList = new ArrayList<>();
		//System.out.println("DHRC getSqlparameters:" + DataHunterUtils.prettyPrintMap(analyzerSqlWithParms.getSqlparameters().getValues()));
		//System.out.println("DHRC getSql          :" + analyzerSqlWithParms.getSql());

		try {
			asyncMessageaAnalyzerResultList = policiesDAO.runAsyncMessageaAnalyzerSql(analyzerSqlWithParms);
		} catch (Exception e) {
			repsonse.setSuccess(String.valueOf(false));			
			repsonse.setFailMsg("sql exception caught: " + e.getMessage() + "[" + asyncMessageaAnalyzerRequest + "]"  );
			repsonse.setRowsAffected(-1);
			return ResponseEntity.ok(repsonse);		
		}	
		int rowsAffected = asyncMessageaAnalyzerResultList.size();
		repsonse.setAsyncMessageaAnalyzerResults(asyncMessageaAnalyzerResultList);
		
		repsonse.setPolicies(Collections.singletonList(new Policies(application,identifier,null, useability, null, null)));
		repsonse.setSuccess(String.valueOf(true));			
		repsonse.setRowsAffected(rowsAffected);

		if (rowsAffected == 0 ){
			repsonse.setFailMsg("sql execution OK, but no rows matched the selection criteria.  Nothing was updated on the database");			
			return ResponseEntity.ok(repsonse);	
		}	
		
		if ( ! DataHunterUtils.isEmpty(toUseability)){
			try {
				asyncMessageaAnalyzerResultList = policiesDAO.updateMultiplePoliciesUseState(asyncMessageaAnalyzerResultList, toUseability);
			} catch (Exception e) {
				repsonse.setSuccess(String.valueOf(false));			
				repsonse.setFailMsg("sql exception caught: " + e.getMessage() + "[" + asyncMessageaAnalyzerRequest + "]"  );
				repsonse.setRowsAffected(rowsAffected);					
				return  ResponseEntity.ok(repsonse);		
			}
		}
		
		repsonse.setAsyncMessageaAnalyzerResults(asyncMessageaAnalyzerResultList);  
		repsonse.setSuccess(String.valueOf(true));			
		repsonse.setFailMsg("");
		return ResponseEntity.ok(repsonse);	
	}
	

	private DataHunterRestApiResponsePojo updateNextPolicy(DataHunterRestApiResponsePojo response, Policies nextPolicy) {
		try {
			SqlWithParms updateSqlWithParms = policiesDAO.constructUpdatePolicyToUsedSql(nextPolicy);
			int rowsUpdated = policiesDAO.runDatabaseUpdateSql(updateSqlWithParms);

			if (rowsUpdated == 1) {
				response.setSuccess(String.valueOf(true));
				response.setFailMsg("");
			} else {
				response.setSuccess(String.valueOf(false));
				response.setFailMsg("1 row should of been updated, but sql reurn count for update indicates "+ rowsUpdated + " rows affected");
			}
		} catch (Exception e) {
			response.setSuccess(String.valueOf(false));			
			response.setFailMsg("sql exception caught: " + e.getMessage() + "[" + response.getPolicies() + "]"  );
		}
		return response;	
	}
	
}
