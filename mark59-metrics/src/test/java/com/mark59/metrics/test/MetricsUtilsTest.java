package com.mark59.metrics.test;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mark59.metrics.data.beans.ServerProfile;
import com.mark59.metrics.pojos.ScriptResponse;
import com.mark59.metrics.utils.MetricsUtils;

public class MetricsUtilsTest  {

	@Test
    public void testRunGroovyScriptForcommandResponseParserLikeFreePhysicalMemory()
    {
		String commandResponse="FreePhysicalMemory"+"\n" +"20325984";
		String commandResponseParserScript="Math.round(Double.parseDouble(commandResponse.replaceAll(\"[^\\\\d.]\", \"\")) / 1000000 )";
		Object result = MetricsUtils.runGroovyScript(commandResponseParserScript, commandResponse); 
		Assert.assertEquals(Long.valueOf(20L), ((Long)result)); 
	}	

	@Test
    public void testRunGroovyScriptForcommandResponseParserLikeWinCpu()
    {
		String commandResponse="LoadPercentage\\n10\\n10\\n25";
		String commandResponseParserScript=
				"java.util.regex.Matcher m = java.util.regex.Pattern.compile(\"-?[0-9]+\").matcher(commandResponse);\r\n"
				+ "Integer sum = 0; \r\n"
				+ "int count = 0; \r\n"
				+ "while (m.find()){ \r\n"
				+ "    sum += Integer.parseInt(m.group()); \r\n"
				+ "    count++;\r\n"
				+ "}; \r\n"
				+ "if (count==0) \r\n"
				+ "    return 0 ; \r\n"
				+ "else \r\n"
				+ "    return sum/count;";
		
		Object result = MetricsUtils.runGroovyScript(commandResponseParserScript, commandResponse); 
		Assert.assertEquals(new BigDecimal(15).intValue(), ((BigDecimal)result).intValue()); 
	}	
    
	@Test   
    public void testRunGroovyScriptForGroovyCommandLikeSimpleScriptSampleRunner()
    {
		String commandString=
				"import java.util.ArrayList;\r\n"
				+ "import java.util.List;\r\n"
				+ "import com.mark59.metrics.data.beans.ServerProfile;\r\n"
				+ "import com.mark59.metrics.pojos.ParsedMetric;\r\n"
				+ "import com.mark59.metrics.pojos.ScriptResponse;\r\n"
				+ "\r\n"
				+ "ScriptResponse scriptResponse = new ScriptResponse();\r\n"
				+ "List<ParsedMetric> parsedMetrics = new ArrayList<ParsedMetric>();\r\n"
				+ "\r\n"
				+ "String commandLogDebug = \"running script \" + serverProfile.getServerProfileName() + \"<br>\" +  serverProfile.getComment();\r\n"
				+ "commandLogDebug += \"<br>passed parms : parm1=\" + parm1 + \", parm2=\" + parm2 + \", parm3=\" + parm3\r\n"
				+ "\r\n"
				+ "Number aNumber = 123;\r\n"
				+ "parsedMetrics.add(new ParsedMetric(\"a_memory_txn\", aNumber, \"MEMORY\"));\r\n"
				+ "parsedMetrics.add(new ParsedMetric(\"a_cpu_util_txn\", 33.3,  \"CPU_UTIL\"));\r\n"
				+ "parsedMetrics.add(new ParsedMetric(\"some_datapoint\", 66.6,  \"DATAPOINT\"));\r\n"
				+ "\r\n"
				+ "scriptResponse.setCommandLog(commandLogDebug);\r\n"
				+ "scriptResponse.setParsedMetrics(parsedMetrics);\r\n"
				+ "return scriptResponse;";
		
		Map<String,Object> scriptParms = new HashMap<>();
		ServerProfile sp = new ServerProfile();
		sp.setServerProfileName("SimpleScriptSampleRunnerUnitTest");
		sp.setComment("Runs some stuff");
		scriptParms.put("serverProfile", sp);
		scriptParms.put("parm1","11");
		scriptParms.put("parm2","55.7");
		scriptParms.put("parm3","333");
		
		ScriptResponse groovyScriptResult = (ScriptResponse)MetricsUtils.runGroovyScript(commandString.replaceAll("\\R", "\n"), scriptParms);
		
		Assert.assertEquals(false, groovyScriptResult.getCommandFailure());
		Assert.assertEquals(3, groovyScriptResult.getParsedMetrics().size());
		Assert.assertEquals( "[label=a_memory_txn, result=123, dataType=MEMORY, success=true, parseFailMsg=null]", 		groovyScriptResult.getParsedMetrics().get(0).toString());
		Assert.assertEquals( "[label=a_cpu_util_txn, result=33.3, dataType=CPU_UTIL, success=true, parseFailMsg=null]", groovyScriptResult.getParsedMetrics().get(1).toString());
		Assert.assertEquals( "[label=some_datapoint, result=66.6, dataType=DATAPOINT, success=true, parseFailMsg=null]",groovyScriptResult.getParsedMetrics().get(2).toString());
		Assert.assertEquals( "running script SimpleScriptSampleRunnerUnitTest<br>Runs some stuff<br>passed parms : parm1=11, parm2=55.7, parm3=333", groovyScriptResult.getCommandLog());
	}	

	
	@Test
    public void testSerialization()
    {
		Map<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		map.put("key3", "value3");
		
		ObjectMapper mapper = new ObjectMapper();
		String serializedJson = null;
		try {
			serializedJson = mapper.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		assertEquals("{\"key1\":\"value1\",\"key2\":\"value2\",\"key3\":\"value3\"}", serializedJson);
		
		TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>(){};
		Map<String, String> dmap = null;
		try {
			dmap = new ObjectMapper().readValue(serializedJson, typeRef);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		assertEquals("{key1=value1, key2=value2, key3=value3}", dmap.toString());
	}	

	
	@Test   
    public void testCreateBasicAuthToken()
    {
    	String basicAuthToken = MetricsUtils.createBasicAuthToken("sampleuser", "samplepass");
    	Assert.assertEquals("c2FtcGxldXNlcjpzYW1wbGVwYXNz", basicAuthToken); 

    	basicAuthToken = MetricsUtils.createBasicAuthToken("myuser", "mypass");
    	Assert.assertEquals("bXl1c2VyOm15cGFzcw==", basicAuthToken); 
	}	
	
}