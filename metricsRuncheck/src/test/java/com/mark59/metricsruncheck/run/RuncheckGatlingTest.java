package com.mark59.metricsruncheck.run;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.mark59.core.utils.Mark59Constants;
import com.mark59.metrics.data.beans.EventMapping;
import com.mark59.metrics.data.beans.Run;
import com.mark59.metrics.data.beans.Transaction;
import com.mark59.metrics.metricSla.MetricSlaResult;
import com.mark59.metrics.sla.SlaTransactionResult;
import com.mark59.metricsruncheck.Runcheck;

import junit.framework.TestCase;


public class RuncheckGatlingTest extends TestCase {

	EmbeddedDatabase db; 
	ApplicationContext context;

	public void setUp() {
		db = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).setName("metricsmem;MODE=MySQL;").addScript("copyofschema.sql").build();
	}
	
	@Test
	public void testRuncheckGatling341GeneralTest() {
		Runcheck.parseArguments(new String[] { "-a", "DataHunter", "-i", "./src/test/resources/GatlingResults", "-l","simulation.logv341",
				"-d", Mark59Constants.H2MEM, "-s","metricsmem",	"-e","responseTimeInMillis|errormsgStartsWith2|errormsgStartsWith3",  "-t","GATLING"  });
		SpringApplication springApplication = new SpringApplication(Runcheck.class);
		springApplication.setWebApplicationType(WebApplicationType.NONE);
		springApplication.setBannerMode(Banner.Mode.OFF);	
		context = springApplication.run();
		
		Runcheck runcheck = (Runcheck) context.getBean("runcheck");	
		
		List<MetricSlaResult> metricSlaResults = runcheck.getMetricSlaResults();
		assertEquals(3, metricSlaResults.size() );
		for (MetricSlaResult metricSlaResult : metricSlaResults){
			// System.out.println("metricSlaRes>>" + metricSlaResult);
			if ("Total_Unused_Policy_Count".equals(metricSlaResult.getTxnId())){
				assertEquals ("txnId=Total_Unused_Policy_Count, metricTxnType=null, valueDerivation=Last, slaResultType=MISSING_SLA_TRANSACTION,"
						+ " messageText=Metric SLA Failed Warning  : no metric has been found but was expected for DATAPOINT Last on Total_Unused_Policy_Count"
						, metricSlaResult.toString());				
			} else if ("localhost".equals(metricSlaResult.getTxnId()) &&  "Average".equals(metricSlaResult.getValueDerivation())  ){
				assertEquals ("txnId=localhost, metricTxnType=null, valueDerivation=Average, slaResultType=MISSING_SLA_TRANSACTION,"
						+ " messageText=Metric SLA Failed Warning  : no metric has been found but was expected for CPU_UTIL Average on localhost"
						, metricSlaResult.toString());
			} else if ("localhost".equals(metricSlaResult.getTxnId()) &&  "PercentOver90".equals(metricSlaResult.getValueDerivation())  ){
				assertEquals ("txnId=localhost, metricTxnType=null, valueDerivation=PercentOver90, slaResultType=MISSING_SLA_TRANSACTION,"
						+ " messageText=Metric SLA Failed Warning  : no metric has been found but was expected for CPU_UTIL PercentOver90 on localhost"
						, metricSlaResult.toString());				
			} else {
				fail("Unexpected metric sla TransactionResult: " + metricSlaResult.getTxnId() );
			}
		}
		
		List<SlaTransactionResult> slaTransactionResults = runcheck.getSlaTransactionResults();
		assertEquals(4, slaTransactionResults.size() );
		for (SlaTransactionResult slaTransactionResult : slaTransactionResults){ 
			// System.out.println("slaRes>>" + slaTransactionResult);
			if ("DH_lifecycle_0100_deleteMultiplePolicies".equals(slaTransactionResult.getTxnId())){
				assertEquals ("txnId : DH_lifecycle_0100_deleteMultiplePolicies, passedAllSlas=false, foundSLAforTxnId=true, passed90thResponse=true, txn90thResponse=0.034, sla90thResponse=0.400,"
						+ " passed95thResponse=true, txn95thResponse=0.034, sla95thResponse=-1.000, passed99thResponse=true, txn99thResponse=0.034, sla99thResponse=-1.000,"
						+ " passedFailPercent=true, txnFailurePercent=0.000, slaFailurePercent=2.000, passedPassCount=false, txnPassCount=2, slaPassCount=30, slaPassCountVariancePercent=20.000"
						, slaTransactionResult.toString());				
			} else if ("DH_lifecycle_0299_sometimes_I_fail".equals(slaTransactionResult.getTxnId())){
				assertEquals ("txnId : DH_lifecycle_0299_sometimes_I_fail, passedAllSlas=false, foundSLAforTxnId=true, passed90thResponse=false, txn90thResponse=1.134, sla90thResponse=0.100,"
						+ " passed95thResponse=true, txn95thResponse=1.134, sla95thResponse=-1.000, passed99thResponse=true, txn99thResponse=1.134, sla99thResponse=-1.000,"
						+ " passedFailPercent=true, txnFailurePercent=0.000, slaFailurePercent=-1.000, passedPassCount=true, txnPassCount=16, slaPassCount=20, slaPassCountVariancePercent=40.000"
						, slaTransactionResult.toString());
			} else if ("DH_lifecycle_0300_countUnusedPolicies".equals(slaTransactionResult.getTxnId())){
				assertEquals ("txnId : DH_lifecycle_0300_countUnusedPolicies, passedAllSlas=false, foundSLAforTxnId=true, passed90thResponse=true, txn90thResponse=0.387, sla90thResponse=0.400,"
						+ " passed95thResponse=true, txn95thResponse=0.387, sla95thResponse=-1.000, passed99thResponse=true, txn99thResponse=0.387, sla99thResponse=-1.000,"
						+ " passedFailPercent=false, txnFailurePercent=40.000, slaFailurePercent=2.000, passedPassCount=false, txnPassCount=3, slaPassCount=20, slaPassCountVariancePercent=20.000"
						, slaTransactionResult.toString());
			} else if ("DH_lifecycle_0500_useNextPolicy".equals(slaTransactionResult.getTxnId())){
				assertEquals ("txnId : DH_lifecycle_0500_useNextPolicy, passedAllSlas=false, foundSLAforTxnId=true, passed90thResponse=false, txn90thResponse=0.781, sla90thResponse=0.400,"
						+ " passed95thResponse=true, txn95thResponse=1.167, sla95thResponse=-1.000, passed99thResponse=true, txn99thResponse=1.169, sla99thResponse=-1.000,"
						+ " passedFailPercent=true, txnFailurePercent=0.000, slaFailurePercent=2.000, passedPassCount=true, txnPassCount=19, slaPassCount=20, slaPassCountVariancePercent=20.000"
						, slaTransactionResult.toString());
			} else {
				fail("Unexpected slaTransactionResult: " + slaTransactionResult.getTxnId() );
			}
		}
		
		List<String> slasWithMissingTxns = runcheck.getSlasWithMissingTxns();
		assertEquals(1, slasWithMissingTxns.size());		
		assertEquals("DH_lifecycle_0200_addPolicy", slasWithMissingTxns.get(0));		
		
		PerformanceTest performanceTest = runcheck.getPerformanceTest();
		
		Run run = performanceTest.getRunSummary();
		assertEquals("DataHunter", run.getApplication());
		String apprun = StringUtils.substringBefore(run.toString(), "isRunIgnored");
		
		List<Transaction> transactions = performanceTest.getTransactionSummariesThisRun();
		assertEquals(7, transactions.size() );
		for (Transaction transaction : transactions) {
			// System.out.println("Txn>>" + transaction);
			if ("DH_lifecycle_0001_gotoDeleteMultiplePoliciesUrl".equals(transaction.getTxnId())){
				assertEquals (apprun +  "txnId=DH_lifecycle_0001_gotoDeleteMultiplePoliciesUrl, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=0.307, txnAverage=0.340, txnMedian=0.307,"
						+ " txnMaximum=0.372, txn90th=0.372, txn95th=0.372, txn99th=0.372, txnPass=2, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH_lifecycle_0100_deleteMultiplePolicies".equals(transaction.getTxnId())){
				assertEquals (apprun +  "txnId=DH_lifecycle_0100_deleteMultiplePolicies, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=0.032, txnAverage=0.033, txnMedian=0.032,"
						+ " txnMaximum=0.034, txn90th=0.034, txn95th=0.034, txn99th=0.034, txnPass=2, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH_lifecycle_0299_sometimes_I_fail".equals(transaction.getTxnId())){
				assertEquals (apprun +  "txnId=DH_lifecycle_0299_sometimes_I_fail, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=1.134, txnAverage=1.134, txnMedian=1.134,"
						+ " txnMaximum=1.134, txn90th=1.134, txn95th=1.134, txn99th=1.134, txnPass=16, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH_lifecycle_0300_countUnusedPolicies".equals(transaction.getTxnId())){
				assertEquals (apprun +  "txnId=DH_lifecycle_0300_countUnusedPolicies, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=0.025, txnAverage=0.264, txnMedian=0.381,"
						+ " txnMaximum=0.387, txn90th=0.387, txn95th=0.387, txn99th=0.387, txnPass=3, txnFail=2, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH_lifecycle_0500_useNextPolicy".equals(transaction.getTxnId())){
				assertEquals (apprun +  "txnId=DH_lifecycle_0500_useNextPolicy, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=0.280, txnAverage=0.427, txnMedian=0.283,"
						+ " txnMaximum=1.169, txn90th=0.781, txn95th=1.167, txn99th=1.169, txnPass=19, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("DH_lifecycle_9999_finalize_deleteMultiplePolicies".equals(transaction.getTxnId())){
				assertEquals (apprun +  "txnId=DH_lifecycle_9999_finalize_deleteMultiplePolicies, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=1.134, txnAverage=1.134, txnMedian=1.134,"
						+ " txnMaximum=1.134, txn90th=1.134, txn95th=1.134, txn99th=1.134, txnPass=4, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("load simulation file".equals(transaction.getTxnId())){
				assertEquals (apprun +  "txnId=load simulation file, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=0.093, txnAverage=0.093, txnMedian=0.093, txnMaximum=0.093, txn90th=0.093,"
						+ " txn95th=0.093, txn99th=0.093, txnPass=1, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());				
			} else {
				fail("unexpectedTransaction: " + transaction.getTxnId() );
			}
		}
		
		List<Transaction> metricTxns = runcheck.getPerformanceTest().getMetricTransactionSummariesThisRun();
		assertEquals(0, metricTxns.size() );
	}
	
	
	@Test
	public void testRuncheckGatling331Test() {
		Runcheck.parseArguments(new String[] { "-a", "junit331", "-i", "./src/test/resources/GatlingResults", "-l","simulation.logv331",
				"-d", Mark59Constants.H2MEM, "-s","metricsmem",	"-e","responseTimeInMillis|errormsgStartsWith2|errormsgStartsWith",  "-t","GATLING"  });
		SpringApplication springApplication = new SpringApplication(Runcheck.class);
		springApplication.setWebApplicationType(WebApplicationType.NONE);
		springApplication.setBannerMode(Banner.Mode.OFF);	
		context = springApplication.run();
		
		Runcheck runcheck = (Runcheck) context.getBean("runcheck");	
		assertEquals(0, runcheck.getMetricSlaResults().size());
		assertEquals(0, runcheck.getSlaTransactionResults().size() );
		assertEquals(0, runcheck.getSlasWithMissingTxns().size());		
		
		Run run = runcheck.getPerformanceTest().getRunSummary();
		assertEquals("junit331", run.getApplication());
		String apprun = StringUtils.substringBefore(run.toString(), "isRunIgnored");
		
		List<Transaction> transactions = runcheck.getPerformanceTest().getTransactionSummariesThisRun();
		assertEquals(2, transactions.size() );
		for (Transaction transaction : transactions) {
			// System.out.println("Txn>>" + transaction);
			if ("Get Info".equals(transaction.getTxnId())){
				assertEquals (apprun + "txnId=Get Info, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=0.220, txnAverage=0.220, txnMedian=0.220,"
						+ " txnMaximum=0.220, txn90th=0.220, txn95th=0.220, txn99th=0.220, txnPass=2, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("MGSX".equals(transaction.getTxnId())){
				assertEquals (apprun + "txnId=MGSX, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=0.020, txnAverage=0.020, txnMedian=0.020,"
						+ " txnMaximum=0.020, txn90th=0.020, txn95th=0.020, txn99th=0.020, txnPass=1, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else {
				fail("unexpectedTransaction: " + transaction.getTxnId() );
			}
		}
	}
	
	
	@Test
	public void testRuncheckGatling351andMockupDatapointandRuntimesTest() {
		EventMapping eventMapping = new EventMapping();
		eventMapping.setTxnType("DATAPOINT");
		eventMapping.setPerformanceTool("Gatling");
		eventMapping.setMetricSource("Gatling_TRANSACTION");
		eventMapping.setMatchWhenLike("dpmock%");
		eventMapping.setTargetNameLB("");
		eventMapping.setTargetNameRB("");
		eventMapping.setIsPercentage("N");
		eventMapping.setIsInvertedPercentage("N");
		eventMapping.setComment(""); 
		
        JdbcTemplate jdbcTemplate = new JdbcTemplate(db);
		String sql = "INSERT INTO EVENTMAPPING (TXN_TYPE, PERFORMANCE_TOOL, METRIC_SOURCE, MATCH_WHEN_LIKE, TARGET_NAME_LB, TARGET_NAME_RB,"
				+ " IS_PERCENTAGE, IS_INVERTED_PERCENTAGE, COMMENT) VALUES (?,?,?,?,?,?,?,?,?)";	
		jdbcTemplate.update(sql, eventMapping.getTxnType(),eventMapping.getPerformanceTool(), eventMapping.getMetricSource(), eventMapping.getMatchWhenLike(), eventMapping.getTargetNameLB(), eventMapping.getTargetNameRB(),
				eventMapping.getIsPercentage(), eventMapping.getIsInvertedPercentage(), eventMapping.getComment());
		
		Runcheck.parseArguments(new String[] { "-a", "junit351", "-i", "./src/test/resources/GatlingResults", "-l","simulation.logv351",
				"-d", Mark59Constants.H2MEM, "-s","metricsmem",	"-t","GATLING"  });
		SpringApplication springApplication = new SpringApplication(Runcheck.class);
		springApplication.setWebApplicationType(WebApplicationType.NONE);
		springApplication.setBannerMode(Banner.Mode.OFF);	
		context = springApplication.run();
		
		Runcheck runcheck = (Runcheck) context.getBean("runcheck");	
		assertEquals(0, runcheck.getMetricSlaResults().size());
		assertEquals(0, runcheck.getSlaTransactionResults().size() );
		assertEquals(0, runcheck.getSlasWithMissingTxns().size());		
			
		Run run = runcheck.getPerformanceTest().getRunSummary();
		assertEquals("junit351", run.getApplication());
		assertEquals("1622693982008", StringUtils.substringBetween(run.getPeriod(), "[", ":" ).trim());
		assertEquals("1622693982162", StringUtils.substringBetween(run.getPeriod(), ":", "]" ).trim());
		String apprun = StringUtils.substringBefore(run.toString(), "isRunIgnored");
		
		List<Transaction> transactions = runcheck.getPerformanceTest().getTransactionSummariesThisRun();
		assertEquals(1, transactions.size() );
		for (Transaction transaction : transactions) {
			// System.out.println("Txn>>" + transaction);
			if ("Get trash List".equals(transaction.getTxnId())){
				assertEquals (apprun + "txnId=Get trash List, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=0.057, txnAverage=0.096, txnMedian=0.057,"
						+ " txnMaximum=0.134, txn90th=0.134, txn95th=0.134, txn99th=0.134, txnPass=2, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else {
				fail("unexpectedTransaction: " + transaction.getTxnId() );
			}
		}
		
		List<Transaction> metricTxns = runcheck.getPerformanceTest().getMetricTransactionSummariesThisRun();
		assertEquals(1, metricTxns.size() );
		for (Transaction transaction : metricTxns) {
			System.out.println("metricTxns>>" + transaction);
			if ("dpmockupmetric".equals(transaction.getTxnId())){
				assertEquals (apprun + "txnId=dpmockupmetric, txnType=DATAPOINT, isCdpTxn=N, txnMinimum=0.134, txnAverage=0.134, txnMedian=-1.000,"
						+ " txnMaximum=0.134, txn90th=-1.000, txn95th=-1.000, txn99th=-1.000, txnPass=1, txnFail=-1, txnStop=-1, txnFirst=0.134, txnLast=0.134, txnSum=0.134, txnDelay=0.000", transaction.toString());
			} else {
				fail("unexpectedTransaction: " + transaction.getTxnId() );
			}
		}
	}

	
	@Test
	public void testRuncheckGatling360Test() {
		Runcheck.parseArguments(new String[] { "-a", "junit360", "-i", "./src/test/resources/GatlingResults", "-l","simulation.logv360",
				"-d", Mark59Constants.H2MEM, "-s","metricsmem",	"-e","responseTimeInMillis",  "-t","GATLING"  });
		SpringApplication springApplication = new SpringApplication(Runcheck.class);
		springApplication.setWebApplicationType(WebApplicationType.NONE);
		springApplication.setBannerMode(Banner.Mode.OFF);	
		context = springApplication.run();
		
		Runcheck runcheck = (Runcheck) context.getBean("runcheck");	
		assertEquals(0, runcheck.getMetricSlaResults().size());
		assertEquals(0, runcheck.getSlaTransactionResults().size() );
		assertEquals(0, runcheck.getSlasWithMissingTxns().size());		
		
		Run run = runcheck.getPerformanceTest().getRunSummary();
		assertEquals("junit360", run.getApplication());
		String apprun = StringUtils.substringBefore(run.toString(), "isRunIgnored");
		
		List<Transaction> transactions = runcheck.getPerformanceTest().getTransactionSummariesThisRun();
		assertEquals(2, transactions.size() );
		for (Transaction transaction : transactions) {
			// System.out.println("Txn>>" + transaction);
			if ("Get Single trash Record".equals(transaction.getTxnId())){
				assertEquals (apprun + "txnId=Get Single trash Record, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=0.057, txnAverage=0.057, txnMedian=0.057,"
						+ " txnMaximum=0.057, txn90th=0.057, txn95th=0.057, txn99th=0.057, txnPass=1, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("Get trash List".equals(transaction.getTxnId())){
				assertEquals (apprun + "txnId=Get trash List, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=0.265, txnAverage=0.265, txnMedian=0.265,"
						+ " txnMaximum=0.265, txn90th=0.265, txn95th=0.265, txn99th=0.265, txnPass=1, txnFail=1, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else {
				fail("unexpectedTransaction: " + transaction.getTxnId() );
			}
		}
	}
	
	
	@Test
	public void testRuncheckGatling900CustomTest() {
		Runcheck.parseArguments(new String[] { "-a", "junit900", "-i", "./src/test/resources/GatlingResults", "-l","simulation.log900Custom",
				"-d", Mark59Constants.H2MEM, "-s","metricsmem",	"-m","3,4,5,6,7",  "-t","GATLING"  });
		SpringApplication springApplication = new SpringApplication(Runcheck.class);
		springApplication.setWebApplicationType(WebApplicationType.NONE);
		springApplication.setBannerMode(Banner.Mode.OFF);	
		context = springApplication.run();
		
		Runcheck runcheck = (Runcheck) context.getBean("runcheck");	
		assertEquals(0, runcheck.getMetricSlaResults().size());
		assertEquals(0, runcheck.getSlaTransactionResults().size() );
		assertEquals(0, runcheck.getSlasWithMissingTxns().size());	
		
		Run run = runcheck.getPerformanceTest().getRunSummary();
		assertEquals("junit900", run.getApplication());
		String apprun = StringUtils.substringBefore(run.toString(), "isRunIgnored");
		
		List<Transaction> transactions = runcheck.getPerformanceTest().getTransactionSummariesThisRun();
		assertEquals(2, transactions.size() );
		for (Transaction transaction : transactions) {
			// System.out.println("Txn>>" + transaction);
			if ("Get Single trash Record".equals(transaction.getTxnId())){
				assertEquals (apprun + "txnId=Get Single trash Record, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=0.057, txnAverage=0.057, txnMedian=0.057,"
						+ " txnMaximum=0.057, txn90th=0.057, txn95th=0.057, txn99th=0.057, txnPass=1, txnFail=0, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else if ("Get trash List".equals(transaction.getTxnId())){
				assertEquals (apprun + "txnId=Get trash List, txnType=TRANSACTION, isCdpTxn=N, txnMinimum=0.265, txnAverage=0.265, txnMedian=0.265,"
						+ " txnMaximum=0.265, txn90th=0.265, txn95th=0.265, txn99th=0.265, txnPass=1, txnFail=1, txnStop=0, txnFirst=-1.000, txnLast=-1.000, txnSum=-1.000, txnDelay=0.000", transaction.toString());
			} else {
				fail("unexpectedTransaction: " + transaction.getTxnId() );
			}
		}
	}
	
}
