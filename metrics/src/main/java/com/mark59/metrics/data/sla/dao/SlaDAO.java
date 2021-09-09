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

package com.mark59.metrics.data.sla.dao;

import java.util.List;

import com.mark59.metrics.data.beans.Sla;
import com.mark59.metrics.form.BulkApplicationPassCountsForm;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public interface SlaDAO {
	 public void insertData(Sla sla);
	 public int bulkInsertOrUpdateApplication(BulkApplicationPassCountsForm bulkApplication);
	 public List<Sla> getSlaList();
	 public List<Sla> getSlaList(String application);
	 public void updateData(Sla sla);
	 public void deleteAllSlasForApplication(String application);
	 public void deleteData(String application, String txnId, String isCdpTxn);
	 public Sla getSla(String application, String txnId, String isCdpTxn);
	 public List<String> findApplications();
	 public List<String> getSlasWithMissingTxnsInThisRunCdpTags(String application, String runTime);
	 public List<String> getListOfIgnoredTransactionsCdpTags(String application);
}
