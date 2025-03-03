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

package com.mark59.datahunter.application;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.mark59.datahunter.data.beans.Policies;
import com.mark59.datahunter.data.policies.dao.PoliciesDAO;
import com.mark59.datahunter.model.PolicySelectionCriteria;
import com.mark59.datahunter.pojo.ReindexResult;
import com.mark59.datahunter.pojo.ValidReuseIxPojo;

public class ReusableIndexedUtils  {

	private int rowsMoved=0;
	
	/**
	 * validation of a ReusableIndexed data type via a Polices object
	 * 
	 * @param policies  a Policies object
	 * @param policiesDAO policiesDAO bean
	 * @return ValidReuseIxPojo result
	 * 
	 * @see #validateReusableIndexed(PolicySelectionCriteria, PoliciesDAO)
	 */
	public static ValidReuseIxPojo validateReusableIndexed(Policies policies, PoliciesDAO policiesDAO){
		PolicySelectionCriteria policySelect = new PolicySelectionCriteria();
		policySelect.setApplication(policies.getApplication());
		policySelect.setLifecycle(policies.getLifecycle());
		policySelect.setUseability(policies.getUseability());
		return validateReusableIndexed(policySelect, policiesDAO);
	}			
	
	
	/**
	 * Validates if a passed Application-Lifecycle-Useability-Usability is a valid ReusableIndexed datatype.  
	 * 
	 * @param policySelect a PolicySelectionCriteria object
	 * @param policiesDAO  policiesDAO bean
	 * @return ValidReuseIxPojo result
	 */
	public static ValidReuseIxPojo validateReusableIndexed(PolicySelectionCriteria policySelect, PoliciesDAO policiesDAO){

		ValidReuseIxPojo validReuseIxPojo = new ValidReuseIxPojo();
		validReuseIxPojo.setPolicyReusableIndexed(false);
		validReuseIxPojo.setValidatedOk(true);
		validReuseIxPojo.setErrorMsg("Not Reusable Indexed data (usability of REUSABLE required) ");
		validReuseIxPojo.setCurrentIxCount(-1);
		validReuseIxPojo.setIdsinRangeCount(0);
		
		if (DataHunterConstants.REUSABLE.equals(policySelect.getUseability())){
			Policies ixPolicyRow = new Policies();
			ixPolicyRow.setApplication(policySelect.getApplication());
			ixPolicyRow.setIdentifier(DataHunterConstants.INDEXED_ROW_COUNT);
			ixPolicyRow.setLifecycle(policySelect.getLifecycle());
			
			SqlWithParms sqlWithParmsIx = policiesDAO.constructSelectPolicySql(ixPolicyRow);
			List<Policies> policiesIxs = policiesDAO.runSelectPolicieSql(sqlWithParmsIx);
			
			if (!policiesIxs.isEmpty() && DataHunterConstants.REUSABLE.equals(policiesIxs.get(0).getUseability())){	
				// an index row exists for this set of REUSABLE data
				validReuseIxPojo.setPolicyReusableIndexed(true);
				ixPolicyRow = policiesIxs.get(0);
				validReuseIxPojo.setIxPolicy(ixPolicyRow); 
				
				if (StringUtils.isNumeric(ixPolicyRow.getOtherdata().trim())){
					int currentIxCount = Integer.valueOf(ixPolicyRow.getOtherdata().trim());
					validReuseIxPojo.setCurrentIxCount(currentIxCount);
					
					SqlWithParms sqlWithParms = policiesDAO.countReusableIndexedIdsInExpectedRange(policySelect, currentIxCount);
					int idsinRangeCount = policiesDAO.runCountSql(sqlWithParms);
					validReuseIxPojo.setIdsinRangeCount(idsinRangeCount);
				} else {
					validReuseIxPojo.setValidatedOk(false);
					validReuseIxPojo.setErrorMsg("Error: For selection "+ policySelect + " numeric value expected in Otherdata"
							+ "for the Reusuabe Index row, but was " + ixPolicyRow.getOtherdata().trim());
					return validReuseIxPojo;
				}
			} else { // no index row
				validReuseIxPojo.setErrorMsg("Not marked as Reusable Indexed Data (no Id 0000000000_IX row) ");
			}
		}
		return validReuseIxPojo;
	}	
	
	
	/**
	 * Updates the index counter of a ReusableIndexed datatype
	 * 
	 * @param policies
	 * @param indexedId the number of rows (excluding the index itself) in a ReusableIndexed datatype.
	 * @param policiesDAO policiesDAO bean
	 * @return affected row count (1 - the index row) 
	 */
	public static int updateIndexedRowCounter(Policies policies, int indexedId, PoliciesDAO policiesDAO) {
		policies.setIdentifier(DataHunterConstants.INDEXED_ROW_COUNT);
		policies.setOtherdata(String.valueOf(indexedId));
		policies.setEpochtime(System.currentTimeMillis());
		SqlWithParms sqlWithParms = policiesDAO.constructUpdatePoliciesSql(policies);
		return policiesDAO.runDatabaseUpdateSql(sqlWithParms);
	}


	/**
	 * This will remove 'holes' the in the ids for a ReusableIndexed datatype.  Id's are ideally a contiguous
	 * 'numeric' (actually stored as a string with leading zeros).
	 * 
	 * Where rows exist than do not have valid ids ('numeric' and within the row range count), they will be shuffled 
	 * into any holes in the range. When the holes are filled they are added to the end of the range.    
	 * 
	 * @param application  application for the ReusableIndexed datatype
	 * @param lifecycle lifecycle for the ReusableIndexed datatype   
	 * @param policiesDAO policiesDAO bean
	 * @return ReindexResult indicates success and rows affected (moved)
	 */
	public ReindexResult reindexReusableIndexed(String application, String lifecycle, PoliciesDAO policiesDAO){
		ReindexResult result = new ReindexResult();
		result.setSuccess(false);
		result.setMessage("?");
		result.setRowsMoved(0);
		result.setIxCount(-1); 
		
		PolicySelectionCriteria targetData =  new PolicySelectionCriteria();
		targetData.setApplication(application);
		targetData.setLifecycle(lifecycle);
		targetData.setUseability(DataHunterConstants.REUSABLE);
		
		ValidReuseIxPojo validReuseIx = validateReusableIndexed(targetData, policiesDAO);
		if (!validReuseIx.getPolicyReusableIndexed()){
			result.setMessage(validReuseIx.getErrorMsg());
			return result;
		}
		
		SqlWithParms sqlWithParms = policiesDAO.countNonReusableIdsForReusableIndexedData(application, lifecycle);
		int nonReuseableidsCount = policiesDAO.runCountSql(sqlWithParms);		
		if (nonReuseableidsCount != 0){
			result.setMessage("No action : Application | lifecycle "+application+" | "+lifecycle+" contains Ids "
					+ "that are marked other than REUSABLE. Please reset or remove this data as appropriate");
			return result;
		}
		
		sqlWithParms = policiesDAO.constructCountPoliciesSql(targetData);
		int policyCount = policiesDAO.runCountSql(sqlWithParms) - 1;
		
		sqlWithParms = policiesDAO.constructCollectDataOutOfExpectedIxRangeSql(application, lifecycle, policyCount);
		Stream<Policies> policyStream = policiesDAO.runStreamPolicieSql(sqlWithParms);
		
//		System.out.println(" -- "+application+":"+lifecycle+":"+policyCount); 
		
		Iterator<Policies> policyStreamIter = policyStream.iterator();

		Policies currPolicy = new Policies();
		currPolicy.setApplication(application);
		currPolicy.setLifecycle(lifecycle);
		currPolicy.setUseability(DataHunterConstants.REUSABLE);
		rowsMoved=0;
		
		for (int ix = 1; ix <= policyCount && policyStreamIter.hasNext(); ix++) { // lets start filling up holes
			currPolicy.setIdentifier(StringUtils.leftPad(String.valueOf(ix), 10, "0"));
//			System.out.println("loop "+ix+" currPolicy: "+ currPolicy );
			sqlWithParms = policiesDAO.constructSelectPolicySql(currPolicy);
			List<Policies> existingidInRange = policiesDAO.runSelectPolicieSql(sqlWithParms);
//			System.out.println("loop "+ix+" found: "+ existingidInRange );
			
			if (existingidInRange.isEmpty()) { // a hole in range, use a out of range row to plug it
				movePolicyToHole(currPolicy, ix, policyStreamIter.next(), policiesDAO);
			}
		}
		
		ReusableIndexedUtils.updateIndexedRowCounter(currPolicy, policyCount, policiesDAO);
		
		result.setIxCount(policyCount); 
		result.setSuccess(true);
		result.setMessage(DataHunterConstants.OK);
		result.setRowsMoved(rowsMoved);
		return result;
	}


	private synchronized void movePolicyToHole(Policies currPolicy, int ix, Policies toMovePolicy, PoliciesDAO policiesDAO){
		//System.out.println("moving:"+toMovePolicy+", to:"+currPolicy);
		PolicySelectionCriteria pscToMovePolicy = new PolicySelectionCriteria();
		pscToMovePolicy.setApplication(toMovePolicy.getApplication());
		pscToMovePolicy.setIdentifier(toMovePolicy.getIdentifier());
		pscToMovePolicy.setLifecycle(toMovePolicy.getLifecycle());
		SqlWithParms sqlWithParms = policiesDAO.constructDeletePoliciesSql(pscToMovePolicy);
		policiesDAO.runDatabaseUpdateSql(sqlWithParms);
		
		currPolicy.setOtherdata(toMovePolicy.getOtherdata());
		sqlWithParms =  policiesDAO.constructInsertDataSql(currPolicy);
		policiesDAO.runDatabaseUpdateSql(sqlWithParms);
		rowsMoved++; 
	}
	
}