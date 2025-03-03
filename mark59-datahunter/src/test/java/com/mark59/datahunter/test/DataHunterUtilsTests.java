package com.mark59.datahunter.test;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.mark59.datahunter.application.DataHunterUtils;

import junit.framework.TestCase;


public class DataHunterUtilsTests extends TestCase {	
	
	@Test
	public void checkCommaDelimStringTrimAll() throws IOException {
		assertEquals("", DataHunterUtils.commaDelimStringTrimAll(""));
		assertEquals("", DataHunterUtils.commaDelimStringTrimAll(null));
		assertEquals("", DataHunterUtils.commaDelimStringTrimAll(","));
		assertEquals("", DataHunterUtils.commaDelimStringTrimAll(",,,,,,,"));
		assertEquals("", DataHunterUtils.commaDelimStringTrimAll(", ,	, "));
		
		assertEquals("oneval", DataHunterUtils.commaDelimStringTrimAll("oneval"));
		assertEquals("one spaced val", DataHunterUtils.commaDelimStringTrimAll("one spaced val"));
		assertEquals("one spaced val", DataHunterUtils.commaDelimStringTrimAll(" one spaced val  "));
		assertEquals("@&<\\>b_r! #<<//", DataHunterUtils.commaDelimStringTrimAll(" @&<\\>b_r! #<<// "));
		
		assertEquals("cat,mat", DataHunterUtils.commaDelimStringTrimAll("cat,mat"));
		assertEquals("cat,mat", DataHunterUtils.commaDelimStringTrimAll(" cat	, mat "));
		assertEquals("cat,mat", DataHunterUtils.commaDelimStringTrimAll(" cat,mat "));
		assertEquals("cat,mat", DataHunterUtils.commaDelimStringTrimAll("cat ,mat"));
		assertEquals("cat%25%,mat", DataHunterUtils.commaDelimStringTrimAll(" cat%25%	, mat "));
		assertEquals("cat %25%,ma t", DataHunterUtils.commaDelimStringTrimAll(" cat %25%,ma t"));

		assertEquals("cat,mat", DataHunterUtils.commaDelimStringTrimAll(",,cat,,mat, , ,"));
		assertEquals("cat 1,mat 2", DataHunterUtils.commaDelimStringTrimAll(",,cat 1,,mat 2, , ,"));
		assertEquals("cat 1,mat 2", DataHunterUtils.commaDelimStringTrimAll(",,,cat 1,,, mat 2, , ,"));	
		assertEquals("cat 1,mat 2", DataHunterUtils.commaDelimStringTrimAll(", ,,cat 1, ,, mat 2, , ,,"));	
		
		assertEquals("cat,sat mat", DataHunterUtils.commaDelimStringTrimAll("cat,sat mat"));			
		assertEquals("ca t,s at,mat", DataHunterUtils.commaDelimStringTrimAll(",ca t,,s at, mat,"));			
		assertEquals("ca t,s_at,on mat", DataHunterUtils.commaDelimStringTrimAll("	,ca t,	,s_at, on mat	,,"));	
		assertEquals("ca''t ',%s'at,on ' mat", DataHunterUtils.commaDelimStringTrimAll(" ca''t ' , %s'at, on ' mat	"));	
	}
	
}