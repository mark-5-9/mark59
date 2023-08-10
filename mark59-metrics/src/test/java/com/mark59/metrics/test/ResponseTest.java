package com.mark59.metrics.test;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import com.mark59.metrics.data.beans.ServerProfile;
import com.mark59.metrics.pojos.ParsedMetric;
import com.mark59.metrics.pojos.ScriptResponse;


/**
 * Tests run in build using via Maven Surefire Plugin 
 */
public class ResponseTest{	
	
	@Test
	public void testCheckScriptResponseFormatTest() {
		
		ServerProfile serverProfile = new ServerProfile();
		serverProfile.setServerProfileName("Some_Server_Profile");

		ScriptResponse scriptResponse = new ScriptResponse();
		List<ParsedMetric> parsedMetrics = new ArrayList<ParsedMetric>();
		String commandLogForDebugging = "running supplied script sample " + serverProfile.getServerProfileName();
		commandLogForDebugging += "<br>" + serverProfile.getComment();

		Number aNumber = 123;
		parsedMetrics.add(new ParsedMetric("a_memory_txn", aNumber, "MEMORY", null));
		parsedMetrics.add(new ParsedMetric("a_cpu_util_txn", 33.3, "CPU_UTIL", null));
		parsedMetrics.add(new ParsedMetric("some_datapoint", 44.6, "DATAPOINT", null));
		parsedMetrics.add(new ParsedMetric("set_a_failure", 66.6, "DATAPOINT", false, null));

		scriptResponse.setCommandLog(commandLogForDebugging);
		scriptResponse.setParsedMetrics(parsedMetrics);
		
		Assert.assertEquals("[[label=a_memory_txn, result=123, dataType=MEMORY, success=true, parseFailMsg=null], "
				+ "[label=a_cpu_util_txn, result=33.3, dataType=CPU_UTIL, success=true, parseFailMsg=null], "
				+ "[label=some_datapoint, result=44.6, dataType=DATAPOINT, success=true, parseFailMsg=null], "
				+ "[label=set_a_failure, result=66.6, dataType=DATAPOINT, success=false, parseFailMsg=null]]"
				+ "", scriptResponse.getParsedMetrics().toString() );
	}
	
}
