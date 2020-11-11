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
import java.util.ArrayList;
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
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.SimpleAES;
import com.mark59.metrics.application.AppConstantsMetrics;
import com.mark59.metrics.data.eventMapping.dao.EventMappingDAO;
import com.mark59.metrics.data.metricSla.dao.MetricSlaDAO;
import com.mark59.metrics.data.run.dao.RunDAO;
import com.mark59.metrics.data.sla.dao.SlaDAO;
import com.mark59.metrics.data.testTransactions.dao.TestTransactionsDAO;
import com.mark59.metrics.data.transaction.dao.TransactionDAO;
import com.mark59.metrics.metricSla.MetricSlaChecker;
import com.mark59.metrics.metricSla.MetricSlaResult;
import com.mark59.metrics.sla.SlaChecker;
import com.mark59.metrics.sla.SlaTransactionResult;
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
    String currentDatabaseProfile;  
    
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
	private static String argDatabasetype;
	private static String argReference;
	private static String argExcludestart;
	private static String argCaptureperiod;
	private static String argTimeZone;

	private List<MetricSlaResult> metricSlaResults = new ArrayList<MetricSlaResult>();

	
	public static void parseArguments(String[] args) {
		Options options = new Options(); 
		options.addRequiredOption("a", "application",	true, "Application Id, as it will appear in the Trending Graph Application dropdown selections");
		options.addRequiredOption("i", "input",			true, "The directory or file containing the performance test results.  Multiple xml/csv/jtl results files allowed for Jmeter within a directory, a single .mdb file is required for Loadrunner");			
		options.addOption("d", "databasetype",			true, "Load data to a 'h2', 'pg' or 'mysql' database (defaults to 'h2')");			
		options.addOption("r", "reference",		 		true, "A reference.  Usual purpose would be to identify this run (possibly by a link). Eg <a href='http://ciServer/job/myJob/001/HTML_Report'>run 001</a>");
		options.addOption("t", "tool",      			true, "Performance Tool used to generate the results to be processed { JMETER (default) | LOADRUNNER }" );
		options.addOption("h", "dbserver",				true, "Server hosting the database where results will be held (defaults to localhost). \n"
																+ "*******************************************\n"
																+ "** NOTE: all db options applicable to MySQL or Postgres ONLY\n"  
																+ "*******************************************");
		options.addOption("p", "dbPort",    			true, "Port number for the database where results will be held (defaults to 3306 for MySQL, 5432 for Postgres, 9902 for H2 tcp)" );		
		options.addOption("s", "dbSchema",				true, "database schema (MySQL terminology) / database name (Postgres terminology) defaults to metricsdb" );
		options.addOption("u", "dbUsername",   			true, "Username for the database (defaults to admin)" );				
		options.addOption("w", "dbpassWord",   			true, "Password for the database" );				
		options.addOption("y", "dbpassencrYpted",		true, "Encrypted Password for the database (value as per the encryption used by mark59-server-metrics-web application 'Edit Server Profile' page)");				
		options.addOption("q", "dbxtraurlparms",		true, "Any special parameters to append to the end of the database URL (include the ?). Eg \"?allowPublicKeyRetrieval=truee&useSSL=false\" (the quotes are needed to escape the ampersand)");				
		options.addOption("x", "eXcludestart",     		true, "exclude results at the start of the test for the given number of minutes (defaults to 0)" );			
		options.addOption("c", "captureperiod",    		true, "Only capture test results for the given number of minutes, from the excluded start period (default is all results except those skipped by the excludestart parm are included)" );			
		options.addOption("z", "timeZone",    			true, "(Loadrunner only) Required when running extract from zone other than where Analysis Report was generated. Also, internal raw stored time"
				+ " may not take daylight saving into account.  Two format options 1) offset against GMT. Eg 'GMT+02:00' or 2) IANA Time Zone Database (TZDB) codes. Refer to https://en.wikipedia.org/wiki/List_of_tz_database_time_zones. Eg 'Australia/Sydney' ");   	
		
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
		argDatabasetype 	= commandLine.getOptionValue("d", Mark59Constants.MYSQL);	
		argReference 		= commandLine.getOptionValue("r", AppConstantsMetrics.NO_ARGUMENT_PASSED + " (a reference will be generated)" ); 	  		
		argTool   			= commandLine.getOptionValue("t", AppConstantsMetrics.JMETER );
		argExcludestart  	= commandLine.getOptionValue("x", "0");		
		argCaptureperiod  	= commandLine.getOptionValue("c", AppConstantsMetrics.ALL );
		argTimeZone  		= commandLine.getOptionValue("z", new GregorianCalendar().getTimeZone().getID() );				
		
		if (!Mark59Constants.H2.equalsIgnoreCase(argDatabasetype)
				&& !Mark59Constants.MYSQL.equalsIgnoreCase(argDatabasetype)
				&& !Mark59Constants.PG.equalsIgnoreCase(argDatabasetype)
				&& !Mark59Constants.H2TCPCLIENT.equalsIgnoreCase(argDatabasetype)
				&& !Mark59Constants.H2MEM.equalsIgnoreCase(argDatabasetype)) {    // h2mem used for internal testing
			formatter.printHelp("Runcheck", options);
			printSampleUsage();
			throw new RuntimeException(
					"The database type (d) argument must be set to 'pg', 'mysql', 'h2', 'h2mem' or 'h2tcpclient'! (or not used, in which case 'mysql' is assumed)");
		}
		if ( !AppConstantsMetrics.JMETER.equalsIgnoreCase(argTool)  &&  !AppConstantsMetrics.LOADRUNNER.equalsIgnoreCase(argTool)) {
			formatter.printHelp( "Runcheck", options );
			printSampleUsage();
			throw new RuntimeException("The tool (t) argument must be set to JMETER or LOADRUNNER ! (or not used, in which case JMETER is assumed)");  
		}
		if (!StringUtils.isNumeric(argExcludestart) ) {
			formatter.printHelp( "Runcheck", options );
			printSampleUsage();
			throw new RuntimeException("The excludestart (x) parameter must be numeric");  
		}
		if (!StringUtils.isNumeric(argCaptureperiod) && !argCaptureperiod.equalsIgnoreCase(AppConstantsMetrics.ALL) ) {
			formatter.printHelp( "Runcheck", options );
			printSampleUsage();
			throw new RuntimeException("The captureperiod (c) parameter must be numeric or " +  (AppConstantsMetrics.ALL) );  
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
	
		String dbDefaultPort = "";
		if (Mark59Constants.MYSQL.equalsIgnoreCase(argDatabasetype)){
			dbDefaultPort = "3306";
		} else if (Mark59Constants.PG.equalsIgnoreCase(argDatabasetype)){
			dbDefaultPort = "5432";
		} else if (Mark59Constants.H2TCPCLIENT.equalsIgnoreCase(argDatabasetype)){
			dbDefaultPort = "9092";
		}
		
		String dbserver	 	 	= commandLine.getOptionValue("h", "localhost");
		String dbPort 	 	 	= commandLine.getOptionValue("p", dbDefaultPort);
		String dbSchema	 		= commandLine.getOptionValue("s", "metricsdb");
		String dbUsername 	 	= commandLine.getOptionValue("u", "admin");
		String dbxtraurlparms	= commandLine.getOptionValue("q", "");
	
		// argDatabasetype used to set via application.properties: spring.profiles.active={spring.profile}
		System.setProperty("spring.profile", argDatabasetype);
		
		System.setProperty("mysql.server",  	  	dbserver);
		System.setProperty("mysql.port",    	  	dbPort);		
		System.setProperty("mysql.schema",  	  	dbSchema);		
		System.setProperty("mysql.username",	  	dbUsername);
		System.setProperty("mysql.xtra.url.parms",	dbxtraurlparms);
		
		System.setProperty("pg.server",   			dbserver);
		System.setProperty("pg.port",     			dbPort);		
		System.setProperty("pg.database",      		dbSchema);		
		System.setProperty("pg.username", 	   		dbUsername);
		System.setProperty("pg.xtra.url.parms",		dbxtraurlparms);
		
		System.setProperty("h2.server",   			dbserver);
		System.setProperty("h2.port",     			dbPort);
		
		// for H2/H2MEM database settings are hard coded in their property files and cannot be changed,
		// so this is just for display purposes
		if (Mark59Constants.H2.equalsIgnoreCase(argDatabasetype)){
			dbserver   = "localhost (all db settings hard-coded for h2";
			dbPort     = "";
			dbSchema   = "metrics";
			dbUsername = "sa";
		} else if (Mark59Constants.H2MEM.equalsIgnoreCase(argDatabasetype)){
			dbserver   = "localhost (all db settings hard-coded for h2mem)";
			dbPort     = "";
			dbSchema   = "metricsmem";
			dbUsername = "sa";			
		} 
		
		String dbpassWord 	   = commandLine.getOptionValue("w", "admin");
		String dbpassencrYpted = commandLine.getOptionValue("y");
		
		if (StringUtils.isAllBlank(dbpassencrYpted) ) {
			System.setProperty("mysql.password", dbpassWord);		
			System.setProperty("pg.password",    dbpassWord);		
		} else {
			System.setProperty("mysql.password", SimpleAES.decrypt(dbpassencrYpted));			
			System.setProperty("pg.password", 	 SimpleAES.decrypt(dbpassencrYpted));			
		}
		
		System.out.println();
		System.out.println("Runcheck executing using the following arguments " );
		System.out.println("------------------------------------------------ " );	
		System.out.println(" application    : " + argApplication );				    
		System.out.println(" input          : " + argInput );
		System.out.println(" database       : " + argDatabasetype );
		System.out.println(" reference      : " + argReference );
		System.out.println(" tool           : " + argTool );		
		System.out.println(" dbserver       : " + dbserver );		
		System.out.println(" dbPort         : " + dbPort );				
		System.out.println(" dbSchema       : " + dbSchema );				
		System.out.println(" dbxtraurlparms : " + dbxtraurlparms );				
		System.out.println(" dbUsername     : " + dbUsername );				
		System.out.println(" eXcludestart   : " + argExcludestart + " (mins)" );		
		System.out.println(" captureperiod  : " + argCaptureperiod + " (mins)"  );
		System.out.println(" timeZone       : " + argTimeZone );		
		System.out.println("------------------------------------------------   " );				    
		System.out.println();
	}

	public static void printSampleUsage() {
		System.out.println();	
		System.out.println( "Sample usages");
		System.out.println( "------------");
		System.out.println( "   1. JMeter example ");
		System.out.println( "   Process Jmeter xml formatted result in directory C:/jmeter-results/BIGAPP  (file/s ends in .xml)");
		System.out.println( "   The graph application name will be MY_COMPANY_BIG_APP, with a reference for this run of 'run ref 645'.");
		System.out.println( "   The metricsdb database is hosted locally on a MySql instance assigned to port 3309 (default user/password of admin/admin) : "  );
		System.out.println( "   java -jar metricsRuncheck.jar -a MY_COMPANY_BIG_APP -i C:/jmeter-results/BIGAPP -r \"run ref 645\" -p 3309  ");
		System.out.println( "   2. Loadrunner example");		
		System.out.println( "   Process Loadrunner analysis result at C:/templr/BIGAPP/AnalysisSession (containing file AnalysisSession.mdb).  ");
		System.out.println( "   The graph application name will be MY_COMPANY_BIG_APP, with a reference for this run of 'run ref 644'.");
		System.out.println( "   The metricsdb database is hosted locally on a MySql instance assigned to port 3309 (default user/password of admin/admin) : "  );
		System.out.println( "   java -jar metricsRuncheck.jar -a MY_COMPANY_BIG_APP -i C:/templr/BIGAPP/AnalysisSession/AnalysisSession.mdb -r \"run ref 644\" -p 3309 -t LOADRUNNER" );
		System.out.println();
	}


	
	/**
	 * The args are already parsed (as they may contain properties that need to be pre-set for the spring Application Context) 
	 */
	@Override
	public void run(String... args) throws Exception {
		System.out.println("Spring configuration complete, " + currentDatabaseProfile + " database in use."  );
		
		if (Mark59Constants.H2.equalsIgnoreCase(currentDatabaseProfile) || Mark59Constants.H2TCPCLIENT.equalsIgnoreCase(currentDatabaseProfile)){
			System.out.println();			
			System.out.println("***************************************************************************************" );
			System.out.println("*   The use of a H2 database is intended to assist in tutorials and 'quick start'.    *" );
			System.out.println("*   Please use a MySQL or PostgreSQL database for more formal work.                   *" );			
			System.out.println("*                                                                                     *" );			
			System.out.println("***************************************************************************************" );
			System.out.println();			
		} else 	if (Mark59Constants.H2MEM.equalsIgnoreCase(currentDatabaseProfile)) {
			System.out.println();			
			System.out.println("***************************************************************************************" );
			System.out.println("*   The H2 'In Memory' option primarily created for internal testing.                 *" );
			System.out.println("***************************************************************************************" );
			System.out.println();			
		}
		loadTestRun(argTool, argApplication, argInput, argReference, argExcludestart, argCaptureperiod, argTimeZone );
	};

	
	public void loadTestRun(String tool, String application, String input, String runReference, String excludestart, String captureperiod, String timeZone) throws IOException {

		PerformanceTest performanceTest;
		
		if (AppConstantsMetrics.LOADRUNNER.equalsIgnoreCase(tool)){		
			performanceTest = new LrRun(context, application, input, runReference, excludestart, captureperiod, timeZone );
		} else {
			performanceTest = new JmeterRun(context, application, input, runReference, excludestart, captureperiod);
		}
		
		List<SlaTransactionResult> slaTransactionResults = new SlaChecker().listTransactionsWithFailedSlas(application, performanceTest.getTransactionSummariesThisRun(), slaDAO);;
		printTransactionalMetricSlaResults(slaTransactionResults);
		
		String runTime = performanceTest.getRunSummary().getRunTime(); 

		List<String> slasWithMissingTxns  =  new SlaChecker().checkForMissingTransactionsWithDatabaseSLAs(application, runTime, slaDAO  ); 
		printSlasWitMissingTxnsInThisRun(slasWithMissingTxns);		
		
		if (slasWithMissingTxns.isEmpty()  && slaTransactionResults.isEmpty() ){
			System.out.println( "Runcheck:  No transactionalSLA has been marked as failed, as recorded on the SLA Reference Database");
		} 
		
		metricSlaResults = new MetricSlaChecker().listFailedMetricSLAs(application, runTime, null, metricSlaDAO, transactionDAO);
		printMetricSlaResults(metricSlaResults);
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
	 * for testing purposes
	 * @return metricSlaResults
	 */
	public List<MetricSlaResult> getMetricSlaResults() {
		return metricSlaResults;
	}


	
	
}
