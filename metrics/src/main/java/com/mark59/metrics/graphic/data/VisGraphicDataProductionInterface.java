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

import java.util.List;

import com.mark59.metrics.data.beans.GraphMapping;

/**
 * @author Philip Webb
 * Written: Australian Autumn 2020
 * 
 * Interface to allow Spring Injection for VisGraphicDataProduction   
 */
public interface VisGraphicDataProductionInterface 
{
	public String createDataPoints(String application, GraphMapping graphMapping, String runDatesToGraph,
			List<String> listOfStdTransactionNamesToGraph, List<String> listOfCdpTransactionNamesToGraph, List<String> listOfTransactionNamesToGraphTagged);
}