package com.mark59.datahunter.api.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
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
import com.mark59.datahunter.api.model.PolicySelectionFilter;
import com.mark59.datahunter.api.rest.samples.DataHunterRestApiClientSampleUsage;

/**
 *  A DataHunter REST Service Client.
 *  <p>Invokes the DataHunter REST Service.  Please refer to the <code>DataHunterRestController</code> class in the mark59-datahunter project.
 *  <p>In order to execute this program, the target jar file for the mark59-datahunter-api project needs to be
 *  on the class path.  Eg: for a script running in a JMeter instance mark59-datahunter-api.jar should be placed in JMeter's lib/ext directory.
 *  <p>A few general observations about the DataHunter REST Service:
 *  <ul>
 *  <li>The operations performed by the REST service align as closely as possible to the actions that are available using the DataHunter web
 *  application. In fact apart from the odd extra validation required on the REST service, the behavior of the REST service should be the same as
 *  the corresponding DataHunter web application pages.
 *  <li>Where the REST operation does not require the <code>policies</code> list in the response to be populated with a result, it is populated with a single
 *  policy entry in the first position of the policies list.
 *
 *  <li>For detailed examples of the usage of this client, see {@link com.mark59.datahunter.api.rest.samples.DataHunterRestApiClientSampleUsage}
 *  </ul>
 *
 * @see com.mark59.datahunter.api.rest.samples.DataHunterRestApiClientSampleUsage
 * @see DataHunterRestApiResponsePojo
 *
 * @author Philip Webb
 * Written: Australian Autumn 2022
 */
public class DataHunterRestApiClient {
	private static final Logger LOG = LogManager.getLogger(DataHunterRestApiClient.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final String dataHunterUrl;


	/**
	 * @param dataHunterUrl target datahunter url (eg http://localhost:8081/mark59-datahunter)
	 */
	public DataHunterRestApiClient(String dataHunterUrl) {
		this.dataHunterUrl = dataHunterUrl;
	}


	/**
	 * Add an Item to DataHunter
	 *
	 * @param policies :
	 * 	  <br>&nbsp;  The Item key is application|identifier|lifecycle, and must be unique
	 * 	  <br>&nbsp;  useability : One of {@link DataHunterConstants#USEABILITY_LIST}
	 *    <br>&nbsp;  epochtime : for epochtime the system current time is used a numeric value is not passed
	 * @return DataHunterRestApiResponsePojo
	 */
	public DataHunterRestApiResponsePojo addPolicy(Policies policies) {
		String webServiceUrl = new UrlBuilder("/api/addPolicy")
				.addParam("application", policies.getApplication())
				.addParam("identifier", policies.getIdentifier())
				.addParam("lifecycle", policies.getLifecycle())
				.addParam("useability", policies.getUseability())
				.addParam("otherdata", policies.getOtherdata())
				.addParam("epochtime", policies.getEpochtime())
				.build();
		return invokeDataHunterRestApi(webServiceUrl);
	}


	/**
	 * Count Items matching the selection criteria
	 *
	 * @param application application
	 * @param lifecycle   blank to select all lifecycle values matching the other criteria
	 * @param useability  blank to select all useability values matching the other criteria,
	 *                    otherwise one of {@link DataHunterConstants#USEABILITY_LIST}
	 * @return DataHunterRestApiResponsePojo indicates the count of policies satisfying selection criteria
	 */
	public DataHunterRestApiResponsePojo countPolicies(String application, String lifecycle, String useability){
		String webServiceUrl = new UrlBuilder("/api/countPolicies")
				.addParam("application", application)
				.addParam("lifecycle", lifecycle)
				.addParam("useability", useability)
				.build();
		return invokeDataHunterRestApi(webServiceUrl);
	}


	/**
	 * Breakdown of counts by application, lifecycle and useability for Items matching the selection criteria.
	 * <br>The breakdown will appear in the <b>countPoliciesBreakdown</b> element of the response
	 *
	 * @param applicationStartsWithOrEquals  must be "EQUALS" or "STARTS_WITH" (applied to application selection)
	 * @param application  application, or partial application id if using a applicationStartsWithOrEquals param of "STARTS_WITH"
	 * @param lifecycle    blank to select all lifecycle values matching the other criteria
	 * @param useability   {@link DataHunterConstants#USEABILITY_LIST} blank to select all useability values matching the other criteria
	 * @return DataHunterRestApiResponsePojo containing breakdown counts by application, lifecycle and useability for Items matching the selection criteria.
	 * The breakdown will appear in the <b>countPoliciesBreakdown</b> element of the response, and the <b>policies</b> element of the response
	 * is used to indicate the selection request (note: just to provide visibility of all request params in the response,
	 * the OtherData fields is populated with the applicationStartsWithOrEquals param)
	 */
	public DataHunterRestApiResponsePojo policiesBreakdown(String applicationStartsWithOrEquals, String application, String lifecycle, String useability){
		String webServiceUrl = new UrlBuilder("/api/policiesBreakdown")
				.addParam("applicationStartsWithOrEquals", applicationStartsWithOrEquals)
				.addParam("application", application)
				.addParam("lifecycle", lifecycle)
				.addParam("useability", useability)
				.build();
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
	 * Retrieve the Item matching the application/id/lifecycle key
	 *
	 * @param application application
	 * @param identifier identifier
	 * @param lifecycle lifecycle (optional)
	 * @return if exists an item with matching application,identifier and lifecycle
	 * @see #printPolicy(String, String)
	 */
	public DataHunterRestApiResponsePojo printPolicy(String application, String identifier, String lifecycle){
		String webServiceUrl = new UrlBuilder("/api/printPolicy")
				.addParam("application", application)
				.addParam("identifier", identifier)
				.addParamIfNotEmpty("lifecycle", lifecycle)
				.build();
		return invokeDataHunterRestApi(webServiceUrl);
	}


	/**
	 * Retrieve selected Items (basic selection)
	 *
	 * <p>Note the maximum number of items that will be returned is 100 (the default LIMIT).
	 *
	 * @param application application
	 * @param lifecycle leave blank to select all lifecycle values matching the other criteria
	 * @param useability {@link DataHunterConstants#USEABILITY_LIST}, leave blank to select all useability values matching the other criteria
	 * @return DataHunterRestApiResponsePojo list of items matching the above criteria (limited to 100 rows, natural key order)
	 * @see #printSelectedPolicies(PolicySelectionFilter)
	 */
	public DataHunterRestApiResponsePojo printSelectedPolicies(String application, String lifecycle, String useability){
		String webServiceUrl = new UrlBuilder("/api/printSelectedPolicies")
				.addParam("application", application)
				.addParam("lifecycle", lifecycle)
				.addParam("useability", useability)
				.build();
		return invokeDataHunterRestApi(webServiceUrl);
	}

	/**
	 * Retrieve selected Items (with optional filters)
	 *
	 * <p>To get a feel of Policy Selection Filter usage, refer to the Datahunter UI page
	 * ../mark59-datahunter/print_selected_policies, and the Mark59 User Guide ('DataHunter' chapter)
	 *
	 * <p>The filters have the same format as per the UI :
	 * <br> application  	application (required)
	 * <br> lifecycle    	blank to select all lifecycles matching the other criteria
	 * <br> useability   	{@link DataHunterConstants#USEABILITY_LIST},or blank to select all useabilities matching the other criteria
	 * <br> identifierLikeSelected true|false to filter on identifier
	 * <br> identifierLike  identifier filter - SQL 'LIke' format is used eg %id6%
	 * <br> identifierListSelected true|false to filter on identifier 'IN'
	 * <br> identifierList identifier filter - comma separated list of identifiers to be used in a SQL 'IN' format
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
	 * <br> selectOrder		field used to ORDER the list by {@link DataHunterConstants#FILTERED_SELECT_ORDER_LIST} default is natural key order
	 * <br> orderDirection  ASCENDING (default) | DESCENDING  {@link DataHunterConstants#ORDER_DIRECTION_LIST}
	 * <br> limit			0 to 1000  (default 100, if gt 1000 will be set to 1000)
 	 *
	 * @param policySelectionFilter selection criteria for Item selection
	 * @return DataHunterRestApiResponsePojo list of items matching the above criteria
	 * @see #printSelectedPolicies(String, String, String)
	 */
	public DataHunterRestApiResponsePojo printSelectedPolicies(PolicySelectionFilter policySelectionFilter){
		String webServiceUrl = new UrlBuilder("/api/printSelectedPolicies")
				.addParam("application", policySelectionFilter.getApplication())
				.addParam("lifecycle", policySelectionFilter.getLifecycle())
				.addParam("useability", policySelectionFilter.getUseability())
				.addParam("identifierLikeSelected", policySelectionFilter.isIdentifierLikeSelected())
				.addParam("identifierLike", policySelectionFilter.getIdentifierLike())
				.addParam("identifierListSelected", policySelectionFilter.isIdentifierListSelected())
				.addParam("identifierList", policySelectionFilter.getIdentifierList())
				.addParam("otherdataSelected", policySelectionFilter.isOtherdataSelected())
				.addParam("otherdata", policySelectionFilter.getOtherdata())
				.addParam("createdSelected", policySelectionFilter.isCreatedSelected())
				.addParam("createdFrom", policySelectionFilter.getCreatedFrom())
				.addParam("createdTo", policySelectionFilter.getCreatedTo())
				.addParam("updatedSelected", policySelectionFilter.isUpdatedSelected())
				.addParam("updatedFrom", policySelectionFilter.getUpdatedFrom())
				.addParam("updatedTo", policySelectionFilter.getUpdatedTo())
				.addParam("epochtimeSelected", policySelectionFilter.isEpochtimeSelected())
				.addParam("epochtimeFrom", policySelectionFilter.getEpochtimeFrom())
				.addParam("epochtimeTo", policySelectionFilter.getEpochtimeTo())
				.addParam("selectOrder", policySelectionFilter.getSelectOrder())
				.addParam("orderDirection", policySelectionFilter.getOrderDirection())
				.addParam("limit", policySelectionFilter.getLimit())
				.build();

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
		String webServiceUrl = new UrlBuilder("/api/deletePolicy")
				.addParam("application", application)
				.addParam("identifier", identifier)
				.addParam("lifecycle", lifecycle)
				.build();
		return invokeDataHunterRestApi(webServiceUrl);
	}


	/**
	 * Delete multiple Policies (basic selection)
	 *
	 * @param application application
	 * @param lifecycle leave blank to delete all lifecycle values matching the other criteria
	 * @param useability {@link DataHunterConstants#USEABILITY_LIST}, useability leave blank to delete all useability values matching the other criteria
	 * @return DataHunterRestApiResponsePojo will indicate how many policies were deleted
	 * @see #deleteMultiplePolicies(PolicySelectionFilter)
	 */
	public DataHunterRestApiResponsePojo deleteMultiplePolicies(String application, String lifecycle, String useability){
		String webServiceUrl = new UrlBuilder("/api/deleteMultiplePolicies")
				.addParam("application", application)
				.addParam("lifecycle", lifecycle)
				.addParam("useability", useability)
				.build();
		return invokeDataHunterRestApi(webServiceUrl);
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
	 * <br> identifierLikeSelected true|false to filter on identifier
	 * <br> identifierLike  identifier filter - SQL 'LIke' format is used eg %id6%  %
	 * <br> identifierListSelected true|false to filter on identifier 'IN'
	 * <br> identifierList identifier filter - comma separated list of identifiers to be used in a SQL 'IN' format
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
	 * @return DataHunterRestApiResponsePojo will indicate how many policies were deleted
	 * @see #deleteMultiplePolicies(String, String, String)
	 */
	public DataHunterRestApiResponsePojo deleteMultiplePolicies(PolicySelectionFilter policySelectionFilter){
		String webServiceUrl = new UrlBuilder("/api/deleteMultiplePolicies")
				.addParam("application", policySelectionFilter.getApplication())
				.addParam("lifecycle", policySelectionFilter.getLifecycle())
				.addParam("useability", policySelectionFilter.getUseability())
				.addParam("identifierLikeSelected", policySelectionFilter.isIdentifierLikeSelected())
				.addParam("identifierLike", policySelectionFilter.getIdentifierLike())
				.addParam("identifierListSelected", policySelectionFilter.isIdentifierListSelected())
				.addParam("identifierList", policySelectionFilter.getIdentifierList())
				.addParam("otherdataSelected", policySelectionFilter.isOtherdataSelected())
				.addParam("otherdata", policySelectionFilter.getOtherdata())
				.addParam("createdSelected", policySelectionFilter.isCreatedSelected())
				.addParam("createdFrom", policySelectionFilter.getCreatedFrom())
				.addParam("createdTo", policySelectionFilter.getCreatedTo())
				.addParam("updatedSelected", policySelectionFilter.isUpdatedSelected())
				.addParam("updatedFrom", policySelectionFilter.getUpdatedFrom())
				.addParam("updatedTo", policySelectionFilter.getUpdatedTo())
				.addParam("epochtimeSelected", policySelectionFilter.isEpochtimeSelected())
				.addParam("epochtimeFrom", policySelectionFilter.getEpochtimeFrom())
				.addParam("epochtimeTo", policySelectionFilter.getEpochtimeTo())
				.build();
		return invokeDataHunterRestApi(webServiceUrl);
	}


	/**
	 * Reindex Reusable Indexed Policies
	 *
	 * @param application application
	 * @param lifecycle blank for a blank lifecycle (not all lifecycles within the application)
	 * @return DataHunterRestApiResponsePojo indicates the success or otherwise on the operation
	 */
	public DataHunterRestApiResponsePojo reindexReusableIndexedPolicies(String application, String lifecycle){
		String webServiceUrl = new UrlBuilder("/api/reindexReusableIndexedPolicies")
				.addParam("application", application)
				.addParam("lifecycle", lifecycle)
				.build();
		return invokeDataHunterRestApi(webServiceUrl);
	}


	/**
	 * Update an existing Item
	 *
	 * @param policies an existing to be  updated:
	 * 		<br>&nbsp;  The Item key is <i>application|identifier|lifecycle</i>.  No action if the item does not exist
	 * 		<br>&nbsp;  <i>useability</i> : One of {@link DataHunterConstants#USEABILITY_LIST}
	 * 		<br>&nbsp;  <i>otherdata</i> : otherdata (set empty if null passed)
	 * 		<br>&nbsp;  <i>epochtime</i> : a long value, or if blank or non-numeric will to set to System.currentTimeMillis().
	 * @return DataHunterRestApiResponsePojo
	 */
	public DataHunterRestApiResponsePojo updatePolicy(Policies policies) {
		String webServiceUrl = new UrlBuilder("/api/updatePolicy")
				.addParam("application", policies.getApplication())
				.addParam("identifier", policies.getIdentifier())
				.addParam("lifecycle", policies.getLifecycle())
				.addParam("useability", policies.getUseability())
				.addParam("otherdata", policies.getOtherdata())
				.addParam("epochtime", policies.getEpochtime())
				.build();
		return invokeDataHunterRestApi(webServiceUrl);
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
	 * @return DataHunterRestApiResponsePojo if fetched, next Item will be the only the element in the policies list
	 */
	public DataHunterRestApiResponsePojo lookupNextPolicy(String application, String lifecycle,String useability, String selectOrder ){
		String webServiceUrl = new UrlBuilder("/api/lookupNextPolicy")
				.addParam("application", application)
				.addParam("lifecycle", lifecycle)
				.addParam("useability", useability)
				.addParam("selectOrder", selectOrder)
				.build();
		return invokeDataHunterRestApi(webServiceUrl);
	}


	/**
	 * Use Next Policy
	 *
	 * <br>Updates the 'next' Item (determined by the selection criteria) to a useability of 'USED'
	 *
	 * @param application application
	 * @param lifecycle   blank to include all lifecycle values matching the other criteria
	 * @param useability  {@link DataHunterConstants#USEABILITY_LIST}
	 * @param selectOrder {@link DataHunterConstants#GET_NEXT_POLICY_SELECTOR}
	 * @return DataHunterRestApiResponsePojo if fetched, next Item will be the only the element in the policies list
	 */
	public DataHunterRestApiResponsePojo useNextPolicy(String application, String lifecycle,String useability, String selectOrder ){
		String webServiceUrl = new UrlBuilder("/api/useNextPolicy")
				.addParam("application", application)
				.addParam("lifecycle", lifecycle)
				.addParam("useability", useability)
				.addParam("selectOrder", selectOrder)
				.build();
		return invokeDataHunterRestApi(webServiceUrl);
	}


	/**
	 * Change the Use State for an Item, or for multiple Items in an Application (no lifecycle selection)
	 * <p>Included for backwards compatibility only (equivalent of passing an empty lifecycle value in
	 * {@link #updatePoliciesUseState(String, String, String, String, String, String)})
	 *
	 * @param application  application
	 * @param identifier   blank to select all identifier values matching the other criteria (application, useability)
	 * @param useability   blank to select all useability values matching the other criteria, otherwise
	 *                     {@link DataHunterConstants#USEABILITY_LIST}
	 * @param toUseability One of {@link DataHunterConstants#USEABILITY_LIST}.
	 * @param toEpochTime  is only updated if passed a numeric value. Eg contains the System.currentTimeMillis() or another integer
	 *
	 * @return DataHunterRestApiResponsePojo indicates number of rows updated
	 * @deprecated Use {@link #updatePoliciesUseState(String, String, String, String, String, String)} with empty lifecycle instead.
	 * This method will be removed in a future version.
	 */
	@Deprecated
	public DataHunterRestApiResponsePojo updatePoliciesUseState(String application, String identifier, String useability,
			String toUseability, String toEpochTime){
		String webServiceUrl = new UrlBuilder("/api/updatePoliciesUseState")
				.addParam("application", application)
				.addParam("identifier", identifier)
				.addParam("useability", useability)
				.addParam("toUseability", toUseability)
				.addParam("toEpochTime", toEpochTime)
				.build();
		return invokeDataHunterRestApi(webServiceUrl);
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
	 * @return DataHunterRestApiResponsePojo indicates number of rows updated
	 */
	public DataHunterRestApiResponsePojo updatePoliciesUseState(String application, String identifier, String lifecycle, String useability,
			String toUseability, String toEpochTime){
		String webServiceUrl = new UrlBuilder("/api/updatePoliciesUseState")
				.addParam("application", application)
				.addParam("identifier", identifier)
				.addParam("lifecycle", lifecycle)
				.addParam("useability", useability)
				.addParam("toUseability", toUseability)
				.addParam("toEpochTime", toEpochTime)
				.build();
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
	 * @param application  application (or application that start with this value)
	 * @param identifier   blank to select all identifier values matching the other criteria
	 * @param useability   blank to select all useability values matching the other criteria, otherwise {@link DataHunterConstants#USEABILITY_LIST}
	 * @param toUseability blank to not update useability, otherwise one of {@link DataHunterConstants#USEABILITY_LIST}.
	 * @return  DataHunterRestApiResponsePojo  The getAsyncMessageAnalyzerResults list in the response provides the results, including the max time difference
	 *  between each set of matched rows.
	 */
	public DataHunterRestApiResponsePojo asyncMessageAnalyzer(String applicationStartsWithOrEquals, String application, String identifier,
			String useability, String toUseability){
		String webServiceUrl = new UrlBuilder("/api/asyncMessageAnalyzer")
				.addParam("applicationStartsWithOrEquals", applicationStartsWithOrEquals)
				.addParam("application", application)
				.addParam("identifier", identifier)
				.addParam("useability", useability)
				.addParam("toUseability", toUseability)
				.build();
		return invokeDataHunterRestApi(webServiceUrl);
	}


	/**
	 * Call to the DataHunter Rest controller, returning a DataHunterRestApiResponsePojo
	 *
	 * @param webServiceUrl DataHunter Rest controller URL
	 * @return DataHunterRestApiResponsePojo
	 */
	private DataHunterRestApiResponsePojo invokeDataHunterRestApi(String webServiceUrl)  {
    HttpURLConnection con = null;
    DataHunterRestApiResponsePojo responsePojo = new DataHunterRestApiResponsePojo();
    Integer responseCode = null;

    try {
        URL url = new URL(webServiceUrl);
        con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        responseCode = con.getResponseCode();

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            StringBuilder jsonResponseStr = new StringBuilder();
            String respLine;
            while ((respLine = in.readLine()) != null) {
                jsonResponseStr.append(respLine);
            }
            responsePojo = OBJECT_MAPPER.readValue(jsonResponseStr.toString(), DataHunterRestApiResponsePojo.class);

            if (responsePojo == null) {
                throw new RuntimeException("Error : Unexpected null Response returned from the DataHunter Api!");
            }
        }

    } catch (Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        String errorMsg = "Error: Failure calling the DataHunter Rest API at " + webServiceUrl
                + " message : \n" + e.getMessage() + "\n" + stackTrace.toString();
        LOG.error(errorMsg);
        LOG.debug("        last response-code from DataHunter Rest API was " + responseCode);
        LOG.debug("        last response from DataHunter Rest API was \n" + responsePojo);

        // Populate response object with error details
        responsePojo.setSuccess("false");
        responsePojo.setRowsAffected(0);
        responsePojo.setFailMsg(errorMsg);

	} finally {
        if (con != null) {
            con.disconnect();
        }
    }
    return responsePojo;
}

	/**
	 * @param uriParm  Parameter to encode
	 * @return encoded parameter
	 */
	private String encode(String uriParm) {
		return URLEncoder.encode(nullToEmpty(uriParm), StandardCharsets.UTF_8);
	}


	private String nullToEmpty(String str) {
		return null == str ? "" : str;
	}

	/**
	 * Helper class to build URLs with query parameters in a clean and efficient way.
	 * A relatively generic builder - makes some assumptions such as the first param value,
	 * and a optional empty param value are strings.
	 */
	private class UrlBuilder {
		private final StringBuilder url;
		private boolean firstParam = true;

		public UrlBuilder(String endpoint) {
			this.url = new StringBuilder(dataHunterUrl).append(endpoint).append("?");
		}

		public UrlBuilder addParam(String name, String value) {
			if (!firstParam) {
				url.append("&");
			}
			url.append(name).append("=").append(encode(value));
			firstParam = false;
			return this;
		}

		public UrlBuilder addParam(String name, Long value) {
			return addParam(name, String.valueOf(value));
		}

		public UrlBuilder addParam(String name, Boolean value) {
			return addParam(name, String.valueOf(value));
		}

		public UrlBuilder addParamIfNotEmpty(String name, String value) {
			if (!DataHunterUtils.isEmpty(value)) {
				addParam(name, value);
			}
			return this;
		}

		public String build() {
			return url.toString();
		}
	}

}
