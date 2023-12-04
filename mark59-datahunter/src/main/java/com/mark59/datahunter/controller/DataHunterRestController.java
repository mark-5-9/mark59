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
import com.mark59.datahunter.application.SqlWithParms;
import com.mark59.datahunter.data.beans.Policies;
import com.mark59.datahunter.data.policies.dao.PoliciesDAO;
import com.mark59.datahunter.model.AsyncMessageaAnalyzerRequest;
import com.mark59.datahunter.model.AsyncMessageaAnalyzerResult;
import com.mark59.datahunter.model.CountPoliciesBreakdown;
import com.mark59.datahunter.model.DataHunterRestApiResponsePojo;
import com.mark59.datahunter.model.PolicySelectionCriteria;
import com.mark59.datahunter.model.PolicySelectionFilter;
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
 *  policy entry in the first position of the policies list.
 *  <li>For detailed examples of the usage of this service, see class <code>DataHunterRestApiClientSampleUsage</code> in the mark59-datahunter-api project    
 *  </ul>  
 *   
 *  @see DataHunterRestApiResponsePojo
 *   
 * @author Philip Webb
 * Written: Australian Summer 2021/22 
 *   
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
	 * @param identifier  identifier
	 * @param lifecycle   lifecycle
	 * @param useability  One of {@link DataHunterConstants#USEABILITY_LIST}, 
	 * @param otherdata   (set to empty if null passed)
	 * @param epochtime   (maps to Long) the system current time is used a numeric value is not passed
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
		
		if (otherdata==null){
			policies.setOtherdata("");
		} else {
			policies.setOtherdata(otherdata);
		}
		
		if (StringUtils.isNumeric(epochtime)){
			policies.setEpochtime(Long.parseLong(epochtime)  );
		} else {
			policies.setEpochtime(System.currentTimeMillis());
		}
		
		SqlWithParms sqlWithParms = policiesDAO.constructInsertDataSql(policies);
		int rowsAffected;

		DataHunterRestApiResponsePojo response = new DataHunterRestApiResponsePojo();
		response.setPolicies(Collections.singletonList(policies));
		
		if (!DataHunterConstants.USEABILITY_LIST.contains(useability)){
			return ResponseEntity.ok(UseabilityError(useability, response));	
		}

		try {
			rowsAffected = policiesDAO.runDatabaseUpdateSql(sqlWithParms);
		} catch (Exception e) {
			response.setSuccess(String.valueOf(false));			
			response.setFailMsg("sql exception caught: " + e.getMessage() + ", sqlWithParms=" + sqlWithParms);
			response.setRowsAffected(-1);
			return ResponseEntity.ok(response);	
		}	
		response.setRowsAffected(rowsAffected);

		if (rowsAffected == 1 ){
			response.setSuccess(String.valueOf(true));			
			response.setFailMsg("");
		} else {
			response.setSuccess(String.valueOf(false));	
			response.setFailMsg("sql execution : Error.  1 row should of been affected, but sql result indicates " + rowsAffected + " rows affected?" );
		}
		return ResponseEntity.ok(response);	
	}


	/**
	 * Count Items matching the selection criteria 
	 * 
	 * @param application  application
	 * @param lifecycle    blank to select all lifecycle values matching the other criteria
	 * @param useability   blank to select all useability values matching the other criteria, 
	 *                     otherwise one of {@link DataHunterConstants#USEABILITY_LIST}
	 * @return ResponseEntity (ok) indicates the count of policies satisfying selection criteria
	 */
	@GetMapping(path = "/countPolicies")
	public ResponseEntity<Object> countPolicies(@RequestParam String application, @RequestParam(required=false) String lifecycle, 
			@RequestParam(required=false) String useability) {
	
		PolicySelectionCriteria policySelectionCriteria = new PolicySelectionCriteria();
		policySelectionCriteria.setApplication(application);
		policySelectionCriteria.setLifecycle(lifecycle);
		policySelectionCriteria.setUseability(useability);
		
		DataHunterRestApiResponsePojo response = new DataHunterRestApiResponsePojo();
		response.setPolicies(Collections.singletonList(new Policies(application,null,lifecycle, useability, null, null)));
		if (StringUtils.isNotBlank(useability) && !DataHunterConstants.USEABILITY_LIST.contains(useability)){
			return ResponseEntity.ok(UseabilityError(useability, response));	
		}
		
		SqlWithParms sqlWithParms = policiesDAO.constructCountPoliciesSql(policySelectionCriteria);
		int rowsAffected = policiesDAO.runCountSql(sqlWithParms);
		
		response.setRowsAffected(rowsAffected);
		response.setSuccess(String.valueOf(true));			
		response.setFailMsg("");
		return ResponseEntity.ok(response);	
	}
	
	
	/**
	 * Breakdown of counts by application, lifecycle and useability for Items matching the selection criteria. 
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
	@GetMapping(path = "/policiesBreakdown")
	public ResponseEntity<Object> policiesBreakdown(@RequestParam String applicationStartsWithOrEquals, @RequestParam String application, 
			@RequestParam(required=false) String lifecycle,	@RequestParam(required=false) String useability) {

		DataHunterRestApiResponsePojo response = new DataHunterRestApiResponsePojo();
		// fudge: the 'otherData' field is populated with the applicationStartsWithOrEquals param
		response.setPolicies(Collections.singletonList(new Policies(application,null,lifecycle, useability, applicationStartsWithOrEquals, null)));
	
		if ( ! (DataHunterConstants.EQUALS.equalsIgnoreCase(applicationStartsWithOrEquals)
				|| DataHunterConstants.STARTS_WITH.equalsIgnoreCase(applicationStartsWithOrEquals))) {
			response.setRowsAffected(0);
			response.setSuccess(String.valueOf(false));		
			response.setFailMsg("applicationStartsWithOrEquals was '" + applicationStartsWithOrEquals + "' - must be either 'EQUALS' or 'STARTS_WITH'");
			return ResponseEntity.ok(response);				
		}
		
		if (StringUtils.isNotBlank(useability) && !DataHunterConstants.USEABILITY_LIST.contains(useability)){
			return ResponseEntity.ok(UseabilityError(useability, response));	
		}
		
		PolicySelectionCriteria policySelectionCriteria = new PolicySelectionCriteria();
		policySelectionCriteria.setApplicationStartsWithOrEquals(applicationStartsWithOrEquals);
		policySelectionCriteria.setApplication(application);
		policySelectionCriteria.setLifecycle(lifecycle);
		policySelectionCriteria.setUseability(useability);
		
		SqlWithParms sqlWithParms = policiesDAO.constructCountPoliciesBreakdownSql(policySelectionCriteria);
		List<CountPoliciesBreakdown> countPoliciesBreakdownList = policiesDAO.runCountPoliciesBreakdownSql(sqlWithParms);
		int rowsAffected = countPoliciesBreakdownList.size();

		response.setCountPoliciesBreakdown(countPoliciesBreakdownList);
		response.setRowsAffected(rowsAffected);
		response.setSuccess(String.valueOf(true));			
		
		if (rowsAffected == 0 ){
			response.setFailMsg("sql execution OK, but no rows matched the selection criteria.");
		} else {
			response.setFailMsg("" );
		}
		return ResponseEntity.ok(response);	
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
		policySelectionCriteria.setSelectClause(PoliciesDAO.SELECT_POLICY_COLUMNS);

		SqlWithParms sqlWithParms = policiesDAO.constructSelectPolicySql(policySelectionCriteria);
		List<Policies> policiesList = policiesDAO.runSelectPolicieSql(sqlWithParms);
		
		DataHunterRestApiResponsePojo response = new DataHunterRestApiResponsePojo();
		response.setPolicies(policiesList);
		response.setRowsAffected(policiesList.size());
		
		if (policiesList.size() == 1 ){
			response.setSuccess(String.valueOf(true));			
			response.setFailMsg("");
		} else if (policiesList.size() == 0){
			response.setSuccess(String.valueOf(false));			
			response.setFailMsg("No rows matching the selection.");
		} else {
			response.setSuccess(String.valueOf(false));	
			response.setFailMsg("sql execution : Error.  1 row should of been affected, but sql result indicates " + policiesList.size() + " rows affected?" );
		}
		return ResponseEntity.ok(response);	
	}
	
	
	/**
	 * Retrieve selected Items 
	 * 
	 * <p>To get a feel of the optional Policy Selection filter usage, also refer to the Datahunter UI page 
	 * ../mark59-datahunter/print_selected_policies, and the Mark59 User Guide ('DataHunter' chapter)
	 * 
	 * <p>The filters have the same format as per the UI.
	 * 
	 * @param application  	application (required)
	 * @param lifecycle    	blank to select all lifecycles matching the other criteria
	 * @param useability   	{@link DataHunterConstants#USEABILITY_LIST},or blank to select all useabilities matching the other criteria
	 * @param selectOrder	field used to ORDER the list by {@link DataHunterConstants#FILTERED_SELECT_ORDER_LIST} default is natural key order
	 * @param otherdataSelected true|false to filter on otherdata
	 * @param otherdata     otherdata filter - SQL 'LIke' format is used eg %5other% 
	 * @param createdSelected true|false to filter on a created date range
	 * @param createdFrom  	eg 2001-01-01 01:01:10.000001 
	 * @param createdTo		eg 2099-12-31 23:59:59.999999
	 * @param updatedSelected true|false to filter on an updated date range
	 * @param updatedFrom	eg 2001-01-01 01:01:10.000001
	 * @param updatedTo		eg 2099-12-31 23:59:59.999999
	 * @param epochtimeSelected true|false to filter on an epochtime range
	 * @param epochtimeFrom eg 0   (max 13 numerics)
	 * @param epochtimeTo	eg 4102444799999 (max 13 numerics)
	 * @param orderDirection ASCENDING (default) | DESCENDING  {@link DataHunterConstants#ORDER_DIRECTION_LIST}
	 * @param limit			0 to 1000  (default 100, if gt 1000 will be set to 1000)
	 * @return ResponseEntity (ok) list of items matching the above criteria.  Maximum of 1000 returned rows
	 */
	@GetMapping(path = "/printSelectedPolicies")
	public ResponseEntity<Object> printSelectedPolicies(@RequestParam String application, @RequestParam(required=false) String lifecycle, 
			@RequestParam(required=false) String useability,@RequestParam(required=false) String selectOrder, 
			@RequestParam(required=false) String otherdataSelected,@RequestParam(required=false) String otherdata,
			@RequestParam(required=false) String createdSelected,@RequestParam(required=false) String createdFrom,@RequestParam(required=false) String createdTo,
			@RequestParam(required=false) String updatedSelected,@RequestParam(required=false) String updatedFrom,@RequestParam(required=false) String updatedTo,
			@RequestParam(required=false) String epochtimeSelected,@RequestParam(required=false) String epochtimeFrom,@RequestParam(required=false) String epochtimeTo,
			@RequestParam(required=false) String orderDirection,@RequestParam(required=false) String limit ){
		
		PolicySelectionFilter policySelectionFilter = new PolicySelectionFilter();
		policySelectionFilter.setApplication(application);
		policySelectionFilter.setLifecycle(lifecycle);
		policySelectionFilter.setUseability(useability);
		policySelectionFilter.setSelectOrder(selectOrder);
		policySelectionFilter.setOtherdataSelected(Boolean.valueOf(otherdataSelected));
		policySelectionFilter.setOtherdata(otherdata);	
		policySelectionFilter.setCreatedSelected(Boolean.valueOf(createdSelected));
		policySelectionFilter.setCreatedFrom(createdFrom);	
		policySelectionFilter.setCreatedTo(createdTo);
		policySelectionFilter.setUpdatedSelected(Boolean.valueOf(updatedSelected));
		policySelectionFilter.setUpdatedFrom(updatedFrom);	
		policySelectionFilter.setUpdatedTo(updatedTo);		
		policySelectionFilter.setEpochtimeSelected(Boolean.valueOf(epochtimeSelected));
		policySelectionFilter.setEpochtimeFrom(epochtimeFrom);	
		policySelectionFilter.setEpochtimeTo(epochtimeTo);
		policySelectionFilter.setOrderDirection(orderDirection);
		policySelectionFilter.setLimit(limit);	
		
		DataHunterRestApiResponsePojo response = new DataHunterRestApiResponsePojo();
		if (StringUtils.isNotBlank(useability) && !DataHunterConstants.USEABILITY_LIST.contains(useability)){
			return ResponseEntity.ok(UseabilityError(useability, response));	
		}
		SqlWithParms sqlWithParms = policiesDAO.constructSelectPoliciesFilterSql(policySelectionFilter);

		List<Policies> policiesList = new ArrayList<>(); 
		try {
			policiesList = policiesDAO.runSelectPolicieSql(sqlWithParms);
		} catch (Exception e) {
			response.setSuccess(String.valueOf(false));			
			response.setFailMsg("sql exception caught: " + e.getMessage() + ", sqlWithParms=" + sqlWithParms);
			response.setRowsAffected(-1);
			return ResponseEntity.ok(response);		
		}	
		response.setPolicies(policiesList);
		response.setRowsAffected(policiesList.size());
		response.setSuccess(String.valueOf(true));			
		response.setFailMsg("");
		return ResponseEntity.ok(response);	
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
		
		DataHunterRestApiResponsePojo response = new DataHunterRestApiResponsePojo();
		response.setPolicies(Collections.singletonList(new Policies(application,identifier,lifecycle, null, null, null)));
		int rowsAffected;
		
		try {
			rowsAffected = policiesDAO.runDatabaseUpdateSql(sqlWithParms);
		} catch (Exception e) {
			response.setSuccess(String.valueOf(false));			
			response.setFailMsg("sql exception caught: " + e.getMessage() +	", sqlWithParms=" + sqlWithParms 
					+ ", policySelectionCriteria=[" + policySelectionCriteria + "]" );
			response.setRowsAffected(-1);
			return ResponseEntity.ok(response);		
		}	

		response.setRowsAffected(rowsAffected);

		if (rowsAffected == 1 ){
			response.setSuccess(String.valueOf(true));	
			response.setFailMsg("");
		} else if (rowsAffected == 0){
			response.setSuccess(String.valueOf(true));	
			response.setFailMsg("No rows matching the selection.");
		} else {
			response.setSuccess(String.valueOf(false));			
			response.setFailMsg("sql execution : Error. Mulitple rows ( "  + rowsAffected + " ) have been affected (deleted)" );
		}
		return ResponseEntity.ok(response);	
	}

	
	/**
	 * Delete multiple Policies (with optional filters)
	 * 
	 * <p>To get a feel of Policy Selection Filter usage, refer to the Datahunter UI page 
	 * ../mark59-datahunter/select_multiple_policies, and the Mark59 User Guide ('DataHunter' chapter)
	 * 
	 * <p>The filters have the same format as per the UI : 
	 * <br> application  	application (required)
	 * <br> lifecycle    	blank to select all lifecycles matching the other criteria
	 * <br> useability   	{@link DataHunterConstants#USEABILITY_LIST},or blank to select all useabilities matching the other criteria
	 * <br> otherdataSelected true|false to filter on otherdata
	 * <br> otherdata     	otherdata filter - SQL 'LIke' format is used eg %5other% 
	 * <br> createdSelected true|false to filter on a created date range
	 * <br> createdFrom  	eg 2001-01-01 01:01:10.000001 
	 * <br> createdTo		eg 2099-12-31 23:59:59.999999
	 * <br> updatedSelected true|false to filter on an updated date range
	 * <br> updatedFrom		eg 2001-01-01 01:01:10.000001
	 * <br> updatedTo		eg 2099-12-31 23:59:59.999999
	 * <br> epochtimeSelected true|false to filter on an epochtime range
	 * <br> epochtimeFrom 	eg 0   (max 13 numerics)
	 * <br> epochtimeTo		eg 4102444799999 (max 13 numerics)
 	 * 
	 * @param policySelectionFilter selection criteria for Item selection  
	 * @return ResponseEntity (ok) indicates number of rows deleted
	 */
	@GetMapping(path = "/deleteMultiplePolicies")
	public ResponseEntity<Object> deleteMultiplePolicies(@RequestParam String application, @RequestParam(required=false) String lifecycle, 
			@RequestParam(required=false) String useability,@RequestParam(required=false) String otherdataSelected,@RequestParam(required=false) String otherdata,
			@RequestParam(required=false) String createdSelected,@RequestParam(required=false) String createdFrom,@RequestParam(required=false) String createdTo,
			@RequestParam(required=false) String updatedSelected,@RequestParam(required=false) String updatedFrom,@RequestParam(required=false) String updatedTo,
			@RequestParam(required=false) String epochtimeSelected,@RequestParam(required=false) String epochtimeFrom,@RequestParam(required=false) String epochtimeTo){

		PolicySelectionFilter policySelectionFilter = new PolicySelectionFilter();
		policySelectionFilter.setApplication(application);
		policySelectionFilter.setLifecycle(lifecycle);
		policySelectionFilter.setUseability(useability);
		policySelectionFilter.setOtherdataSelected(Boolean.valueOf(otherdataSelected));
		policySelectionFilter.setOtherdata(otherdata);	
		policySelectionFilter.setCreatedSelected(Boolean.valueOf(createdSelected));
		policySelectionFilter.setCreatedFrom(createdFrom);	
		policySelectionFilter.setCreatedTo(createdTo);
		policySelectionFilter.setUpdatedSelected(Boolean.valueOf(updatedSelected));
		policySelectionFilter.setUpdatedFrom(updatedFrom);	
		policySelectionFilter.setUpdatedTo(updatedTo);		
		policySelectionFilter.setEpochtimeSelected(Boolean.valueOf(epochtimeSelected));
		policySelectionFilter.setEpochtimeFrom(epochtimeFrom);	
		policySelectionFilter.setEpochtimeTo(epochtimeTo);
		
		DataHunterRestApiResponsePojo response = new DataHunterRestApiResponsePojo();
		
		if (StringUtils.isNotBlank(useability) && !DataHunterConstants.USEABILITY_LIST.contains(useability)){
			return ResponseEntity.ok(UseabilityError(useability, response));	
		}
		
		SqlWithParms sqlWithParms = policiesDAO.constructDeleteMultiplePoliciesSql(policySelectionFilter);
		int rowsAffected;
		try {
			rowsAffected = policiesDAO.runDatabaseUpdateSql(sqlWithParms);
		} catch (Exception e) {
			response.setSuccess(String.valueOf(false));			
			response.setFailMsg("sql exception caught: " + e.getMessage() + ", sqlWithParms=" + sqlWithParms);
			response.setRowsAffected(-1);
			return ResponseEntity.ok(response);	
		}	

		response.setPolicies(Collections.singletonList(new Policies(application,null,lifecycle, useability, null, null)));
		response.setSuccess(String.valueOf(true));			
		response.setRowsAffected(rowsAffected);
		
		if (rowsAffected == 0 ){
			response.setFailMsg("sql execution OK, but no rows matched the selection criteria.");
		} else {
			response.setFailMsg("");
		}
		return ResponseEntity.ok(response);	
	}
	
	
	/**
	 * Update an existing Item 
	 *  
	 * <p>The Item key is application|identifier|lifecycle.  No action is performed if the item does not exist. 
	 * 
	 * @param application application (of an existing item)
	 * @param identifier  identifier  (of an existing item)
	 * @param lifecycle   lifecycle  (of an existing item)
	 * @param useability  One of {@link DataHunterConstants#USEABILITY_LIST} 
	 * @param otherdata   otherdata (set empty if null passed)
	 * @param epochtime   a long value, or if blank or non-numeric will to set to System.currentTimeMillis().
	 * @return ResponseEntity (ok)
	 */
	@GetMapping(path = "/updatePolicy")
	public ResponseEntity<Object> updatePolicy(@RequestParam String application, @RequestParam String identifier, @RequestParam String lifecycle, 
			@RequestParam String useability, @RequestParam(required=false) String otherdata, @RequestParam(required=false) String epochtime){

		Policies policies = new Policies();
		policies.setApplication(application);
		policies.setIdentifier(identifier);
		policies.setLifecycle(lifecycle);
		
		PolicySelectionCriteria policySelectionCriteria = new PolicySelectionCriteria();
		policySelectionCriteria.setApplication(application);
		policySelectionCriteria.setIdentifier(identifier);
		policySelectionCriteria.setLifecycle(lifecycle);
		policySelectionCriteria.setSelectClause(PoliciesDAO.SELECT_POLICY_COLUMNS);
		SqlWithParms sqlWithParms = policiesDAO.constructSelectPolicySql(policySelectionCriteria);
		
		DataHunterRestApiResponsePojo response = new DataHunterRestApiResponsePojo();
		
		if (!DataHunterConstants.USEABILITY_LIST.contains(useability)){
			return ResponseEntity.ok(UseabilityError(useability, response));	
		}		
		
		List<Policies> existingPoliciesList= new ArrayList<>();
		try {
			existingPoliciesList = policiesDAO.runSelectPolicieSql(sqlWithParms);
		} catch (Exception e) {
			response.setSuccess(String.valueOf(false));			
			response.setFailMsg("sql exception caught (Policy not found): " + e.getMessage() + ", sqlWithParms=" + sqlWithParms);			
			response.setRowsAffected(0);
			return ResponseEntity.ok(response);	
		}	
		if (existingPoliciesList.size() != 1 ){
			response.setSuccess(String.valueOf(false));
			response.setRowsAffected(existingPoliciesList.size());
			response.setFailMsg("Policy does not exist.");
			return ResponseEntity.ok(response);	
		}
		
		policies.setUseability(useability);

		if (StringUtils.isNotBlank(otherdata)){
			policies.setOtherdata(otherdata);
		} else {
			policies.setOtherdata("");
		}
		
		if (StringUtils.isNumeric(epochtime)){
			policies.setEpochtime(Long.parseLong(epochtime));
		} else {
			policies.setEpochtime(null); // DOA will set to current time
		}
				
		sqlWithParms = policiesDAO.constructUpdatePoliciesSql(policies);
		int rowsAffected;
		try {
			rowsAffected = policiesDAO.runDatabaseUpdateSql(sqlWithParms);
		} catch (Exception e) {
			response.setSuccess(String.valueOf(false));			
			response.setFailMsg("sql exception caught: " + e.getMessage() + ", sqlWithParms=" + sqlWithParms);
			response.setRowsAffected(-1);
			return ResponseEntity.ok(response);	
		}	
		response.setRowsAffected(rowsAffected);

		if (rowsAffected == 1 ){
			response.setSuccess(String.valueOf(true));			
			response.setFailMsg("");
			response.setPolicies(Collections.singletonList(policies));
		} else {
			response.setSuccess(String.valueOf(false));	
			response.setFailMsg("sql execution : Error.  1 row should of been affected, but sql result indicates " + rowsAffected + " rows affected?" );
		}
		return ResponseEntity.ok(response);	
	}
	
	
	/**
	 * 	Lookup Next Item
	 * 
	 *  <br><br>As for {@link #useNextPolicy}, except the Item not updated..
	 * 
	 * @param application application 
	 * @param lifecycle   blank to include all lifecycle values matching the other criteria 
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
	 * Use Next Item
	 * 
	 * <br>Updates the 'next' Item (determined by the selection criteria) to a useability of 'USED' 
	 * (unless a REUSABLE item, in which the Item is left as REUSABLE).     
	 * 
	 * @param application application 
	 * @param lifecycle   blank to include all lifecycle values matching the other criteria 
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
	 * Next Policy (lookup or use)
	 * 
	 * <br><br>As per {@link #useNextPolicy} or {@link #lookupNextPolicy}.  Which of these actions is invoked depends upon the value of 
	 * the lookupOrUse parameter:
	 * <br><br>'USE' invokes {@link #useNextPolicy}, 'LOOKUP' invokes {@link #lookupNextPolicy} (also the default action for 
	 * other values).   
	 * 
	 * @param lookupOrUse 'USE' or 'LOOKUP' (other values behave as if a 'LOOKUP').  
	 * @param application application 
	 * @param lifecycle   blank to include all lifecycle values matching the other criteria 
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
		policySelectionCriteria.setSelectOrder(selectOrder);

		List<Policies> policiesList;
		SqlWithParms selectSqlWithParms = policiesDAO.constructSelectNextPolicySql(policySelectionCriteria);

		DataHunterRestApiResponsePojo response = new DataHunterRestApiResponsePojo();
		response.setPolicies(Collections.singletonList( // just setting for debug purposes on failure 
				new Policies(application,null, lifecycle, useability, "(lookup=" + lookupOrUse + " selectOrder="+ selectOrder +""  , null)));
		
		if (!DataHunterConstants.USEABILITY_LIST.contains(useability)){  // forces a Useability value to be passed
			return ResponseEntity.ok(UseabilityError(useability, response));	
		}
		
		synchronized (this) {
		
			try {
				policiesList = policiesDAO.runSelectPolicieSql(selectSqlWithParms);
			} catch (Exception e) {
				response.setSuccess(String.valueOf(false));			
				response.setFailMsg("sql exception caught: " + e.getMessage() + ", selectSqlWithParms=" + selectSqlWithParms); 
				response.setRowsAffected(-1);
				return ResponseEntity.ok(response);		
			}
	
			response.setRowsAffected(policiesList.size());
	
			if (policiesList.size() == 0) {
				response.setSuccess(String.valueOf(false));	
				response.setFailMsg(
						"No rows matching the selection.  Possibly we have ran out of data for application:["
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
	 * @param application  application
	 * @param identifier   blank to select all identifier values matching the other criteria (application, lifecycle, useability)
	 * @param lifecycle    blank to select all identifier values matching the other criteria (application, identifier, useability)
	 * @param useability   blank to select all useability values matching the other criteria, otherwise 
	 *                     {@link DataHunterConstants#USEABILITY_LIST} 
	 * @param toUseability One of {@link DataHunterConstants#USEABILITY_LIST}.
	 * @param toEpochTime  is only updated if passed a numeric value. Eg contains the System.currentTimeMillis() or another integer    
	 * @return ResponseEntity (ok) indicates number of rows updated
	 */
	@GetMapping(path = "/updatePoliciesUseState")
	public ResponseEntity<Object> updatePoliciesUseState(@RequestParam String application, @RequestParam(required=false) String identifier, 
			@RequestParam(required=false) String lifecycle,	@RequestParam(required=false) String useability, 
			@RequestParam String toUseability, @RequestParam(required=false) String toEpochTime){
	
		UpdateUseStateAndEpochTime updateUseStateAndEpochTime = new UpdateUseStateAndEpochTime();
		updateUseStateAndEpochTime.setApplication(application);
		updateUseStateAndEpochTime.setIdentifier(identifier);
		updateUseStateAndEpochTime.setLifecycle(lifecycle);
		updateUseStateAndEpochTime.setUseability(useability);
		updateUseStateAndEpochTime.setToUseability(toUseability);
		
		if (StringUtils.isNumeric(toEpochTime)) {
			updateUseStateAndEpochTime.setToEpochTime(Long.parseLong(toEpochTime.trim()));
		} else {
			updateUseStateAndEpochTime.setToEpochTime(null); // DAO will not update epochtime		
		}
		SqlWithParms sqlWithParms = policiesDAO.constructUpdatePoliciesUseStateSql(updateUseStateAndEpochTime);

		DataHunterRestApiResponsePojo response = new DataHunterRestApiResponsePojo();

		if (StringUtils.isNotBlank(useability) && !DataHunterConstants.USEABILITY_LIST.contains(useability)){
			return ResponseEntity.ok(UseabilityError(useability, response));	
		}
		if (!DataHunterConstants.USEABILITY_LIST.contains(toUseability)){
			return ResponseEntity.ok(UseabilityError(toUseability, response));	
		}

		int rowsAffected;
		try {
			rowsAffected = policiesDAO.runDatabaseUpdateSql(sqlWithParms);
		} catch (Exception e) {
			response.setSuccess(String.valueOf(false));			
			response.setFailMsg("sql exception caught: " + e.getMessage() + ", sqlWithParms=" + sqlWithParms 
					+ ", updateUseStateAndEpochTime=[" + updateUseStateAndEpochTime + "]"  );
			response.setRowsAffected(-1);
			return ResponseEntity.ok(response);		
		}	
		
		response.setPolicies(Collections.singletonList(new Policies(application,identifier,null, useability, null, null)));
		response.setSuccess(String.valueOf(true));			
		response.setRowsAffected(rowsAffected);

		if (rowsAffected == 0 ){
			response.setFailMsg("sql execution OK, but no rows matched the selection criteria.  Nothing was updated on the database");			
		} else if (rowsAffected == 1 ){  
			response.setFailMsg("");
		} else {
			response.setFailMsg("sql execution OK.  Note muliple rows where updated by this query");
		}	
		return ResponseEntity.ok(response);	
	}
	
	
	
	/**
	 * Asynchronous Message Analyzer
	 * 
	 * <p>Provides a timing calculation between a set of Items that match the input criteria.  It is designed to assist with timing 
	 * asynchronous events during a performance test.  For further details please refer to the Mark59 user guide, and sample 
	 * usages from <code>DataHunterRestApiClientSampleUsage</code> in the mark59-datahunter-api project.
	 * 
	 * @param applicationStartsWithOrEquals  must be "EQUALS" or "STARTS_WITH" (applied to application selection)
	 * @param application  application (or application that start with this value) 
	 * @param identifier   blank to select all identifier values matching the other criteria
	 * @param useability   blank to select all useability values matching the other criteria, otherwise {@link DataHunterConstants#USEABILITY_LIST}   
	 * @param toUseability blank to not update useability, otherwise one of {@link DataHunterConstants#USEABILITY_LIST}.
	 * @return  ResponseEntity (ok)  The getAsyncMessageaAnalyzerResults list in the response provides the results, including the max time difference
	 *  between each set of matched rows.  
	 */
	@GetMapping(path = "/asyncMessageAnalyzer")
	public ResponseEntity<Object> asyncMessageAnalyzer(@RequestParam String applicationStartsWithOrEquals, @RequestParam String application, 
			@RequestParam(required=false) String identifier, @RequestParam(required=false) String useability, @RequestParam(required=false) String toUseability){
	
		DataHunterRestApiResponsePojo response = new DataHunterRestApiResponsePojo();
		// fudge: the 'otherData' field is populated with the applicationStartsWithOrEquals param
		response.setPolicies(Collections.singletonList(new Policies(application,identifier,null, useability, applicationStartsWithOrEquals, null)));
		
		if ( ! (DataHunterConstants.EQUALS.equalsIgnoreCase(applicationStartsWithOrEquals) 
				|| DataHunterConstants.STARTS_WITH.equalsIgnoreCase(applicationStartsWithOrEquals))) {
			response.setRowsAffected(0);
			response.setSuccess(String.valueOf(false));			
			response.setFailMsg("applicationStartsWithOrEquals was '" + applicationStartsWithOrEquals + "' - must be either 'EQUALS' or 'STARTS_WITH'");
			return ResponseEntity.ok(response);				
		}
		
		if (StringUtils.isNotBlank(useability) && !DataHunterConstants.USEABILITY_LIST.contains(useability)){
			return ResponseEntity.ok(UseabilityError(useability, response));	
		}
		if (StringUtils.isNotBlank(toUseability) &&  !DataHunterConstants.USEABILITY_LIST.contains(toUseability)){
			return ResponseEntity.ok(UseabilityError(toUseability, response));	
		}
		
		AsyncMessageaAnalyzerRequest asyncMessageaAnalyzerRequest = new AsyncMessageaAnalyzerRequest();
		asyncMessageaAnalyzerRequest.setApplicationStartsWithOrEquals(applicationStartsWithOrEquals);
		asyncMessageaAnalyzerRequest.setApplication(application);
		asyncMessageaAnalyzerRequest.setIdentifier(identifier);
		asyncMessageaAnalyzerRequest.setUseability(useability);
		asyncMessageaAnalyzerRequest.setToUseability(toUseability);

		SqlWithParms analyzerSqlWithParms = policiesDAO.constructAsyncMessageaAnalyzerSql(asyncMessageaAnalyzerRequest);
		List<AsyncMessageaAnalyzerResult> asyncMessageaAnalyzerResultList = new ArrayList<>();

		try {
			asyncMessageaAnalyzerResultList = policiesDAO.runAsyncMessageaAnalyzerSql(analyzerSqlWithParms);
		} catch (Exception e) {
			response.setSuccess(String.valueOf(false));			
			response.setFailMsg("sql exception caught: " + e.getMessage() + ", analyzerSqlWithParms=" + analyzerSqlWithParms 
					+ ", asyncMessageaAnalyzerRequest=[" + asyncMessageaAnalyzerRequest + "]"  );
			response.setRowsAffected(-1);
			return ResponseEntity.ok(response);		
		}	
		int rowsAffected = asyncMessageaAnalyzerResultList.size();
		response.setAsyncMessageaAnalyzerResults(asyncMessageaAnalyzerResultList);
		
		response.setPolicies(Collections.singletonList(new Policies(application,identifier,null, useability, null, null)));
		response.setSuccess(String.valueOf(true));			
		response.setRowsAffected(rowsAffected);

		if (rowsAffected == 0 ){
			response.setFailMsg("sql execution OK, but no rows matched the selection criteria.  Nothing was updated on the database");			
			return ResponseEntity.ok(response);	
		}	
		
		if (StringUtils.isNotBlank(toUseability)){
			try {
				asyncMessageaAnalyzerResultList = policiesDAO.updateMultiplePoliciesUseState(asyncMessageaAnalyzerResultList, toUseability);
			} catch (Exception e) {
				response.setSuccess(String.valueOf(false));			
				response.setFailMsg("sql exception caught: " + e.getMessage() + ", asyncMessageaAnalyzerRequest=[" + asyncMessageaAnalyzerRequest + "]"
						+ ", toUseability=[" + toUseability + "], asyncMessageaAnalyzerResultList= [" + asyncMessageaAnalyzerResultList + "]" );
				response.setRowsAffected(rowsAffected);					
				return  ResponseEntity.ok(response);		
			}
		}
		response.setAsyncMessageaAnalyzerResults(asyncMessageaAnalyzerResultList);  
		response.setSuccess(String.valueOf(true));			
		response.setFailMsg("");
		return ResponseEntity.ok(response);	
	}
	

	private DataHunterRestApiResponsePojo updateNextPolicy(DataHunterRestApiResponsePojo response, Policies nextPolicy) {
		SqlWithParms updateSqlWithParms = new SqlWithParms();
		try {
			updateSqlWithParms = policiesDAO.constructUpdatePolicyToUsedSql(nextPolicy);
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
			response.setFailMsg("sql exception caught: " + e.getMessage() + ", updateSqlWithParms=" + updateSqlWithParms 
					+ ", nextPolicy=[" + nextPolicy + "]"+ ", response.getPolicies=[" + response.getPolicies() + "]"  );
		}
		return response;	
	}

	
	private DataHunterRestApiResponsePojo UseabilityError(String useability, DataHunterRestApiResponsePojo response) {
		response.setSuccess(String.valueOf(false));			
		response.setFailMsg("useability must be one of " + DataHunterConstants.USEABILITY_LIST + ", but was '" + useability + "'.");    
		response.setRowsAffected(-1);
		return response;
	}

}
