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

package com.mark59.metricsruncheck;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.mark59.metrics.application.AppConstants;
import com.mark59.metrics.data.application.dao.ApplicationDAO;
import com.mark59.metrics.data.application.dao.ApplicationDAOjdbcTemplateImpl;
import com.mark59.metrics.data.beans.Sla;
import com.mark59.metrics.data.eventMapping.dao.EventMappingDAO;
import com.mark59.metrics.data.eventMapping.dao.EventMappingDAOjdbcTemplateImpl;
import com.mark59.metrics.data.graphMapping.dao.GraphMappingDAO;
import com.mark59.metrics.data.graphMapping.dao.GraphMappingDAOjdbcTemplateImpl;
import com.mark59.metrics.data.metricSla.dao.MetricSlaDAO;
import com.mark59.metrics.data.metricSla.dao.MetricSlaDAOjdbcImpl;
import com.mark59.metrics.data.run.dao.RunDAO;
import com.mark59.metrics.data.run.dao.RunDAOjdbcTemplateImpl;
import com.mark59.metrics.data.sla.dao.SlaDAO;
import com.mark59.metrics.data.sla.dao.SlaDAOjdbcImpl;
import com.mark59.metrics.data.testTransactions.dao.TestTransactionsDAO;
import com.mark59.metrics.data.testTransactions.dao.TestTransactionsDAOjdbcTemplateImpl;
import com.mark59.metrics.data.transaction.dao.TransactionDAO;
import com.mark59.metrics.data.transaction.dao.TransactionDAOjdbcTemplateImpl;
import com.mark59.metrics.metricSla.MetricSlaChecker;
import com.mark59.metrics.metricSla.MetricSlaResult;
import com.mark59.metrics.services.SlaService;
import com.mark59.metrics.services.SlaServiceImpl;
import com.mark59.metrics.sla.SlaChecker;
import com.mark59.metrics.sla.SlaTransactionResult;
import com.mark59.metrics.sla.SlaUtilities;
import com.mark59.metricsruncheck.run.JmeterRun;
import com.mark59.metricsruncheck.run.LrRun;
import com.mark59.metricsruncheck.run.PerformanceTest;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */

@SpringBootApplication
public class Runcheck  implements CommandLineRunner 
{
	
    @Autowired
    DataSource dataSource;
	
	@Autowired
	MetricSlaDAO  metricSlaDAO;
	
	@Autowired
	TransactionDAO transactionDAO; 	
	
	@Autowired
	SlaDAO slaDAO; 

	@Autowired
	RunDAO runDAO; 

	@Autowired
	TestTransactionsDAO testTransactionsDAO; 	
	
	@Autowired
	EventMappingDAO eventMappingDAO; 	
	
	@Autowired
	ApplicationContext context;


	private static String argTool;
	private static String argApplication;
	private static String argInput;
	private static String argReference;
	private static String argExcludestart;
	private static String argCaptureperiod;
	private static String argTimeZone;

    @Bean
    public ApplicationDAO applicationDAO() {
        return new ApplicationDAOjdbcTemplateImpl();
    }
    
    @Bean
    public RunDAO runDAO() {
        return new RunDAOjdbcTemplateImpl();
    }
    
    @Bean
    public TransactionDAO transactionDAO() {
        return new TransactionDAOjdbcTemplateImpl();
    }
    
    @Bean
    public SlaDAO slaDAO() {
        return new SlaDAOjdbcImpl();
    }
    
    @Bean
    public MetricSlaDAO metricSlaDAO() {
        return new MetricSlaDAOjdbcImpl();
    }

    @Bean
    public SlaService slaService() {
        return new SlaServiceImpl();
    }

    @Bean
    public GraphMappingDAO graphMappingDAO() {
        return new GraphMappingDAOjdbcTemplateImpl();
    }

    @Bean
    public EventMappingDAO eventMappingDAO() {
        return new EventMappingDAOjdbcTemplateImpl();
    }

    @Bean
    public TestTransactionsDAO testTransactionsDAO() {
        return new TestTransactionsDAOjdbcTemplateImpl();
    }
	
	
	private static void parseArguments(String[] args) {
		Options options = new Options(); 
		options.addRequiredOption("a", "application",   true, "Application Id, as it will appear in the Trending Graph Application dropdown selections");
		options.addRequiredOption("i", "input",			true, "The directory or file containing the performance test results.  Multiple xml/csv/jtl results files allowed for Jmeter within a directory, a single .mdb file is required for Loadrunner");			
		options.addOption("r", "reference",		 		true, "A reference.  Usual purpose would be to identify this run (possibly by a link). Eg <a href='http://ciServer/job/myJob/001/HTML_Report'>run 001</a>");
		options.addOption("t", "tool",      			true, "Performance Tool used to generate the results to be processed { JMETER (default) | LOADRUNNER }" );
		options.addOption("h", "mysqlserver",  			true, "Server hosting the 'pvmetrics' mySql database where results will be held (defaults to localhost)" );
		options.addOption("p", "mysqlPort",    			true, "Port number for the 'pvmetrics' mySql database where results will be held (defaults to 3306)" );		
		options.addOption("u", "dbUsername",   			true, "Username 'pvmetrics' mySql database (defaults to admin)" );				
		options.addOption("w", "dbpassWord",   			true, "Password for the 'pvmetrics' mySql database" );				
		options.addOption("x", "eXcludestart",     		true, "exclude results at the start of the test for the given number of minutes (defaults to 0)" );			
		options.addOption("c", "captureperiod",    		true, "Only capture test results for the given number of minutes, from the excluded start period (default is all results except those skipped by the excludestart parm are included)" );			
		options.addOption("z", "timeZone",    			true, "(Loadrunner only) Required when running extract from zone other than where Analysis Report was generated. Also, internal raw stored time"
				+ " may not take daylight saving into account.  Two format otions 1) offest againt GMT. Eg 'GMT+02:00' or 2) IANA Time Zone Database (TZDB) codes. Refer to https://en.wikipedia.org/wiki/List_of_tz_database_time_zones. Eg 'Australia/Sydney' ");   	
		
		HelpFormatter formatter = new HelpFormatter();
		CommandLine commandLine = null;
		CommandLineParser parser = new DefaultParser();
		try {
			commandLine = parser.parse(options, args);
		} catch (ParseException exp) {
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			formatter.printHelp("Runcheck", options);
			printSampleUsage();
			throw new RuntimeException();
		}		
		
		argApplication     	= commandLine.getOptionValue("a");			
		argInput 			= commandLine.getOptionValue("i");	
		argReference 		= commandLine.getOptionValue("r", AppConstants.NO_ARGUMENT_PASSED + " (a reference will be generated)" ); 	  		
		argTool   			= commandLine.getOptionValue("t", AppConstants.JMETER );
		argExcludestart  	= commandLine.getOptionValue("x", "0");		
		argCaptureperiod  	= commandLine.getOptionValue("c", AppConstants.ALL );
		argTimeZone  		= commandLine.getOptionValue("z", new GregorianCalendar().getTimeZone().getID() );				
		
		if ( !AppConstants.JMETER.equalsIgnoreCase(argTool)  &&  !AppConstants.LOADRUNNER.equalsIgnoreCase(argTool)) {
			formatter.printHelp( "Runcheck", options );
			printSampleUsage();
			throw new RuntimeException("The tool (t) argument must be set to JMETER or LOADRUNNER ! (or not used, in which case JMETER is assumed)");  
		}
		if (!StringUtils.isNumeric(argExcludestart) ) {
			formatter.printHelp( "Runcheck", options );
			printSampleUsage();
			throw new RuntimeException("The excludestart (x) parameter must be numeric");  
		}
		if (!StringUtils.isNumeric(argCaptureperiod) && !argCaptureperiod.equalsIgnoreCase(AppConstants.ALL) ) {
			formatter.printHelp( "Runcheck", options );
			printSampleUsage();
			throw new RuntimeException("The captureperiod (c) parameter must be numeric or " +  (AppConstants.ALL) );  
		}
		if (! ( argTimeZone.equals("GMT") || !TimeZone.getTimeZone(argTimeZone).getID().equals("GMT"))){
			// https://stackoverflow.com/questions/13092865/timezone-validation-in-java
			formatter.printHelp( "Runcheck", options );
			printSampleUsage();
			throw new RuntimeException("The timezone argumment (z) is invalid : " +  argTimeZone );  
		}
		if (argApplication.length() > 32 ) {
			argApplication = argApplication.substring(0, 32);					
			System.out.println();
			System.out.println("** The application arugment will be truncated to 32 characters : " + argApplication );
			System.out.println();
		}
		
		String mysqlserver	= commandLine.getOptionValue("h", "localhost");
		String mysqlPort 	= commandLine.getOptionValue("p", "3306");
		String dbusername  	= commandLine.getOptionValue("u", "admin");
		String dbpassword  	= commandLine.getOptionValue("w", "admin");
	
		System.setProperty("mysql.server", mysqlserver);
		System.setProperty("mysql.port",   mysqlPort);		
		System.setProperty("dbusername",   dbusername);		
		System.setProperty("dbpassword",   dbpassword);		
		
		System.out.println();
		System.out.println("Runcheck executing using the following arguments " );
		System.out.println("------------------------------------------------ " );	
		System.out.println(" application    : " + argApplication );				    
		System.out.println(" input          : " + argInput );
		System.out.println(" reference      : " + argReference );
		System.out.println(" tool           : " + argTool );		
		System.out.println(" mysqlserver    : " + mysqlserver );		
		System.out.println(" mysqlPort      : " + mysqlPort );				
		System.out.println(" dbUsername     : " + dbusername );				
		System.out.println(" eXcludestart   : " + argExcludestart + " (mins)" );		
		System.out.println(" captureperiod  : " + argCaptureperiod + " (mins)"  );
		System.out.println(" timeZone       : " + argTimeZone );		
		System.out.println("------------------------------------------------   " );				    
		System.out.println();
	}

	private static void printSampleUsage() {
		System.out.println();	
		System.out.println( "Sample usages");
		System.out.println( "------------");
		System.out.println( "   1. Jmeter example ");
		System.out.println( "   Process Jmeter xml formatted result in directory C:/jmeter-results/BIGAPP  (file/s ends in .xml)");
		System.out.println( "   The graph application name will be MY_COMPANY_BIG_APP, with a reference for this run of 'run ref 645'.");
		System.out.println( "   The pvmetrics database is hosted locally on a MySql instance assigned to port 3309 (default user/password of admin/admin) : "  );
		System.out.println( "   java -jar runcheck.jar-1.0 -a MY_COMPANY_BIG_APP -i C:/jmeter-results/BIGAPP -r \"run ref 645\" -p 3309  ");
		System.out.println( "   2. Loadrunner example");		
		System.out.println( "   Process Loadrunner analysis result at C:/templr/BIGAPP/AnalysisSession (containing file AnalysisSession.mdb).  ");
		System.out.println( "   The graph application name will be MY_COMPANY_BIG_APP, with a reference for this run of 'run ref 644'.");
		System.out.println( "   The pvmetrics database is hosted locally on a MySql instance assigned to port 3309 (default user/password of admin/admin) : "  );
		System.out.println( "   java -jar runcheck.jar-1.0 -a MY_COMPANY_BIG_APP -i C:/templr/BIGAPP/AnalysisSession/AnalysisSession.mdb -r \"run ref 644\" -p 3309 -t LOADRUNNER" );
		System.out.println();
	}


	
	/**
	 * The args are already parsed (as they may contain properties that need to be pre-set for the spring Application Context) 
	 */
	@Override
	public void run(String... args) throws Exception {
		loadTestRun(argTool, argApplication, argInput, argReference, argExcludestart, argCaptureperiod, argTimeZone );
	};
	
	private void loadTestRun(String tool, String application, String input, String runReference, String excludestart, String captureperiod, String timeZone) throws IOException {

		PerformanceTest performanceTest;
		
		if (AppConstants.LOADRUNNER.equalsIgnoreCase(tool)){		
			performanceTest = new LrRun(context, application, input, runReference, excludestart, captureperiod, timeZone );
		} else {
			performanceTest = new JmeterRun(context, application, input, runReference, excludestart, captureperiod);
		}
		
		printGenericSlaKeyUsage(application);

		List<SlaTransactionResult> slaTransactionResults = new SlaChecker().listTransactionsWithFailedSlas(application, performanceTest.getTransactionsMap().values(), slaDAO);;
		printTransactionalMetricSlaResults(slaTransactionResults);
		
		String runTime = performanceTest.getRunSummary().getRunTime(); 

		List<String> slasWithMissingTxns  =  new SlaChecker().checkForMissingTransactionsWithDatabaseSLAs(application, runTime, slaDAO  ); 
		printSlasWitMissingTxnsInThisRun(slasWithMissingTxns);		
		
		if (slasWithMissingTxns.isEmpty()  && slaTransactionResults.isEmpty() ){
			System.out.println( "Runcheck:  No transactionalSLA has been marked as failed, as recorded on the SLA Reference Database");
		} 
		
		List<MetricSlaResult> metricSlaResults = new MetricSlaChecker().listFailedMetricSLAs(application, runTime, null, metricSlaDAO, transactionDAO);
		printMetricSlaResults(metricSlaResults);
	}
	

	private void printGenericSlaKeyUsage(String application) {
		//printing if a generic SLA exists - for information only ... the used SLA is derived in slaChecker
		String defaultSlaTxnId 	= SlaUtilities.deriveDefaultSLAtransactionId(application);
		Sla genericSla = slaDAO.getSla(application, defaultSlaTxnId, defaultSlaTxnId);
		if ( genericSla != null ){
			System.out.println( "          SLA Generic KEY values.  90th : " +   genericSla.getSla90thResponse() + ", fail% : " +  genericSla.getSlaFailPercent() );
		} else {
			System.out.println( "          SLA Generic KEY not found (ie, only exact transaction matches will be used)" );
		}
	}


	private void printSlasWitMissingTxnsInThisRun(List<String> slasWithMissingTxns) {
		for (String slaWithMissingTxn : slasWithMissingTxns) {
    		System.out.println( "Runcheck: SLA Failed : Error : an SLA exists for transaction " + slaWithMissingTxn + ", but that transaction does not appear in the run results ! "  );			
		}
	}



	private Boolean printTransactionalMetricSlaResults(List<SlaTransactionResult> slaTransactionResults) {
		Boolean allPassed = true; 
		for (SlaTransactionResult slaTransactionResult : slaTransactionResults) {
			if ( !slaTransactionResult.isPassedAllSlas()){
				allPassed = false;
			}
			
			if ( !slaTransactionResult.isPassed90thResponse()){
	    		System.out.println( "Runcheck: SLA Failed Warning  : " + slaTransactionResult.getTxnId() + " has failed it's 90th Percentile Response Time SLA as recorded on the SLA database ! "  );
	    		System.out.println( "                      response was " + slaTransactionResult.getTxn90thResponse() + " secs, SLA of " +  slaTransactionResult.getSla90thResponse());				
				
			}
			if ( !slaTransactionResult.isPassedFailPercent()){
	    		System.out.println( "Runcheck: SLA Failed : Error : " + slaTransactionResult.getTxnId() + " has failed it's % Error Rate SLA as recorded on the SLA database ! "  );
	    		System.out.println( "                      % errort was " + new DecimalFormat("#.##").format(slaTransactionResult.getTxnFailurePercent()) + 
	    				" %, SLA of " +  slaTransactionResult.getSlaFailurePercent());				
			}
			if ( !slaTransactionResult.isPassedPassCount()){
	    		System.out.println( "Runcheck: SLA Failed : Error : " + slaTransactionResult.getTxnId() + " has failed it's Pass Count SLA as recorded on the SLA database ! "  );
	    		System.out.println( "                      count was " + slaTransactionResult.getTxnPassCount() + 
	    				" Pass Count SLA is " +  slaTransactionResult.getSlaPassCount() + ", with variance of " + slaTransactionResult.getSlaPassCountVariancePercent() + "%") ;				
			}
		}
		return allPassed;
	}


	
	private void printMetricSlaResults(List<MetricSlaResult> metricSlaResults) {
		for (MetricSlaResult metricSlaResult : metricSlaResults) {
			System.out.println( "Runcheck: " + metricSlaResult.getMessageText()); 
		}
		if (metricSlaResults.isEmpty()){
			System.out.println( "Runcheck:  No metrics SLA has been marked as failed, as recorded on the SLA Metrics Reference Database");
		}
	}



	/**
	 * for a quick and dirty test, set the args: eg
	 * args = new String[] { "-a", "BIGAPP", "-i", "C:/Jmeter_Results/BIGAPP","-h","localhost", "-p", "3306", "-t", "JMETER", "-r", "test_run_01"  };
	 */
	public static void main(String[] args) throws IOException {
		try {

			System.out.println("Starting runcheck .. (v2.2.0) ");
			
			parseArguments(args);
			
			SpringApplication application = new SpringApplication(Runcheck.class);
			application.setWebApplicationType(WebApplicationType.NONE);
			application.setBannerMode(Banner.Mode.OFF);
			application.run(args);
			
			System.out.println("runcheck completed.");

		} catch (Exception e) {
			System.out.println("Runcheck: Error -  : Exception - stacktrace follows.");
			e.printStackTrace();
		}
	}

}
