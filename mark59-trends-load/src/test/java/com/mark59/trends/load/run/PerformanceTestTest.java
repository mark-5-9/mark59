package com.mark59.trends.load.run;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

import com.mark59.trends.data.application.dao.ApplicationDAO;
import com.mark59.trends.data.application.dao.ApplicationDAOjdbcTemplateImpl;
import com.mark59.trends.data.beans.DateRangeBean;
import com.mark59.trends.data.beans.Run;
import com.mark59.trends.data.eventMapping.dao.EventMappingDAO;
import com.mark59.trends.data.eventMapping.dao.EventMappingDAOjdbcTemplateImpl;
import com.mark59.trends.data.graphMapping.dao.GraphMappingDAO;
import com.mark59.trends.data.graphMapping.dao.GraphMappingDAOjdbcTemplateImpl;
import com.mark59.trends.data.metricSla.dao.MetricSlaDAO;
import com.mark59.trends.data.metricSla.dao.MetricSlaDAOjdbcImpl;
import com.mark59.trends.data.run.dao.RunDAO;
import com.mark59.trends.data.run.dao.RunDAOjdbcTemplateImpl;
import com.mark59.trends.data.sla.dao.SlaDAO;
import com.mark59.trends.data.sla.dao.SlaDAOjdbcImpl;
import com.mark59.trends.data.testTransactions.dao.TestTransactionsDAO;
import com.mark59.trends.data.testTransactions.dao.TestTransactionsDAOjdbcTemplateImpl;
import com.mark59.trends.data.transaction.dao.TransactionDAO;
import com.mark59.trends.data.transaction.dao.TransactionDAOjdbcTemplateImpl;

import junit.framework.TestCase;

@Profile("ForceSpringBeanConfigurationUniqueForThisTest")
@Configuration
 public class PerformanceTestTest extends TestCase {

	@Autowired
	ApplicationContext context;
	
	@Bean
	DataSource dataSource() {
		return DataSourceBuilder.create().build()  ;
	};
		
    @Value("h2mem")
    String springProfilesActive;	
	@Bean
    String currentDatabaseProfile() {return "h2mem";   }   
	@Bean
	ApplicationDAO applicationDAO() {return new ApplicationDAOjdbcTemplateImpl();}
	@Bean
	RunDAO runDAO() {return new RunDAOjdbcTemplateImpl();}
	@Bean
	TransactionDAO transactionDAO() {return new TransactionDAOjdbcTemplateImpl();}
	@Bean
	SlaDAO slaDAO() {return new SlaDAOjdbcImpl();}
	@Bean
	MetricSlaDAO metricSlaDAO() {return new MetricSlaDAOjdbcImpl();}
	@Bean
	GraphMappingDAO graphMappingDAO() {	return new GraphMappingDAOjdbcTemplateImpl();}
	@Bean
	EventMappingDAO eventMappingDAO() {	return new EventMappingDAOjdbcTemplateImpl();}
	@Bean
	TestTransactionsDAO testTransactionsDAO() {return new TestTransactionsDAOjdbcTemplateImpl();	}
		
	PerformanceTest performanceTest;
	EmbeddedDatabase db; 
	
	public void setUp() {
		String applicationId = "testApplicationId";
		String runReferenceArg = "testRunReferenceArg";
		System.setProperty("spring.profile", "ForceSpringBeanConfigurationUniqueForThisTest");
		SpringApplication springApplication = new SpringApplication(PerformanceTestTest.class);
		springApplication.setWebApplicationType(WebApplicationType.NONE);
		springApplication.setBannerMode(Banner.Mode.OFF);
		context = springApplication.run();
		performanceTest = new PerformanceTest(context, applicationId, runReferenceArg);	
	}
	
	@Test
	public void testPerformanceTestRunSummaryTest() {
		Run run = performanceTest.getRunSummary();
		assertTrue("testApplicationId".equals(run.getApplication()));
		assertTrue("testRunReferenceArg".equals(run.getRunReference()));
		assertTrue("N".equals(run.getBaselineRun()));
	}
	
	@Test
	public void testPerformanceTestCalculateAndSetRunTimesUsingEpochStartAndEndTest() {
		DateRangeBean dateRangeBean= new DateRangeBean(1600000000000L, 1610000000000L);
		Run run = performanceTest.getRunSummary();
		run = performanceTest.calculateAndSetRunTimesUsingEpochStartAndEnd(run, dateRangeBean);
		assertTrue("testApplicationId".equals(run.getApplication()));
		assertTrue("testRunReferenceArg".equals(run.getRunReference()));
		assertTrue("N".equals(run.getBaselineRun()));
		assertTrue("166666".equals(run.getDuration()));
	}
	
}
