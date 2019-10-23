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

package com.mark59.metrics.services;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.mark59.metrics.data.beans.Sla;
import com.mark59.metrics.data.sla.dao.SlaDAO;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class SlaServiceImpl  implements SlaService {

	 @Autowired
	 SlaDAO sladao;

	 @Override
	 public void insertData(Sla sla) {
	  sladao.insertData(sla);
	 }

	 @Override
	 public int bulkInsertOrUpdateApplication(String graphApplication, Sla slaKeywithDefaultValues) {
		 return sladao.bulkInsertOrUpdateApplication(graphApplication, slaKeywithDefaultValues  );
	 }
	 
	 @Override
	 public List<Sla> getSlaList() {
	  return sladao.getSlaList();
	 }
	 
	 @Override
	 public List<Sla> getSlaList(String slaApplicationKey) {
	  return sladao.getSlaList(slaApplicationKey);
	 }
	 

	 @Override
	 public void deleteData(String slaApplicationKey, String txnId) {
	  sladao.deleteData(slaApplicationKey, txnId);
	  
	 }

	 
	 @Override
	 public void deleteAllSlasForApplication(String slaApplicationKey) {
		 sladao.deleteAllSlasForApplication(slaApplicationKey);
		 
	 }
	 
	 
	 
	 @Override
	 public Sla getSla(String slaApplicationKey, String txnId, String defaultSlaForApplicationKey) {
	  return sladao.getSla(slaApplicationKey, txnId, defaultSlaForApplicationKey);  
	 }

	 @Override
	 public void updateData(Sla sla) {
	  sladao.updateData(sla);
	  
	 }

	@Override
	public List<String> findApplications() {
		return sladao.findSlaApplicationKeys();
	}




}
