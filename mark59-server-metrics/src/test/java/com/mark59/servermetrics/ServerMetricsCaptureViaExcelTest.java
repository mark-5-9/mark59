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

package com.mark59.servermetrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.logging.log4j.Level;
import org.junit.Test;

import com.mark59.core.utils.Log4jConfigurationHelper;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.servermetricsweb.drivers.CommandDriver;
import com.mark59.servermetricsweb.utils.AppConstantsServerMetricsWeb;
import com.mark59.servermetricsweb.utils.ServerMetricsWebUtils;

public class ServerMetricsCaptureViaExcelTest  {
	
	
	@Test
    public final void testSimpleScriptSampleRunnerViaExcel()
    {
		Log4jConfigurationHelper.init(Level.INFO);	
		ServerMetricsCaptureViaExcel groovyscripttest = new ServerMetricsCaptureViaExcel();
		Map<String,String> additionalTestParametersMap = new LinkedHashMap<String,String>();
		additionalTestParametersMap.put(ServerMetricsCaptureViaExcel.OVERRIDE_PROPERTY_MARK59_SERVER_PROFILES_EXCEL_FILE_PATH , 
				"./src/test/resources/simpleSheetWithLocalhostProfileForEachOs/mark59serverprofiles.xlsx");
		JavaSamplerContext groovyscriptcontext = new JavaSamplerContext( setArgs(groovyscripttest, "SimpleScriptSampleRunner") );
		groovyscripttest.setupTest(groovyscriptcontext);
		SampleResult srMain = groovyscripttest.runTest(groovyscriptcontext);
		
		SampleResult[] subResArray = srMain.getSubResults();
		String listOfTxnNames = "";
		String listOfResponses= "";
		for (int i = 0; i < subResArray.length; i++) {
			listOfTxnNames  += "_" + subResArray[i].getSampleLabel() + "_";
			listOfResponses += subResArray[i].getResponseMessage();
		} 
		System.out.println(listOfTxnNames + " : " + listOfResponses );		
		
		assertEquals("wrong txn count", 3, subResArray.length);
		assertTrue(listOfTxnNames + " isnt right, no listng for a_memory_txn" , listOfTxnNames.contains( "a_memory_txn") );		
		assertTrue(listOfTxnNames + " isnt right, no listng for a_cpu_util_txn" , listOfTxnNames.contains( "a_cpu_util_txn") );		
		assertTrue(listOfTxnNames + " isnt right, no listng for some_datapoint" , listOfTxnNames.contains( "some_datapoint") );		
		assertTrue(listOfResponses + " isnt right"  , "PASSPASSPASS".equals(listOfResponses));	  //the failure is not sent 	
    }


	
	@Test
    public final void testSimpleSheetWithLocalhostProfileForEachOs()
    {
		if (AppConstantsServerMetricsWeb.OS.WINDOWS.getOsName().equals(ServerMetricsWebUtils.obtainOperatingSystemForLocalhost()) ||
		      AppConstantsServerMetricsWeb.OS.LINUX.getOsName().equals(ServerMetricsWebUtils.obtainOperatingSystemForLocalhost())){
			
			Log4jConfigurationHelper.init(Level.INFO);	
			ServerMetricsCaptureViaExcel smExcel = new ServerMetricsCaptureViaExcel();
			JavaSamplerContext context = new JavaSamplerContext( setArgs(smExcel, "localhost_" + ServerMetricsWebUtils.obtainOperatingSystemForLocalhost()) );
			smExcel.setupTest(context);
			SampleResult srMain = smExcel.runTest(context);   
			
			SampleResult[] subResArray = srMain.getSubResults();
			
			String listOfTxnNames = "";
			String listOfResponses= "";
			for (int i = 0; i < subResArray.length; i++) {
				listOfTxnNames  += "_" + subResArray[i].getSampleLabel() + "_";
				listOfResponses += subResArray[i].getResponseMessage();
			} 
			System.out.println("list of txns:responses " + listOfTxnNames + " : " + listOfResponses );
			
			String server = CommandDriver.obtainReportedServerId("localhost", "");   //TODO: also test obtainReportedServerId("localhost", "HOSTID")
						
			if (AppConstantsServerMetricsWeb.OS.WINDOWS.getOsName().equals(ServerMetricsWebUtils.obtainOperatingSystemForLocalhost())){
				assertEquals("wrong txn count", 3, subResArray.length);
				assertTrue(listOfTxnNames + " isnt right, no listng for Memory_"+server+"_FreePhysicalG" , listOfTxnNames.contains( "Memory_"+server+"_FreePhysicalG") );
				assertTrue(listOfTxnNames + " isnt right, no listng for Memory_"+server+"_FreeVirtualG"  , listOfTxnNames.contains( "Memory_"+server+"_FreeVirtualG") );
				assertTrue(listOfTxnNames + " isnt right, no listng for CPU_"+server   				     , listOfTxnNames.contains( "CPU_"+server) );
				assertTrue(listOfResponses + " isnt right"  , "PASSPASSPASS".equals(listOfResponses));
			} else  {   // LINUX
				assertTrue("wrong txn count (subResArray.length): should be 3 or 4", subResArray.length >= 3 && subResArray.length <= 4 );			
				assertTrue(listOfTxnNames + " isnt right, no listng for Memory_server_freeG"  		, listOfTxnNames.contains( "Memory_"+server+"_freeG") );
				assertTrue(listOfTxnNames + " isnt right, no listng for Memory_server_totalG"   	, listOfTxnNames.contains( "Memory_"+server+"_totalG") );
				assertTrue(listOfTxnNames + " isnt right, no listng for Memory_server_usedG"   		, listOfTxnNames.contains( "Memory_"+server+"_usedG") );
				//  mpstat is not be installed on Linux by default, so can't be tested
				//  assertTrue(listOfTxnNames + " isnt right, no listng for CPU_server_IDLE"  			, listOfTxnNames.contains( "CPU_"+server+"_IDLE") );
				assertTrue(listOfResponses + " isnt right"  , listOfResponses.startsWith("PASSPASSPASS"));
			} 
		}else {
			System.out.println("This test is not set up for the "  +  ServerMetricsWebUtils.obtainOperatingSystemForLocalhost() + " o/s ... bypassing test case."  );
		}
    }


	private Arguments setArgs(ServerMetricsCaptureViaExcel smExcel, String serverProfileName) {
		Arguments args = smExcel.getDefaultParameters();
		Map<String,String> argmap = args.getArgumentsAsMap();
		
		Map<String,String> testMap = new LinkedHashMap<String,String>();
		testMap.put(ServerMetricsCaptureViaExcel.OVERRIDE_PROPERTY_MARK59_SERVER_PROFILES_EXCEL_FILE_PATH , 
					"./src/test/resources/simpleSheetWithLocalhostProfileForEachOs/mark59serverprofiles.xlsx");
		testMap.put(ServerMetricsCaptureViaExcel.SERVER_PROFILE_NAME, serverProfileName);
		Arguments testargs = Mark59Utils.mergeMapWithAnOverrideMap(argmap,testMap);
		return testargs; 
	}

   
  	
}