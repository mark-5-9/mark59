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

package com.mark59.dsl.samples.devtoolsDSL;


import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v127.network.Network;
import org.openqa.selenium.devtools.v127.network.model.RequestWillBeSent;
import org.openqa.selenium.devtools.v127.network.model.ResponseReceived;
import org.openqa.selenium.devtools.v127.network.model.LoadingFinished;

import com.mark59.scripting.selenium.JmeterFunctionsForSeleniumScripts;




/**
 * This class provides a mechanism to create Selenium CDP (Chrome Devtools Protocol) listeners for a request being initiated, and
 * capturing (optionally filtering) responses for those requests.  The response listener will generate a 'CDP' transaction.
 * 
 * <p>For 'ChromiumWebDrivers only (Chrome/Chromium).   
 * 
 * <p>When creating CDP transactions (eg <code>jm.setCdpTransaction("duffTxnName", 59)</code>, you could in theory use the same transaction name as one
 * of your standard transactions (<code>jm.setTransaction("duffTxnName", 300)</code>, unless the transactions are running concurrently. Although the
 * framework can handle this, it could cause confusion and we suggest against it.   
 * 
 * @author Philip Webb
 * Written: Australian Winter 2021
 */
public class DevToolsDSL  {

	private static final Logger LOG = LogManager.getLogger(DevToolsDSL.class);	

	private DevTools devTools;
	private final Map<String, RequestWillBeSent> cdpRequests = new ConcurrentHashMap<>();
	

	/**
	 * For Chromium drivers only (Chrome/Chromium). 
	 * @param driver (ChromiumDriver)
	 * @return devTools
	 */
	public DevTools createDevToolsSession(WebDriver driver) {
	    devTools = ((ChromiumDriver) driver).getDevTools();     
	    devTools.createSession();
	    devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
	    return devTools;
	}
	
	
	public void addListenerRequestWillBeSent(JmeterFunctionsForSeleniumScripts jm, Predicate<RequestWillBeSent> requestFilter ){
	 
	    devTools.addListener(Network.requestWillBeSent(),
	    		request -> {  
					if (LOG.isDebugEnabled()) {
						LOG.debug("addListenerRequestWillBeSent\t Request (id) URL : (" + request.getRequestId() + ") " + request.getRequest().getUrl() 
							+ " (" + request.getRequest().getMethod() + ") "+ request.getTimestamp() + ",  MostRecentTransactionStarted : "	+ jm.getMostRecentTransactionStarted());
					}
//					System.out.println("addListenerRequestWillBeSent\t Request (id) URL : (" + request.getRequestId() + ") " + request.getRequest().getUrl() 
//							+ " (" + request.getRequest().getMethod() + ") "+ request.getTimestamp() + ",  MostRecentTransactionStarted : "	+ jm.getMostRecentTransactionStarted());
			
	    			if (requestFilter.test(request)){
	    				cdpRequests.put(request.getRequestId().toString(), request);
//	    				System.out.println("\t\t\t\t Request (id) URL : (" + request.getRequestId() + ") " + request.getRequest().getUrl() + " accepted");
	    				if (LOG.isDebugEnabled()){ 
	    					LOG.debug("\t\t\t\t Request (id) URL : (" + request.getRequestId() + ") " + request.getRequest().getUrl() + " accepted");
	    				}
	    			} 
	    		});
	}
	
	
	public void addListenerResponseReceived(JmeterFunctionsForSeleniumScripts jm, BiFunction<RequestWillBeSent,ResponseReceived, String> computeTxnId ) {
		addListenerResponseReceived(jm, res -> true, computeTxnId );
	}
		
	
	public void addListenerResponseReceived(JmeterFunctionsForSeleniumScripts jm
			, Predicate<ResponseReceived> responseFilter
			, BiFunction<RequestWillBeSent,ResponseReceived, String> computeTxnId ) {
		 
	    devTools.addListener(Network.responseReceived(),
	            response -> {  
					if (LOG.isDebugEnabled()) {LOG.debug("addListenerResponseReceived\t Request (id) URL : (" + response.getRequestId() + ") " + response.getResponse().getUrl() 
							+ " (" +response.getResponse().getStatus()+":" + response.getType().toJson() + "), ts = " + response.getTimestamp());
					}
//					System.out.println("addListenerResponseReceived\t Request (id) URL : (" + response.getRequestId() + ") " + response.getResponse().getUrl() 
//							+ " (" +response.getResponse().getStatus()+":" + response.getType().toJson() + "), ts = " + response.getTimestamp());
					
					
					RequestWillBeSent request = cdpRequests.get(response.getRequestId().toString());
					
					if (request != null && responseFilter.test(response)
							&& NumberUtils.isCreatable(request.getTimestamp().toString())
							&& NumberUtils.isCreatable(response.getTimestamp().toString())) {
						
						double mapTimestampDiff = Double.parseDouble(response.getTimestamp().toString()) - Double.parseDouble(request.getTimestamp().toString());
						
						jm.setCdpTransaction(computeTxnId.apply(request, response) , Double.valueOf(mapTimestampDiff * 1000L).longValue() );	                		
					
						if (LOG.isDebugEnabled()){LOG.debug("\t\t\t\t Request (id) URL : (" + request.getRequestId() + ") response accepted, "
								+ ", txnId = " + computeTxnId.apply(request, response) + ", tsDiff = " + mapTimestampDiff );
						}
//						System.out.println("\t\t\t\t Request (id) URL : (" + request.getRequestId() + ") response accepted, "
//								+ ", txnId = " + computeTxnId.apply(request, response) + ", tsDiff = " + mapTimestampDiff );
					} else {
						if (LOG.isDebugEnabled()) {
							LOG.debug("No txn for ReqId : (" + response.getRequestId() + ") " + response.getResponse().getUrl() + " has not been found on prev mapped requests");
							LOG.debug("                   OR response filter excluded this response OR NaN for a timestamp"  );
						}
//						System.out.println("No txn for ReqId : (" + response.getRequestId() + ") " + response.getResponse().getUrl() + " has not been found on prev mapped requests");
//						System.out.println("                   OR response filter excluded this response OR NaN for a timestamp"  );					
					}
	            });
	}
	

	
	
	public void addListenerLoadingFinished(JmeterFunctionsForSeleniumScripts jm, Predicate<LoadingFinished> responseFilter ) {
		 
	    devTools.addListener(Network.loadingFinished(),
	            loadingFinished -> {  
					if (LOG.isDebugEnabled()) {LOG.debug("addListenerLoadingFinished Request (id) : (" + loadingFinished.getRequestId() + ") ts = " + loadingFinished.getTimestamp());}
					
					RequestWillBeSent request = cdpRequests.get(loadingFinished.getRequestId() .toString());
					
					if (request != null && responseFilter.test(loadingFinished)
							&& NumberUtils.isCreatable(request.getTimestamp().toString())
							&& NumberUtils.isCreatable(loadingFinished.getTimestamp().toString())) {
						
						String  responseBody = devTools.send(Network.getResponseBody(loadingFinished.getRequestId())).getBody();
						System.out.println(loadingFinished.getRequestId() + "   body >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " );
						System.out.println(responseBody)  ;
						System.out.println(loadingFinished.getRequestId() + "   body <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< \n\n" );
						//whatever magic you want to do with the response body goes here 
					} else {
						if (LOG.isDebugEnabled()) {LOG.debug("No txn for ReqId : (" + loadingFinished.getRequestId() + ") has not been found on prev mapped requests");}
					}
	            });
	}
	
	
	
	
	
	
	public DevTools getDevTools() {
		return devTools;
	}

}
