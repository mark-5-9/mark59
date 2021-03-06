package com.mark59.datahunter.performanceTest.scripts.jsr223format;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.logging.log4j.Level;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.mark59.core.utils.*;
import com.mark59.selenium.corejmeterimpl.*;
import com.mark59.selenium.drivers.SeleniumDriverFactory;


// >> --------- COMMENT OUT THE NEXT TWO LINES ---------  
public class DataHunterBasicSampleScriptJSR223Format  {
public static void main(String[] args) throws InterruptedException{
// <<  
	
	
class ThisScript extends SeleniumAbstractJavaSamplerClient {
	
	@SuppressWarnings("unused")
	final class TestConstants {
		public static final String DELETE_MULTIPLE_POLICIES_URL_PATH    = "/dataHunter/delete_multiple_policies";
		public static final String COUNT_POLICIES_URL_PATH              = "/dataHunter/count_policies";	
		public static final String ADD_POLICY_URL_PATH                  = "/dataHunter/add_policy";
		public static final String PRINT_SELECTED_POLICIES_URL_PATH     = "/dataHunter/print_selected_policies";
		public static final String COUNT_POLICIES_BREAKDOWN_URL_PATH    = "/dataHunter/count_policies_breakdown";		
		public static final String NEXT_POLICY_URL_PATH                 = "/dataHunter/next_policy";	
		public static final String UNUSED                               = "UNUSED";  
		public static final String EQUALS                               = "EQUALS";
		public static final String SELECT_MOST_RECENTLY_ADDED           = "SELECT_MOST_RECENTLY_ADDED";  
	}
	
	
	@Override
	protected Map<String, String> additionalTestParameters() {
		Map<String, String> jmeterAdditionalParameters = new LinkedHashMap<String, String>();
		jmeterAdditionalParameters.put("DATAHUNTER_URL_HOST_PORT",	"http://localhost:8081");
		jmeterAdditionalParameters.put("DATAHUNTER_APPLICATION_ID", "DATAHUNTER_PV_TEST_BASIC");
		jmeterAdditionalParameters.put("USER", 	 "default_user");		
		jmeterAdditionalParameters.put("DRIVER", "CHROME");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.HEADLESS_MODE, String.valueOf(false));
		jmeterAdditionalParameters.put(SeleniumDriverFactory.PAGE_LOAD_STRATEGY, PageLoadStrategy.NORMAL.toString());
		jmeterAdditionalParameters.put(SeleniumDriverFactory.PROXY, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.ADDITIONAL_OPTIONS, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.WRITE_FFOX_BROWSER_LOGFILE, 	String.valueOf(false));
		jmeterAdditionalParameters.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.EMULATE_NETWORK_CONDITIONS, "");				
		return jmeterAdditionalParameters;			
	}
	

	@Override
	protected void runSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm,  WebDriver driver) {
//		jm.logScreenshotsAtStartOfTransactions(Mark59LogLevels.WRITE);
//		jm.logScreenshotsAtEndOfTransactions(Mark59LogLevels.WRITE);
//		jm.logPageSourceAtStartOfTransactions(Mark59LogLevels.WRITE);		
//		jm.logPageSourceAtEndOfTransactions(Mark59LogLevels.WRITE );
//		jm.logPerformanceLogAtEndOfTransactions(Mark59LogLevels.WRITE);
		// you need to use jm.writeBufferedArtifacts to output BUFFERed data (see end of this method)  		
//		jm.logAllLogsAtEndOfTransactions(Mark59LogLevels.BUFFER);		

		String thread = Thread.currentThread().getName();
		String lifecycle = "thread_" + thread;
//		System.out.println("Thread " + thread + " is running with LOG level " + LOG.getLevel());

		String dataHunterUrl 	= context.getParameter("DATAHUNTER_URL_HOST_PORT");
		String application 		= context.getParameter("DATAHUNTER_APPLICATION_ID");
		String user 			= context.getParameter("USER");

// 		delete any existing policies for this application/thread combination
		
		jm.startTransaction("DH-basic-0001-gotoDeleteMultiplePoliciesUrl");
		driver.get(dataHunterUrl + TestConstants.DELETE_MULTIPLE_POLICIES_URL_PATH + "?application=" + application);
		jm.endTransaction("DH-basic-0001-gotoDeleteMultiplePoliciesUrl");	
		
		driver.findElement(By.id("lifecycle")).sendKeys(lifecycle);  ; 
		
		jm.startTransaction("DH-lifecycle-0100-deleteMultiplePolicies");
		driver.findElement(By.id("submit")).submit();
		checkSqlOk(driver.findElement(By.id("sqlResult")));
		jm.endTransaction("DH-lifecycle-0100-deleteMultiplePolicies");	
	
//		add a policy 		
		driver.get(dataHunterUrl + TestConstants.ADD_POLICY_URL_PATH + "?application=" + application);

		driver.findElement(By.id("identifier")).sendKeys("DH-BASIC-POLICY"); 		
		driver.findElement(By.id("lifecycle")).sendKeys(lifecycle); 		
		
		Select dropdown = new Select(driver.findElement(By.id("useability")));
		dropdown.selectByVisibleText(TestConstants.UNUSED);

		driver.findElement(By.id("otherdata")).sendKeys(user); 
		driver.findElement(By.id("epochtime")).sendKeys(new String(Long.toString(System.currentTimeMillis()))); 
//		jm.writeScreenshot("add_policy_DH-BASIC-POLICY");
		
		jm.startTransaction("DH-lifecycle-0200-addPolicy");
		driver.findElement(By.id("submit")).submit();
		checkSqlOk(driver.findElement(By.id("sqlResult")));
		jm.endTransaction("DH-lifecycle-0200-addPolicy");
		
//		set a Data Point		
		Long rowsAffected = Long.valueOf(driver.findElement(By.id("rowsAffected")).getText());
		LOG.debug( "rowsAffected : " + rowsAffected); 
		jm.userDataPoint(application + "_PolicyRowsAffected", rowsAffected);    // (expected to be always 1 for this action)		

		driver.findElement(By.linkText("Back")).click();
		
//		jm.writeBufferedArtifacts();
	}

	private void checkSqlOk(WebElement sqlResultWebElement) {
		String sqlResultText = sqlResultWebElement.getText();
		if ( !"PASS".equals(sqlResultText) ) {
			throw new RuntimeException("SQL issue (" + sqlResultText + ")");   
		}
	}
};


// >> --------- COMMENT OUT THE NEXT THREE LINES ---------  
org.apache.jmeter.samplers.SampleResult SampleResult = new org.apache.jmeter.samplers.SampleResult();
SampleResult.sampleStart();
Log4jConfigurationHelper.init(Level.INFO) ;
// << 

org.apache.jmeter.samplers.SampleResult testResults = new ThisScript().runSeleniumTest(KeepBrowserOpen.NEVER);
if (testResults != null) {
	for (org.apache.jmeter.samplers.SampleResult subResult : testResults.getSubResults()) {
		SampleResult.addSubResult(subResult, false);
	}
}
SampleResult.setDataType("PARENT" );
SampleResult.setEndTime(0);

// >> --------- COMMENT OUT THE END BRACES BELOW --------- 
}
}