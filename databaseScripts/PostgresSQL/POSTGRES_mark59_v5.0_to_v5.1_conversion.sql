
-- *************************************************************************************
-- **
-- **   from 5.0 to 5.1   
-- **
-- **   THIS IS AN OPTIONAL CHANGE  
-- **   --------------------------
-- **   Loads an improved version of the New Relic Sample script into mark59metricsdb 
-- **
-- *************************************************************************************

-- *************************************************************************************
--  Row updates for the mark59metricsdb  tables
-- *************************************************************************************

DELETE FROM SERVERPROFILES WHERE SERVER_PROFILE_NAME = 'NewRelicTestProfile';
INSERT INTO SERVERPROFILES VALUES ('NewRelicSampleProfile', 'GROOVY_SCRIPT', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'supplied sample New Relic API groovy script', '{"proxyPort":"proxyPort","newRelicXapiKey":"newRelicXapiKey","proxyServer":"proxyServer","newRelicApiAppId":"newRelicApiAppId"}');

DELETE FROM SERVERCOMMANDLINKS WHERE SERVER_PROFILE_NAME = 'NewRelicTestProfile';
INSERT INTO SERVERCOMMANDLINKS VALUES ('NewRelicSampleProfile','NewRelicSampleCmd');

DELETE FROM COMMANDS WHERE COMMAND_NAME = 'NewRelicSampleCmd';
INSERT INTO COMMANDS VALUES ('NewRelicSampleCmd', 'GROOVY_SCRIPT', 'import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import com.mark59.metrics.pojos.ParsedMetric;
import com.mark59.metrics.pojos.ScriptResponse;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

String newRelicApiUrl = "https://api.newrelic.com/v2/applications/";
String url = newRelicApiUrl + newRelicApiAppId + "/hosts.json";
ScriptResponse scriptResponse = new ScriptResponse();
List<ParsedMetric> parsedMetrics = new ArrayList<ParsedMetric>();

Request request; Response response = null; JSONObject jsonResponse = null;
Proxy proxy = StringUtils.isNotBlank(proxyServer + proxyPort) ? new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyServer , new Integer(proxyPort))) : null;
OkHttpClient client = proxy != null ? new OkHttpClient.Builder().proxy(proxy).build() : new OkHttpClient();
Headers headers = new Headers.Builder().add("X-Api-Key", newRelicXapiKey).add("Content-Type", "application/json").build();
String debugJsonResponses =  "running profile " + serverProfile.serverProfileName + ", init req : " + url ;

try {
	request = new Request.Builder().url(url).headers(headers).get().build();
	response = client.newCall(request).execute();
	jsonResponse = new JSONObject(response.body().string());
	debugJsonResponses =  debugJsonResponses + "<br>init res.: " + jsonResponse.toString();

	ZonedDateTime utcTimeNow = ZonedDateTime.now(ZoneOffset.UTC);
	String toHour 	= String.format("%02d", utcTimeNow.getHour());
	String toMinute	= String.format("%02d", utcTimeNow.getMinute());
	ZonedDateTime utcMinus1Min = utcTimeNow.minusMinutes(1);
	String fromHour	= String.format("%02d", utcMinus1Min.getHour());
	String fromMinute = String.format("%02d", utcMinus1Min.getMinute());
	String fromDate = utcMinus1Min.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	String toDate 	= utcTimeNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	String urlDateRangeParmStr = "&from=" + fromDate + "T" + fromHour + "%3A" + fromMinute + "%3A00%2B00%3A00" + "&to=" + toDate + "T" + toHour + "%3A" + toMinute + "%3A00%2B00%3A00";

	JSONArray application_hosts = jsonResponse.getJSONArray("application_hosts");

	for (int i = 0; i < application_hosts.length(); i++) {
		JSONObject application_host = (JSONObject) application_hosts.get(i);
		Integer hostId = (Integer) application_host.get("id");
		String hostName = ((String)application_host.get("host")).replace(":","_");
		url = newRelicApiUrl + newRelicApiAppId  + "/hosts/" + hostId + "/metrics/data.json?names%5B%5D=Memory/Heap/Used&names%5B%5D=CPU/User Time&names%5B%5D=Memory/Physical" + urlDateRangeParmStr;
		debugJsonResponses =  debugJsonResponses + "<br><br>req." + i + ": " + url ;

		request = new Request.Builder().url(url).headers(headers).get().build();
		response = client.newCall(request).execute();
		jsonResponse = new JSONObject(response.body().string());
		debugJsonResponses =  debugJsonResponses + "<br>res." + i + ": " + jsonResponse.toString();

		Number memoryMetric = -1.0;
		memoryMetric =  (Number)((JSONObject)((JSONObject)jsonResponse.getJSONObject("metric_data").getJSONArray("metrics").get(0)).getJSONArray("timeslices").get(0)).getJSONObject("values").get("used_mb_by_host") ;
		parsedMetrics.add(new ParsedMetric("MEMORY_HEAP_USED_MB_" + hostName, memoryMetric, "MEMORY"));

		Number cpuMetric = -1.0;
		cpuMetric = (Number)((JSONObject)((JSONObject)jsonResponse.getJSONObject("metric_data").getJSONArray("metrics").get(1)).getJSONArray("timeslices").get(0)).getJSONObject("values").get("percent");
		parsedMetrics.add(new ParsedMetric("CPU_USER_TIME_%_" + hostName, cpuMetric, "CPU_UTIL"));
	}
} catch (Exception e) {
	debugJsonResponses =  debugJsonResponses + "<br>\n ERROR :  Exception last url: " + url + ", response of  : " + jsonResponse + ", message: "+ e.getMessage();
}
scriptResponse.setCommandLog(debugJsonResponses);
scriptResponse.setParsedMetrics(parsedMetrics);
return scriptResponse;', 'N', 'NewRelic Supplied Sample', '["newRelicApiAppId","newRelicXapiKey","proxyServer","proxyPort"]');
