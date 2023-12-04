package com.mark59.trends.load.run;


import static org.junit.Assert.assertThrows;

import org.junit.Test;

import com.mark59.core.utils.Mark59Constants;
import com.mark59.trends.load.TrendsLoad;
import junit.framework.TestCase;


public class TrendsLoadArgumentValidations extends TestCase {
	
	@Test
	public void testTrendsLoadValidateApplicationNameTest() {

		assertThrows("Invalid app name not rejected: Data$Hunter",RuntimeException.class,
	    	()->{TrendsLoad.parseArguments(new String[]{"-a","Data$Hunter", "-i","./src/test/resources/JmeterResultsDataHunterGeneral", "-d",Mark59Constants.H2MEM, "-s","trendsmem"});});	
		
		assertThrows("Invalid app name not rejected: Data$Hunter",RuntimeException.class,
		    ()->{TrendsLoad.parseArguments(new String[]{"-a","Da//unter", "-i","./src/test/resources/JmeterResultsDataHunterGeneral", "-d",Mark59Constants.H2MEM, "-s","trendsmem"});});	
   
	    assertThrows("Invalid app name not rejected: (empty)",RuntimeException.class,
		    ()->{TrendsLoad.parseArguments(new String[]{"-a","", "-i","./src/test/resources/JmeterResultsDataHunterGeneral", "-d",Mark59Constants.H2MEM, "-s","trendsmem"});});		
	    
	    assertThrows("Invalid app name not rejected: Data Hunter",RuntimeException.class,
		    ()->{TrendsLoad.parseArguments(new String[]{"-a","Data Hunter", "-i","./src/test/resources/JmeterResultsDataHunterGeneral", "-d",Mark59Constants.H2MEM, "-s","trendsmem"});});		
	
	    TrendsLoad.parseArguments(new String[]{"-a","DataHunter", "-i","./src/test/resources/JmeterResultsDataHunterGeneral", "-d",Mark59Constants.H2MEM, "-s","trendsmem"});		
	    TrendsLoad.parseArguments(new String[]{"-a","D.t_u--e.r", "-i","./src/test/resources/JmeterResultsDataHunterGeneral", "-d",Mark59Constants.H2MEM, "-s","trendsmem"});		
	}
	
}
