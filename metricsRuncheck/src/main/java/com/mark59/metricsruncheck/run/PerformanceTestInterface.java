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

package com.mark59.metricsruncheck.run;

import java.util.List;
import java.util.Map;

import com.mark59.metrics.data.beans.Run;
import com.mark59.metrics.data.beans.Transaction;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public interface PerformanceTestInterface {

	public Run getRunSummary();  
	
	public List<String> getReportedTransactionsList(); 
	
	public Map<String, Transaction> getTransactionResponses();

	public List<Transaction> getSystemMetrics();	
	

	
}
