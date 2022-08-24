package com.mark59.datahunter.api.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mark59.datahunter.api.application.DataHunterConstants;
import com.mark59.datahunter.api.application.DataHunterUtils;
import com.mark59.datahunter.api.data.beans.Policies;
import com.mark59.datahunter.api.model.DataHunterRestApiResponsePojo;
import com.mark59.datahunter.api.rest.samples.DataHunterRestApiClientSampleUsage;

/**
 *  A DataHunter REST Service Client.
 *  <p>Invokes the DataHunter REST Service.  Please refer to the <code>DataHunterRestController</code> class in the mark59 DataHunter project.
 *  <p>In order to execute this program, the target jar file for the mark59-datahunter-api project needs to be 
 *  on the class path.  Eg: for a script running in a JMeter instance mark59-datahunter-api.jar should be placed in JMeter's lib/ext directory.        
 *  <p>A few general observations about the DataHunter REST Service:
 *  <ul>
 *  <li>The operations performed by the REST service align as closely as possible to the actions that are available using the DataHunter web 
 *  application. In fact apart from the odd extra validation required on the REST service, the behavior of the REST service should be the same as 
 *  the corresponding DataHunter web application pages. 
 *  <li>Where the REST operation does not require the 'policies' list in the response (see {@link DataHunterRestApiResponsePojo} ) to be populated
 *   with a result, it is populated with a single policy entry which aligns as much as possible with any selection criteria passed from the request. 
 *  (just informational to assist with debugging)
 *  <li>For detailed examples of the usage of this client, see {@link com.mark59.datahunter.api.rest.samples.DataHunterRestApiClientSampleUsage}  
 *  </ul>  
 * 
 * @see com.mark59.datahunter.api.rest.samples.DataHunterRestApiClientSampleUsage
 *   
 * @author Philip Webb
 * Written: Australian Autumn 2022
 */
public class DataHunterRestApiClient {
	private static final Logger LOG = LogManager.getLogger(DataHunterRestApiClient.class);	
	private static final String UTF_8 =  StandardCharsets.UTF_8.toString();
	
	String dataHunterUrl;
	
	
	/**
	 * @param dataHunterUrl target datahunter url (eg http://localhost:8081/mark59-datahunter)
	 */
	public DataHunterRestApiClient(String dataHunterUrl) {
		this.dataHunterUrl = dataHunterUrl;
	}
	

	/**
	 * Add an Item to DataHunter  
	 * <br>The Item key is application|identifier|lifecycle, and must be unique
	 * <br>usability : {@link DataHunterConstants#USEABILITY_LIST} 
	 * 
	 * @param policies policy to add (for epochtime the current time is used if is not passed a numeric value)
	 * @return DataHunterRestApiResponsePojo
	 */
	public DataHunterRestApiResponsePojo addPolicy(Policies policies) {
		String webServiceUrl = dataHunterUrl + "/api/addPolicy?"
				+ "application=" + encode(policies.getApplication())  
				+ "&identifier=" + encode(policies.getIdentifier()) 
				+ "&lifecycle="  + encode(policies.getLifecycle()) 
				+ "&useability=" + encode(policies.getUseability()) 
				+ "&otherdata="  + encode(policies.getOtherdata()) 
				+ "&epochtime="  + policies.getEpochtime();
		// System.out.println("DHRAC addPolicy url = " + webServiceUrl); 
		return invokeDataHunterRestApi(webServiceUrl);
	}

	
	/**
	 * Count Items matching the selection criteria 
	 * 
	 * @param application application
	 * @param lifecycle   blank to select all lifecycle values matching the other criteria
	 * @param useability  {@link DataHunterConstants#USEABILITY_LIST}, blank to select all useability values matching the other criteria
	 * @return DataHunterRestApiResponsePojo indicates the count of policies satisfying selection criteria
	 */
	public DataHunterRestApiResponsePojo countPolicies(String application, String lifecycle, String useability){
		String webServiceUrl = dataHunterUrl + "/api/countPolicies?" 
				+ "application=" + encode(application) + "&lifecycle=" + encode(lifecycle) + "&useability=" + encode(useability); 
		return invokeDataHunterRestApi(webServiceUrl);
	}
	
	
	/**
	 * Breakdown counts by application, lifecycle and useability for Items matching the selection criteria. 
	 * <br>The breakdown will appear in the <b>countPoliciesBreakdown</b> element of the response 
	 *  
	 * @param applicationStartsWithOrEquals  must be "EQUALS" or "STARTS_WITH" (applied to application selection)
	 * @param application  application, or partial application id if using a applicationStartsWithOrEquals param of "STARTS_WITH"
	 * @param lifecycle    blank to select all lifecycle values matching the other criteria
	 * @param useability   {@link DataHunterConstants#USEABILITY_LIST}blank to select all useability values matching the other criteria
	 * @return DataHunterRestApiResponsePojo containing breakdown counts by application, lifecycle and useability for Items matching the selection criteria.
	 * The breakdown will appear in the <b>countPoliciesBreakdown</b> element of the response, and the <b>policies</b> element of the response
	 * is used to indicate the selection request (note: just to provide visibility of all request params in the response, 
	 * the OtherData fields is populated with the applicationStartsWithOrEquals param)   
	 */
	public DataHunterRestApiResponsePojo countPoliciesBreakdown(String applicationStartsWithOrEquals, String application, String lifecycle, String useability){
		String webServiceUrl = dataHunterUrl + "/api/countPoliciesBreakdown?applicationStartsWithOrEquals=" + encode(applicationStartsWithOrEquals) + 
				"&application=" + encode(application) + "&lifecycle=" + encode(lifecycle) + "&useability=" + encode(useability); 
		return invokeDataHunterRestApi(webServiceUrl);
	}
	

	/**
	 * Retrieve the Item matching the application/id, with a blank lifecycle 
	 * 
	 * @param application application
	 * @param identifier identifier
	 * @return DataHunterRestApiResponsePojo if exists an item with matching application,identifier
	 *  and a blank lifecycle (NOT all/any items with this application,identifier)    
	 */
	public DataHunterRestApiResponsePojo printPolicy(String application, String identifier){
		return printPolicy(application, identifier, null);
	}
	
	
	/**
	 * Retrieve the Item matching the application/id/lifecycle 
	 * 
	 * @param application application
	 * @param identifier identifier
	 * @param lifecycle lifecycle
	 * @return if exists an item with matching application,identifier and lifecycle    
	 */
	public DataHunterRestApiResponsePojo printPolicy(String application, String identifier, String lifecycle){
		String webServiceUrl = dataHunterUrl + "/api/printPolicy?" 
				+ "application=" + encode(application) + "&identifier=" + encode(identifier); 
		if ( ! DataHunterUtils.isEmpty(lifecycle)){
			webServiceUrl = webServiceUrl + "&lifecycle=" + encode(lifecycle);
		}
		return invokeDataHunterRestApi(webServiceUrl);
	}
	
	
	/**
	 * Retrieve selected Items 
	 * 
	 * @param application application
	 * @param lifecycle leave blank to select all lifecycle values matching the other criteria
	 * @param useability {@link DataHunterConstants#USEABILITY_LIST}, leave blank to select all useability values matching the other criteria
	 * @return DataHunterRestApiResponsePojo list of items matching the above criteria
	 */
	public DataHunterRestApiResponsePojo printSelectedPolicies(String application, String lifecycle, String useability){
		String webServiceUrl = dataHunterUrl + "/api/printSelectedPolicies?" 
				+ "application=" + encode(application) + "&lifecycle=" + encode(lifecycle) + "&useability=" + encode(useability); 
		return invokeDataHunterRestApi(webServiceUrl);
	}
	
	
	/**
	 * Delete an Item
	 * <br>If exists delete Item with matching application,identifier,lifecycle
	 *  
	 * @param application application
	 * @param identifier  identifier
	 * @param lifecycle  lifecycle 
	 * @return DataHunterRestApiResponsePojo indicates if a Item was deleted or not (rowsAffected count) 
	 */
	public DataHunterRestApiResponsePojo deletePolicy(String application, String identifier, String lifecycle){
		String webServiceUrl = dataHunterUrl + "/api/deletePolicy?" 
				+ "application=" + encode(application) + "&identifier=" + encode(identifier) + "&lifecycle=" + encode(lifecycle) ; 
		return invokeDataHunterRestApi(webServiceUrl);
	}
	
	
	/**
	 * Delete multiple Items
	 * 
	 * @param application application
	 * @param lifecycle leave blank to delete all lifecycle values matching the other criteria
	 * @param useability {@link DataHunterConstants#USEABILITY_LIST}, useability leave blank to delete all useability values matching the other criteria
	 * @return DataHunterRestApiResponsePojo will indicate how many policies were deleted
	 */
	public DataHunterRestApiResponsePojo deleteMultiplePolicies(String application, String lifecycle, String useability){
		String webServiceUrl = dataHunterUrl + "/api/deleteMultiplePolicies?application=" + encode(application) + 
				"&lifecycle=" + encode(lifecycle) + "&useability=" + encode(useability); 
		return invokeDataHunterRestApi(webServiceUrl);
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
	 * @return DataHunterRestApiResponsePojo if fetched, next Item will be the only the element in the policies list 
	 */
	public DataHunterRestApiResponsePojo useNextPolicy(String application, String lifecycle,String useability, String selectOrder ){
		String webServiceUrl = dataHunterUrl + "/api/useNextPolicy?application=" + encode(application)	+ "&lifecycle=" + encode(lifecycle) + 
				"&useability=" + encode(useability) + "&selectOrder=" + encode(selectOrder);
		return invokeDataHunterRestApi(webServiceUrl);
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
	 * @return DataHunterRestApiResponsePojo if fetched, next Item will be the only the element in the policies list  
	 */
	public DataHunterRestApiResponsePojo lookupNextPolicy(String application, String lifecycle,String useability, String selectOrder ){
		String webServiceUrl = dataHunterUrl + "/api/lookupNextPolicy?application=" + encode(application)	+ "&lifecycle=" + encode(lifecycle) + 
				"&useability=" + encode(useability) + "&selectOrder=" + encode(selectOrder);
		return invokeDataHunterRestApi(webServiceUrl);
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
	 * @return DataHunterRestApiResponsePojo indicates number of rows updated
	 */	
	public DataHunterRestApiResponsePojo updatePoliciesUseState(String application, String identifier, String useability, 
			String toUseability, String toEpochTime){
		String webServiceUrl = dataHunterUrl + "/api/updatePoliciesUseState?application=" + encode(application)	+ "&identifier=" + encode(identifier) + 
				"&useability=" + encode(useability) + "&toUseability=" + encode(toUseability) + "&toEpochTime=" + encode(toEpochTime);
		return invokeDataHunterRestApi(webServiceUrl);
	}
	
	
	/**
	 * Asynchronous Message Analyzer
	 * 
	 * <p>Provides a timing calculation between a set of Items that match the input criteria.  It is designed to assist with timing 
	 * asynchronous events during a performance test.  For further details please refer to the Mark59 user guide, and sample 
	 * usages in DataHunterRestApiClientSampleUsage.
	 * 
	 * @see DataHunterRestApiClientSampleUsage#asyncLifeCycleTestWithUseabilityUpdate(DataHunterRestApiClient)
	 * @see DataHunterRestApiClientSampleUsage#workingWithAsyncMessages(DataHunterRestApiClient)
	 *    
	 * @param applicationStartsWithOrEquals  must be "EQUALS" or "STARTS_WITH" (applied to application selection)
	 * @param application application
	 * @param identifier identifier leave blank to select all identifier values matching the other criteria (application, useability)
	 * @param useability {@link DataHunterConstants#USEABILITY_LIST} leave blank to select all useability values matching the other criteria
	 *  (the parameters above).  Note that the value 'UNPAIRED' was specifically created for use in this function (but you have the option to use other values).  
	 * @param toUseability {@link DataHunterConstants#USEABILITY_LIST}. 'Matched' Items satisfying the selection criteria (the parameters above), will have
	 *  useability changed to this value 
	 * @return  DataHunterRestApiResponsePojo  The getAsyncMessageaAnalyzerResults list in the response provides the results, including the max time difference
	 *  between each set of matched rows.  
	 */
	public DataHunterRestApiResponsePojo asyncMessageAnalyzer(String applicationStartsWithOrEquals, String application, String identifier, 
			String useability, String toUseability){
		String webServiceUrl = dataHunterUrl + "/api/asyncMessageAnalyzer?applicationStartsWithOrEquals=" + encode(applicationStartsWithOrEquals) + 
				"&application=" + encode(application) + "&identifier=" + encode(identifier) + "&useability=" + encode(useability) + 
				"&toUseability=" + encode(toUseability);
		return invokeDataHunterRestApi(webServiceUrl);
	}	
	
	
	/**
	 * Call to the DataHunter Rest controller, returning a DataHunterRestApiResponsePojo
	 * 
	 * @param webServiceUrl DataHunter Rest controller URL
	 * @return DataHunterRestApiResponsePojo
	 */
	private DataHunterRestApiResponsePojo invokeDataHunterRestApi(String webServiceUrl)  {
		BufferedReader in = null;
		DataHunterRestApiResponsePojo responsePojo = new DataHunterRestApiResponsePojo();
		Integer repsonseCode = null;
		try {
			URL url = new URL(webServiceUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			repsonseCode = con.getResponseCode();

			in = new BufferedReader( new InputStreamReader(con.getInputStream()));
			String respLine;
			StringBuilder jsonResponseStr = new StringBuilder();
			while ((respLine = in.readLine()) != null) {
				jsonResponseStr.append(respLine);
			}
			in.close();
			responsePojo = new ObjectMapper().readValue(jsonResponseStr.toString(), DataHunterRestApiResponsePojo.class);

			if ( responsePojo == null ){
				throw new RuntimeException("Error : Unexpected null Response returned from the DataHunter Api!");
			}
			
		} catch (Exception | AssertionError e) {
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			String errorMsg = "Error: Failure calling the DataHunter Rest API at " + webServiceUrl + " message : \n"+e.getMessage()+"\n"+stackTrace.toString();
			LOG.error(errorMsg);
			System.out.println(errorMsg);
			LOG.debug("        last response-code from DataHunter Rest API was " + repsonseCode);			
			LOG.debug("        last response from DataHunter Rest API was \n" + responsePojo );
			if (in != null){try {in.close();} catch (IOException ignored) {}}
		}
		return responsePojo;
	}

	
	private String encode(String uriParm) {
		try {
			return URLEncoder.encode(nullToEmpty(uriParm), UTF_8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException("UnsupportedEncodingException in policyToUrlQueryString using " + uriParm );
		}
	}
	
	
	private String nullToEmpty(String str) {
		return null == str ? "" : str;
	}
	
}
