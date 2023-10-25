package com.mark59.datahunter.test;

import java.io.IOException;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.mark59.datahunter.application.DataHunterConstants;
import com.mark59.datahunter.controller.UploadPoliciesFileController;
import com.mark59.datahunter.data.beans.Policies;
import com.opencsv.CSVParser;

import junit.framework.TestCase;


public class DataHunterUploadTests extends TestCase {	
	
	@Test
	public void checkDataHunterUploadCreatePolicy() throws IOException {
		UploadPoliciesFileController upfc = new UploadPoliciesFileController();
		Policies p = new Policies();
		
		p=upfc.createPolicy(" app ,id01,lf,USED,other,1");
		assertsOnPolicy(new Policies("app","id01","lf","USED","other",1L), p);
		p=upfc.createPolicy(" app ,	id01  ,		lf ,	USED,,	2");
		assertsOnPolicy(new Policies("app","id01","lf","USED","",2L), p);
		
		p=upfc.createPolicy(" app ,	','',USED,1and2squotest,3");
		assertsOnPolicy(new Policies("app","'","''","USED","1and2squotest",3L), p);
		p=upfc.createPolicy(" app ,','',USED,1and2squotest,3");
		assertsOnPolicy(new Policies("app","'","''","USED","1and2squotest",3L), p);		
		p=upfc.createPolicy(" app ,',' ',USED,1and2squotest,3");
		assertsOnPolicy(new Policies("app","'","' '","USED","1and2squotest",3L), p);			
		
		p=upfc.createPolicy(" app ,\"x\",'',USED,o',3");  
		assertsOnPolicy(new Policies("app","x","''","USED","o'",3L), p);
		p=upfc.createPolicy(" app ,\" \",'',USED,o\',3"); 
		assertsOnPolicy(new Policies("app","","''","USED","o\'",3L), p);
		p=upfc.createPolicy(" app ,\"\"s,'',USED,''',3");
		assertsOnPolicy(new Policies("app","\"s","''","USED","'''",3L), p);		
		p=upfc.createPolicy(" app ,\"\"s,'',USED,suf,3");
		assertsOnPolicy(new Policies("app","\"s","''","USED","suf",3L), p);		
		p=upfc.createPolicy(" app ,s\"\",'',USED,pre,3");
		assertsOnPolicy(new Policies("app","s\"","''","USED","pre",3L), p);	
		
		//  have to use csvParser.parseToLine(new String[]{...}, true
		CSVParser csvParser = new CSVParser();
		String csvItemLine = csvParser.parseToLine(new String[]{"app","\"\"","''","USED","pre","7"}, true);
		p=upfc.createPolicy(csvItemLine);
		assertsOnPolicy(new Policies("app","\"\"","''","USED","pre",7L), p);	

		p=upfc.createPolicy("\"app\",\"\"\"\"\"\",\"''\",\"USED\",\"pre\",\"7\"");
		assertsOnPolicy(new Policies("app","\"\"","''","USED","pre",7L), p);
	
		csvItemLine = csvParser.parseToLine(new String[]{"app","s\"","''","USED","pre","6"}, true);		
		p=upfc.createPolicy(csvItemLine);
		assertsOnPolicy(new Policies("app","s\"","''","USED","pre",6L), p);	
	}
	
	
	@Test
	public void checkDataHunterUploadApplication() throws IOException {

		int maxErr = UploadPoliciesFileController.MAX_ERRORS_REPORTED;
		UploadPoliciesFileController upfc = new UploadPoliciesFileController();
		
		String line = "testapi1,imx1,,USED,,1696070184895,toomany";
		Assert.assertEquals("<br>line 0000002 [testapi1,imx1,,USED,,1696070184895,toomany] expected 6 columns, but found 7", 
				upfc.validLineOfPolicyData(line, maxErr-1, 2));
		
		line = "testapi1,im2,,USED,not emught";
		Assert.assertEquals("<br>line 0000003 [testapi1,im2,,USED,not emught] expected 6 columns, but found 5", 
				upfc.validLineOfPolicyData(line, maxErr-1, 3));

		line = "testapi1,im3,duplicatedid,DUDSTATE,duplicated id,1234";
		Assert.assertEquals("<br>line 0000004 [testapi1,im3,duplicatedid,DUDSTATE,duplicated id,1234] The 'USEABILITY' "
				+ "value must be one of [REUSABLE, UNPAIRED, UNUSED, USED]", 
				upfc.validLineOfPolicyData(line, maxErr-1, 4));
		
		line = ",,,,,";
		Assert.assertEquals("<br>line 0000004 [,,,,,] A line of all blank values is not considered valid", 
				upfc.validLineOfPolicyData(line, maxErr-1, 4));

		line = " ,im4,nonblanklc,USED,,1696070184907";
		Assert.assertEquals("<br>line 0000004 [ ,im4,nonblanklc,USED,,1696070184907]"
				+ " Loading a blank 'APPLICATION' is not considered valid", 
				upfc.validLineOfPolicyData(line, maxErr-1, 4));

		line = " ValidApp ,im4,nonblanklc,USED,,1696070184907";
		Assert.assertEquals(DataHunterConstants.OK,	upfc.validLineOfPolicyData(line, maxErr-1, 4));
		
		line = "testapix,im2,,USED,atmaxerr";
		Assert.assertEquals("<br><b>... more than 50 invalid lines of data found</b>",	upfc.validLineOfPolicyData(line, maxErr, 5));

		line = "testapix,im2,,USED,atmaxerr+1";
		Assert.assertEquals("",	upfc.validLineOfPolicyData(line, maxErr+1, 5));
	}
	
	private void assertsOnPolicy(Policies expectedPolicy, Policies actualPolicy) {
		assertEquals(expectedPolicy.getApplication(), actualPolicy.getApplication()); 
		assertEquals(expectedPolicy.getIdentifier(), actualPolicy.getIdentifier()); 
		assertEquals(expectedPolicy.getLifecycle(), actualPolicy.getLifecycle()); 
		assertEquals(expectedPolicy.getUseability(), actualPolicy.getUseability()); 
		assertEquals(expectedPolicy.getOtherdata(), actualPolicy.getOtherdata());
		if (expectedPolicy.getEpochtime() != null) {
			assertEquals(expectedPolicy.getEpochtime(), actualPolicy.getEpochtime()); 
		}
	}

	
}