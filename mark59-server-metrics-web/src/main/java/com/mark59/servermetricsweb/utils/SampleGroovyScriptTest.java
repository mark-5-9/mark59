package com.mark59.servermetricsweb.utils;

import java.util.ArrayList;
import java.util.List;

import com.mark59.servermetricsweb.data.beans.ServerProfile;
import com.mark59.servermetricsweb.pojos.ParsedMetric;
import com.mark59.servermetricsweb.pojos.ScriptResponse;



public class SampleGroovyScriptTest {


	
public static ScriptResponse runScript() {		
	ServerProfile	serverProfile = new ServerProfile();
	serverProfile.setServerProfileName("Some_Server_Profile");
	
	
ScriptResponse scriptResponse = new ScriptResponse(); 
List<ParsedMetric> parsedMetrics = new ArrayList<ParsedMetric>();
// -- parameters can be referenced as strings (parameter names will be listed below) --  
String commandLogForDebugging =  "running supplied script sample " + serverProfile.getServerProfileName();
commandLogForDebugging += "<br>" +  serverProfile.getComment();

Number aNumber = 123;
parsedMetrics.add(new ParsedMetric("a_memory_txn", aNumber, "MEMORY"));
parsedMetrics.add(new ParsedMetric("a_cpu_util_txn", 33.3,  "CPU_UTIL"));
parsedMetrics.add(new ParsedMetric("some_datapoint", 44.6,  "DATAPOINT"));
parsedMetrics.add(new ParsedMetric("set_a_failure",  66.6,  "DATAPOINT", false));

scriptResponse.setCommandLog(commandLogForDebugging);
scriptResponse.setParsedMetrics(parsedMetrics);
return scriptResponse;

}

	
	
public static void main(String[] args) {		
	
	ScriptResponse scriptResponse = SampleGroovyScriptTest.runScript();  
	
	System.out.println(">> ParsedMetrics ");
	System.out.println(scriptResponse.getParsedMetrics() );
	System.out.println("<< ParsedMetrics ");
	
	System.out.println(">> CommandLog ");
	System.out.println(scriptResponse.getCommandLog() );
	System.out.println("<< CommandLog ");
	
}
	
	
	
	
}
