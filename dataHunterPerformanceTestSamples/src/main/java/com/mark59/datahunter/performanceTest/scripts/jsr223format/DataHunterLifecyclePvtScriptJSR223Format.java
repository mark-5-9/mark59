package com.mark59.datahunter.performanceTest.scripts.jsr223format;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.logging.log4j.Level;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import com.mark59.core.Outcome;
import com.mark59.core.utils.*;
import com.mark59.selenium.corejmeterimpl.*;
import com.mark59.selenium.drivers.SeleniumDriverFactory;
import com.mark59.seleniumDSL.core._GenericPage;
import com.mark59.seleniumDSL.pageElements.*;


// >> --------- COMMENT OUT THE NEXT TWO LINES ---------  
public class DataHunterLifecyclePvtScriptJSR223Format  {
public static void main(String[] args) throws InterruptedException{
// <<  
	
	
/**
 * Note that in this more advanced example access to the com.mark59.seleniumDSL.* packages is assumed (from the mark59-selenium-sample-dsl project).
 * <p>Hint: A quick and dirty way to do this is to copy target jar from the dataHunterPerformanceTestSamples project since it contains sample DSL
 * packages, into the lib/ext directory of your JMeter instance.  
 *   
 * @author Phil Webb
 */
class ThisScript extends SeleniumAbstractJavaSamplerClient {
	
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
	
	final class DataHunterInputPages extends _GenericPage {

		public DataHunterInputPages(WebDriver driver) {
			super(driver);
		}
		public DropdownList applicationStartsWithOrEquals() {
			return new DropdownList(driver, By.id("applicationStartsWithOrEquals"));
		};			
		public InputTextElement identifier() {
			return new InputTextElement(driver, By.id("identifier"));
		};			
		public InputTextElement lifecycle() {
			return new InputTextElement(driver, By.id("lifecycle"));
		};
		public DropdownList useability() {
			return new DropdownList(driver, By.id("useability"));
		};
		public InputTextElement otherdata() {
			return new InputTextElement(driver, By.id("otherdata"));
		};
		public InputTextElement epochtime() {
			return new InputTextElement(driver, By.id("epochtime"));
		};
		public DropdownList selectOrder() {
			return new DropdownList(driver, By.id("selectOrder"));
		};		
		public SubmitBtn submit() {
			return new SubmitBtn(driver, By.id("submit"));
		};
	}
	
	
	final class DatatHunterActionPages extends  _GenericPage {
		
		public DatatHunterActionPages(WebDriver driver) {
			super(driver);
		}
		public InputTextElement identifier() {
			return new InputTextElement(driver, By.id("identifier"));
		};
		public PageTextElement sql() {
			return new PageTextElement(driver, By.id("sql"));
		};	
		public PageTextElement sqlResult() {
			return new PageTextElement(driver, By.id("sqlResult"));
		};	
		public PageTextElement rowsAffected() {
			return new PageTextElement(driver, By.id("rowsAffected"));
		};	
		public PageTextElement sqlResultText() {
			return new PageTextElement(driver, By.id("sqlResultText"));
		};	
		public Link backLink() {
			return new Link(driver, "Back");
		};	
		public int getCountForBreakdown(String application, String lifecycle, String useability){  // breakdown result only 
			PageTextElement countForBreakdownElement = null;
			try { countForBreakdownElement =  new PageTextElement(driver, By.id((application + "_" + lifecycle + "_" + useability + "_count").replace(" ", "_")));
			} catch ( NoSuchElementException e) {return 0;	}
			int count = Integer.valueOf(countForBreakdownElement.getText());
			return count;
		}
		public HtmlTable printSelectedPoliciesTable() {
			return new HtmlTable(driver, "printSelectedPoliciesTable");
		};		
		public String formatResultsMessage(String tag){
			return  "DataHunter " +  sqlResultText().getText() + " at " + tag + ", SQL statement [" + sql().getText() + "]" +
					", rows affected [" +  rowsAffected().getText() + "], details [" + sqlResultText().getText() + "]";		
		}
	}

	
	@Override
	protected Map<String, String> additionalTestParameters() {
		Map<String, String> jmeterAdditionalParameters = new LinkedHashMap<String, String>();
		jmeterAdditionalParameters.put("DATAHUNTER_URL_HOST_PORT",	"http://localhost:8081");
		jmeterAdditionalParameters.put("DATAHUNTER_APPLICATION_ID", "DATAHUNTER_PV_TEST");
		jmeterAdditionalParameters.put("FORCE_TXN_FAIL_PERCENT", 	"20");
		jmeterAdditionalParameters.put("USER", 	 "default_user");		

		jmeterAdditionalParameters.put(SeleniumDriverFactory.DRIVER, "CHROME");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.HEADLESS_MODE, String.valueOf(false));
		jmeterAdditionalParameters.put(SeleniumDriverFactory.BROWSER_DIMENSIONS, Mark59Constants.DEFAULT_BROWSER_DIMENSIONS);
		jmeterAdditionalParameters.put(SeleniumDriverFactory.PAGE_LOAD_STRATEGY, PageLoadStrategy.NONE.toString());
		jmeterAdditionalParameters.put(SeleniumDriverFactory.PROXY, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.ADDITIONAL_OPTIONS, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.WRITE_FFOX_BROWSER_LOGFILE, String.valueOf(false));
		jmeterAdditionalParameters.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");			
		jmeterAdditionalParameters.put(SeleniumDriverFactory.EMULATE_NETWORK_CONDITIONS, "");			
		return jmeterAdditionalParameters;			
	}
	

	@Override
	protected void runSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm,  WebDriver driver) {
		
		String thread = Thread.currentThread().getName();
		String lifecycle = "thread_" + thread;
		String dataHunterUrl 	= context.getParameter("DATAHUNTER_URL_HOST_PORT");
		String application 		= context.getParameter("DATAHUNTER_APPLICATION_ID");
		int forceTxnFailPercent = Integer.valueOf(context.getParameter("FORCE_TXN_FAIL_PERCENT").trim());
		String user 			= context.getParameter("USER");
		
		PrintSomeMsgOnceAtStartUp(dataHunterUrl, driver);

		DataHunterInputPages dataHunterInputPages = new DataHunterInputPages(driver); 

// 		delete any existing policies for this application/thread combination
		jm.startTransaction("DH-lifecycle-0001-gotoDeleteMultiplePoliciesUrl");
		driver.get(dataHunterUrl + TestConstants.DELETE_MULTIPLE_POLICIES_URL_PATH + "?application=" + application);
		dataHunterInputPages.lifecycle().waitUntilClickable();
		jm.endTransaction("DH-lifecycle-0001-gotoDeleteMultiplePoliciesUrl");	
		
		dataHunterInputPages.lifecycle().type(lifecycle);

		DatatHunterActionPages resultsPage = new DatatHunterActionPages(driver);
		
		jm.startTransaction("DH-lifecycle-0100-deleteMultiplePolicies");		
		dataHunterInputPages.submit().submit().waitUntilClickable( resultsPage.backLink() );  
		waitActionPageCheckSqlOk(new DatatHunterActionPages(driver));
		jm.endTransaction("DH-lifecycle-0100-deleteMultiplePolicies");	
	
//		add a set of policies 		
		driver.get(dataHunterUrl + TestConstants.ADD_POLICY_URL_PATH + "?application=" + application);
		
		for (int i = 1; i <= 5; i++) {
			dataHunterInputPages.identifier().type("TESTID" + i);
			dataHunterInputPages.lifecycle().type(lifecycle);
			dataHunterInputPages.useability().selectByVisibleText(TestConstants.UNUSED) ;
			dataHunterInputPages.otherdata().type(user);		
			dataHunterInputPages.epochtime().type(new String(Long.toString(System.currentTimeMillis())));
//			jm.writeScreenshot("add_policy_TESTID" + i);

			jm.startTransaction("DH-lifecycle-0200-addPolicy");
			dataHunterInputPages.submit().submit().waitUntilClickable( resultsPage.backLink() );   
			waitActionPageCheckSqlOk(resultsPage);
			jm.endTransaction("DH-lifecycle-0200-addPolicy");
			
			resultsPage.backLink().click().waitUntilClickable( dataHunterInputPages.submit() ).thenSleep();    
		} 
	
//		dummy transaction just to test transaction failure behavior
		jm.startTransaction("DH-lifecycle-0299-sometimes-I-fail");
		int randomNum_1_to_100 = ThreadLocalRandom.current().nextInt(1, 101);
		if ( randomNum_1_to_100 >= forceTxnFailPercent ) {
			jm.endTransaction("DH-lifecycle-0299-sometimes-I-fail", Outcome.PASS);
		} else {
			jm.endTransaction("DH-lifecycle-0299-sometimes-I-fail", Outcome.FAIL);
		}
		
		driver.get(dataHunterUrl + TestConstants.COUNT_POLICIES_URL_PATH + "?application=" + application);
		dataHunterInputPages.useability().selectByVisibleText(TestConstants.UNUSED).thenSleep();   // ** note 2

		jm.startTransaction("DH-lifecycle-0300-countUnusedPolicies");
		dataHunterInputPages.submit().submit().waitUntilClickable( resultsPage.backLink() );
		waitActionPageCheckSqlOk(resultsPage);
		jm.endTransaction("DH-lifecycle-0300-countUnusedPolicies");
		
		Long countPolicies = Long.valueOf( resultsPage.rowsAffected().getText());
		LOG.debug( "countPolicies : " + countPolicies); 
		jm.userDataPoint(application + "_Total_Unused_Policy_Count", countPolicies);
		
// 		count breakdown (count for unused DATAHUNTER_PV_TEST policies for this thread )
		driver.get(dataHunterUrl + TestConstants.COUNT_POLICIES_BREAKDOWN_URL_PATH + "?application=" + application);		
		dataHunterInputPages.applicationStartsWithOrEquals().selectByVisibleText(TestConstants.EQUALS);
		dataHunterInputPages.useability().selectByVisibleText(TestConstants.UNUSED);

		jm.startTransaction("DH-lifecycle-0400-countUnusedPoliciesCurrentThread");		
		dataHunterInputPages.submit().submit();
		waitActionPageCheckSqlOk(resultsPage);		
		jm.endTransaction("DH-lifecycle-0400-countUnusedPoliciesCurrentThread");				
		
		// direct access to required row-column table element by computing the id:
		int countUsedPoliciesCurrentThread = resultsPage.getCountForBreakdown(application, lifecycle, TestConstants.UNUSED); 
		LOG.debug( "countUsedPoliciesCurrentThread : " + countUsedPoliciesCurrentThread); 
		jm.userDataPoint(application + "_This_Thread_Unused_Policy_Count", countUsedPoliciesCurrentThread);	

//		use next policy
		driver.get(dataHunterUrl + TestConstants.NEXT_POLICY_URL_PATH + "?application=" + application + "&pUseOrLookup=use");		
		dataHunterInputPages.lifecycle().type(lifecycle);
		dataHunterInputPages.useability().selectByVisibleText(TestConstants.UNUSED);
		dataHunterInputPages.selectOrder().selectByVisibleText(TestConstants.SELECT_MOST_RECENTLY_ADDED);
		
		jm.startTransaction("DH-lifecycle-0500-useNextPolicy");		
		dataHunterInputPages.submit().submit();
		waitActionPageCheckSqlOk(resultsPage);			
		jm.endTransaction("DH-lifecycle-0500-useNextPolicy");	
		
		if (LOG.isDebugEnabled() ) {LOG.debug("useNextPolicy: " + application + "-" + lifecycle + " : " + resultsPage.identifier() );	}
		
		//HTML table demo.
		long used=0;
		long unused=0;
		
		driver.get(dataHunterUrl + TestConstants.PRINT_SELECTED_POLICIES_URL_PATH  + "?application=" + application);
		dataHunterInputPages.submit().waitUntilClickable();
		
		jm.startTransaction("DH-lifecycle-0600-displaySelectedPolicies");	
		dataHunterInputPages.submit().submit();
		waitActionPageCheckSqlOk(resultsPage);
		// demo how to extract a transaction time from with a running script 
		org.apache.jmeter.samplers.SampleResult sr_0600 = jm.endTransaction("DH-lifecycle-0600-displaySelectedPolicies");
		LOG.debug("Transaction " + sr_0600.getSampleLabel() + " ran at " + sr_0600.getTimeStamp() + " and took " + sr_0600.getTime() + " ms." );
		
		HtmlTable printSelectedPoliciesTable = resultsPage.printSelectedPoliciesTable();
		for (HtmlTableRow tableRow : printSelectedPoliciesTable.getHtmlTableRows()) {
			if (tableRow.getColumnNumberOfExpectedColumns(4, 8).getText().equals("USED"))   used++;
			if (tableRow.getColumnNumberOfExpectedColumns(4, 8).getText().equals("UNUSED")) unused++;
		}	
		jm.userDataPoint("USED-count-html-demo",   used );				
		jm.userDataPoint("UNUSED-count-html-demo", unused );	
		LOG.debug("HTML demo: USED=" + used + ", UNUSED=" + unused); 
		
// 		delete multiple policies (test cleanup - a duplicate of the initial delete policies transactions)
		jm.startTransaction("DH-lifecycle-0001-gotoDeleteMultiplePoliciesUrl");		
		driver.get(dataHunterUrl + TestConstants.DELETE_MULTIPLE_POLICIES_URL_PATH + "?application=" + application);
		dataHunterInputPages.lifecycle().waitUntilClickable();		
		jm.endTransaction("DH-lifecycle-0001-gotoDeleteMultiplePoliciesUrl");	
		
		dataHunterInputPages.lifecycle().type(lifecycle);
		
		jm.startTransaction("DH-lifecycle-0100-deleteMultiplePolicies");		
		dataHunterInputPages.submit().submit();
		waitActionPageCheckSqlOk(new DatatHunterActionPages(driver));
		jm.endTransaction("DH-lifecycle-0100-deleteMultiplePolicies");	
		
//		jm.writeBufferedArtifacts();
	}


	private void waitActionPageCheckSqlOk(DatatHunterActionPages resultsPage) {
		String sqlResultText = resultsPage.sqlResult().getText();
		if (!"PASS".equals(sqlResultText)) {
			throw new RuntimeException("SQL issue (" + sqlResultText + ") : " +
						resultsPage.formatResultsMessage(resultsPage.getClass().getName()));
		}
	}
	

	@SuppressWarnings("unchecked")
	private synchronized void PrintSomeMsgOnceAtStartUp(String dataHunterUrl, WebDriver driver) {
		Properties sysprops = System.getProperties();
		if (!"true".equals(sysprops.get("printedOnce")) ) {	
			LOG.info("  using DataHunter with Url " + dataHunterUrl + "/dataHunter");
			Capabilities caps = ((ChromeDriver)driver).getCapabilities();
			LOG.info(" Browser Name and Version : " + caps.getBrowserName() + " " + caps.getVersion());
			if ("chrome".equalsIgnoreCase(caps.getBrowserName()) && caps.getCapability("chrome") != null ){
				String chromedriverVersion =  ((Map<String, String>)caps.getCapability("chrome")).get("chromedriverVersion");
				LOG.info(" Chrome Driver Version    : " +  ((Map<String, String>)caps.getCapability("chrome")).get("chromedriverVersion"));
				if (chromedriverVersion != null &&  chromedriverVersion.startsWith("2.44") ) {
					String outDatedDriver = "\n\n You are using the outdated ChromeDriver that ships with the Mark59 Selenium test scripts " +
							" project 'dataHunterPerformanceTestSamples'.  It may be unstable or not work at all." + 				
							"\n - Please visit https://chromedriver.chromium.org/downloads and update to a ChomeDriver which supports " +
							"Chrome browser version " + caps.getVersion() + "\n";
					System.out.println(outDatedDriver);
					LOG.warn(outDatedDriver);
				}	
			}
			sysprops.put("printedOnce", "true");
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