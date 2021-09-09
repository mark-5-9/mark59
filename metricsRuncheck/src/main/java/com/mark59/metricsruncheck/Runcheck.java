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
import com.mark59.core.utils.Mark59Utils;
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
import com.mark59.metricsruncheck.run.GatlingRun;
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

	
	private static String argApplication;
	private static String argInput;
	private static String argDatabasetype;
	private static String argReference;
	private static String argTool;
	private static String argExcludestart;
	private static String argCaptureperiod;
	private static String argIgnoredErrors;
	private static String argSimulationLog;
	private static String argKeeprawresults;
	private static String argSimlogCustom;
	private static String argTimeZone;

	private PerformanceTest performanceTest;
	private List<MetricSlaResult> metricSlaResults = new ArrayList<MetricSlaResult>();
	private List<SlaTransactionResult> cdpTaggedTransactionsWithFailedSlas = new ArrayList<SlaTransactionResult>();
	private List<String> cdpTaggedMissingTransactions = new ArrayList<String>(); 
	
	public static void parseArguments(String[] args) {
		Options options = new Options(); 
		options.addRequiredOption("a", "application",	true, "Application Id, as it will appear in the Trending Graph Application dropdown selections");
		options.addRequiredOption("i", "input",			true, "The directory or file containing the performance test results.  Multiple xml/csv/jtl results files allowed for JMeter within a directory,"
																+ " a single .mdb file is required for Loadrunner");			
		options.addOption("d", "databasetype",			true, "Load data to a 'h2', 'pg' or 'mysql' database (defaults to 'mysql')");			
		options.addOption("r", "reference",		 		true, "A reference.  Usual purpose would be to identify this run (possibly by a link). Eg <a href='http://ciServer/job/myJob/001/HTML_Report'>run 001</a>");
		options.addOption("t", "tool",      			true, "Performance Tool used to generate the results to be processed { JMETER (default) | GATLING | LOADRUNNER }" );
		options.addOption("h", "dbserver",				true, "Server hosting the database where results will be held (defaults to localhost). \n"
																+ "*******************************************\n"
																+ "** NOTE: all db options applicable to MySQL or Postgres ONLY\n"  
																+ "*******************************************");
		options.addOption("p", "dbPort",    			true, "Port number for the database where results will be held (defaults to 3306 for MySQL, 5432 for Postgres, 9902 for H2 tcp)" );		
		options.addOption("s", "dbSchema",				true, "database schema (MySQL terminology) / database name (Postgres terminology) defaults to metricsdb" );
		options.addOption("u", "dbUsername",   			true, "Username for the database (defaults to admin)" );				
		options.addOption("w", "dbpassWord",   			true, "Password for the database" );				
		options.addOption("y", "dbpassencrYpted",		true, "Encrypted Password for the database (value as per the encryption used by mark59-server-metrics-web application 'Edit Server Profile' page)");				
		options.addOption("q", "dbxtraurlparms",		true, "Any special parameters to append to the end of the database URL (include the ?). Eg \"?allowPublicKeyRetrieval=truee&useSSL=false\" "
																+ "(the quotes are needed to escape the ampersand)");				
		options.addOption("x", "eXcludestart",     		true, "exclude results at the start of the test for the given number of minutes (defaults to 0)" );			
		options.addOption("c", "captureperiod",    		true, "Only capture test results for the given number of minutes, from the excluded start period "
																+ "(default is all results except those skipped by the excludestart parm are included)" );			
		options.addOption("k", "keeprawresults", 		true, "Keep Raw Test Results. If 'true' will keep each transaction for each run in the database (System metrics data is not captured for Loadrunner). "
																+ "This can use a large amount of storage and is not recommended (defaults to false).");
		options.addOption("e", "ignoredErrors",    		true, "Gatling, JMeter(csv) only. A list of pipe (|) delimited strings.  When an error msg starts with any of the strings in the list, "
																+ "it will be treated as a Passed transaction rather than an Error." );	
		options.addOption("l", "simulationLog",			true, "Gatling only. Simulation log file name - must be in the Input directory (defaults to simulation.log)" );
		options.addOption("m", "simlogcustoM",			true, "Gatling only. Simulation log comma-separated customized 'REQUEST' field column positions in order : txn name, epoch start, epoch end, tnx OK, error msg. "
																+ "The text 'REQUEST' is assumed in position 1. EG: for a 3.6.1 layout: '2,3,4,5,6,' (This parameter may assist with un-catered for Gatling versions)" );
		options.addOption("z", "timeZone",    			true, "Loadrunner only. Required when running ann extract from azone other than where the Analysis Report was generated. Also, internal raw stored time"
																+ " may not take daylight saving into account.  Two format options 1) offset against GMT. Eg 'GMT+02:00' or 2) IANA Time Zone Database (TZDB) codes."
																+ " Refer to https://en.wikipedia.org/wiki/List_of_tz_database_time_zones. Eg 'Australia/Sydney' ");   	
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
		argIgnoredErrors	= commandLine.getOptionValue("e", "");				
		argSimulationLog	= commandLine.getOptionValue("l", "simulation.log");				
		argSimlogCustom		= commandLine.getOptionValue("m", "");				
		argKeeprawresults	= commandLine.getOptionValue("k", String.valueOf(false));
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
		if ( !AppConstantsMetrics.JMETER.equalsIgnoreCase(argTool)  &&  !AppConstantsMetrics.LOADRUNNER.equalsIgnoreCase(argTool) && !AppConstantsMetrics.GATLING.equalsIgnoreCase(argTool) ) {
			formatter.printHelp( "Runcheck", options );
			printSampleUsage();
			throw new RuntimeException("The tool (t) argument must be set to JMETER or LOADRUNNER or GATLING ! (or not used, in which case JMETER is assumed)");  
		}
		if ( !String.valueOf(false).equalsIgnoreCase(argKeeprawresults)  &&  !String.valueOf(true).equalsIgnoreCase(argKeeprawresults)) {
			formatter.printHelp( "Runcheck", options );
			printSampleUsage();
			throw new RuntimeException("The Keeprawresults (k) argument must be set to 'true' or 'false' ! (or not used, in which case 'false' is assumed)");  
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
		if (StringUtils.isNotBlank(argSimlogCustom)){
				List<String> mPos = Mark59Utils.commaDelimStringToStringList(argSimlogCustom); 
				if (mPos.size() != 5  ||
					mPos.size() == 5 && ( !StringUtils.isNumeric(mPos.get(0)) || !StringUtils.isNumeric(mPos.get(1)) ||
							              !StringUtils.isNumeric(mPos.get(2)) || !StringUtils.isNumeric(mPos.get(3)) ||
							              !StringUtils.isNumeric(mPos.get(4)) )) {
				formatter.printHelp( "Runcheck", options );
				printSampleUsage();
				throw new RuntimeException("The simlogcustoM (m) parameter must blank or 5 comma-delimited integers") ;  
			}
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
		System.out.println(" ignoredErrors  : " + argIgnoredErrors );		
		System.out.println(" simulationlog  : " + argSimulationLog );		
		System.out.println(" simlogcustoM   : " + argSimlogCustom );		
		System.out.println(" keeprawresults : " + argKeeprawresults );		
		System.out.println(" timeZone       : " + argTimeZone );		
		System.out.println("------------------------------------------------   " );				    
		System.out.println();
	}

	public static void printSampleUsage() {
		System.out.println();	
		System.out.println( "Sample usages");
		System.out.println( "------------");
		System.out.println( "   1. JMeter example ");
		System.out.println( "   Process JMeter xml formatted result in directory C:/jmeter-results/BIGAPP  (file/s ends in .xml)");
		System.out.println( "   The graph application name will be MY_COMPANY_BIG_APP, with a reference for this run of 'run ref 645'.");
		System.out.println( "   The metricsdb database is hosted locally on a MySql instance assigned to port 3309 (default user/password of admin/admin) : "  );
		System.out.println( "   java -jar metricsRuncheck.jar -a MY_COMPANY_BIG_APP -i C:/jmeter-results/BIGAPP -r \"run ref 645\" -p 3309  ");
		System.out.println( "   2. Gatling example ");
		System.out.println( "   Process Gatling simulation.log in directory C:/GatlingProjects/myBigApp");
		System.out.println( "   The graph application name will be MY_COMPANY_BIG_APP, with a reference for this run of 'GatlingIsCool'.");
		System.out.println( "   The metricsdb database is hosted locally on a Postgres instance using all defaults (but you want to disable sslmode) "  );
		System.out.println( "   java -jar metricsRuncheck.jar -a MY_COMPANY_BIG_APP -i C:/GatlingProjects/myBigApp -d pg -q \"?sslmode=disable\" -t GATLING  -r \"GatlingIsCool\" ");		
		System.out.println( "   3. Loadrunner example");		
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
		
		if (String.valueOf(true).equalsIgnoreCase(argKeeprawresults)) {
			System.out.println();			
			System.out.println("***************************************************************************************" );
			System.out.println("*   Setting 'keeprawresults' to 'true' may use a large amount of storage.             *" );
			System.out.println("*   The data is not used within the Mark59 framework.  The option has been made       *" );
			System.out.println("*   available for users who may wish to use it externally for data analysis/reporting *" );
			System.out.println("*   of JMeter results.                                                                *" );
			System.out.println("***************************************************************************************" );
			System.out.println();			
		}
		
		loadTestRun(argTool, argApplication, argInput, argReference, argExcludestart, argCaptureperiod, argKeeprawresults,
				argTimeZone, argIgnoredErrors, argSimulationLog, argSimlogCustom);
	};

	
	public void loadTestRun(String tool, String application, String input, String runReference, String excludestart, String captureperiod, String keeprawresults,  
			String timeZone, String ignoredErrors, String simulationLog, String simlogCustom) throws IOException {
		
		if (AppConstantsMetrics.JMETER.equalsIgnoreCase(tool)){		
			performanceTest = new JmeterRun(context, application, input, runReference, excludestart, captureperiod, keeprawresults, ignoredErrors );
		} else if (AppConstantsMetrics.GATLING.equalsIgnoreCase(tool)){	
			performanceTest = new GatlingRun(context, application, input, runReference, excludestart, captureperiod, keeprawresults, ignoredErrors, simulationLog, simlogCustom);
		} else { 
			performanceTest = new LrRun(context, application, input, runReference, excludestart, captureperiod, keeprawresults, timeZone );
		}
		
		cdpTaggedTransactionsWithFailedSlas = new SlaChecker().listCdpTaggedTransactionsWithFailedSlas(application, performanceTest.getTransactionSummariesThisRun(), slaDAO);
		printTransactionalMetricSlaResults(cdpTaggedTransactionsWithFailedSlas);
		
		String runTime = performanceTest.getRunSummary().getRunTime(); 

		cdpTaggedMissingTransactions  =  new SlaChecker().checkForMissingTransactionsWithDatabaseSLAs(application, runTime, slaDAO  ); 
		printSlasWitMissingTxnsInThisRun(cdpTaggedMissingTransactions);		
		
		if (cdpTaggedMissingTransactions.isEmpty()  && cdpTaggedTransactionsWithFailedSlas.isEmpty() ){
			System.out.println( "Runcheck:  No transactional SLA has failed (as recorded on the SLA Reference Database)");
		} 
		
		metricSlaResults = new MetricSlaChecker().listFailedMetricSLAs(application, runTime, null, metricSlaDAO, transactionDAO);
		printMetricSlaResults(metricSlaResults);
	}
	

	private void printSlasWitMissingTxnsInThisRun(List<String> cdpTaggedMissingTransactions) {
		for (String slaWithMissingTxn : cdpTaggedMissingTransactions) {
    		System.out.println( "Runcheck: SLA Failed : Error : an SLA exists for transaction " + slaWithMissingTxn + ", but that transaction does not appear in the run results ! "  );			
		}
	}

	
	private Boolean printTransactionalMetricSlaResults(List<SlaTransactionResult> cdpTaggedTransactionsWithFailedSlas) {
		Boolean allPassed = true; 
		for (SlaTransactionResult slaTransactionResult : cdpTaggedTransactionsWithFailedSlas) {
			if ( !slaTransactionResult.isPassedAllSlas()){
				allPassed = false;
			}

			if ( !slaTransactionResult.isPassed90thResponse()){
	    		System.out.println( "Runcheck: SLA Failed Warning  : " + slaTransactionResult.getTxnId() + " has failed it's 90th Percentile Response Time SLA as recorded on the SLA database ! "  );
	    		System.out.println( "                      response was " + slaTransactionResult.getTxn90thResponse() + " secs, SLA of " +  slaTransactionResult.getSla90thResponse());				
				
			}
			if ( !slaTransactionResult.isPassed95thResponse()){
	    		System.out.println( "Runcheck: SLA Failed Warning  : " + slaTransactionResult.getTxnId() + " has failed it's 95th Percentile Response Time SLA as recorded on the SLA database ! "  );
	    		System.out.println( "                      response was " + slaTransactionResult.getTxn95thResponse() + " secs, SLA of " +  slaTransactionResult.getSla95thResponse());				
				
			}
			if ( !slaTransactionResult.isPassed99thResponse()){
	    		System.out.println( "Runcheck: SLA Failed Warning  : " + slaTransactionResult.getTxnId() + " has failed it's 99th Percentile Response Time SLA as recorded on the SLA database ! "  );
	    		System.out.println( "                      response was " + slaTransactionResult.getTxn99thResponse() + " secs, SLA of " +  slaTransactionResult.getSla99thResponse());				
				
			}
			if ( !slaTransactionResult.isPassedFailPercent()){
	    		System.out.println( "Runcheck: SLA Failed : Error : " + slaTransactionResult.getTxnId() + " has failed it's % Error Rate SLA as recorded on the SLA database ! "  );
	    		System.out.println( "                      % error was " + new DecimalFormat("#.##").format(slaTransactionResult.getTxnFailurePercent()) + 
	    				" %, SLA of " +  slaTransactionResult.getSlaFailurePercent());				
			}
			if ( !slaTransactionResult.isPassedFailCount()){
	    		System.out.println( "Runcheck: SLA Failed : Error : " + slaTransactionResult.getTxnId() + " has failed it's Fail Count SLA as recorded on the SLA database ! "  );
	    		System.out.println( "                      count was " + slaTransactionResult.getTxnFailCount() + 
	    				" Fail Count SLA is " +  slaTransactionResult.getSlaFailCount());				
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
			System.out.println( "Runcheck:  No metric SLA has failed (as recorded on the SLA Metrics Reference Database)");
		}
	}

	
	/**
	 * for testing purposes
	 * @return performanceTest
	 */
	public PerformanceTest getPerformanceTest(){
		return performanceTest;
	}	
	/**
	 * for testing purposes
	 * @return metricSlaResults
	 */
	public List<SlaTransactionResult> getSlaTransactionResults() {
		return cdpTaggedTransactionsWithFailedSlas;
	}
	/**
	 * for testing purposes
	 * @return metricSlaResults
	 */
	public List<MetricSlaResult> getMetricSlaResults() {
		return metricSlaResults;
	}
	/**
	 * for testing purposes
	 * @return metricSlaResults
	 */
	public List<String> getSlasWithMissingTxns() {
		return cdpTaggedMissingTransactions;
	}
	
}
