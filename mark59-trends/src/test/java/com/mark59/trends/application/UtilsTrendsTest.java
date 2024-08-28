/*
 *  Copyright 2019 Mark59.com
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

package com.mark59.trends.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class UtilsTrendsTest  {
	
	@Test
    public void testUtilsStringListToCommaDelimString(){
    	
 		List<String> ListOfStrings = new ArrayList<>();
		String commaDelimString = UtilsTrends.stringListToCommaDelimString(ListOfStrings);
		Assert.assertEquals(0, commaDelimString.length());
		Assert.assertNotNull(commaDelimString);
                
 		ListOfStrings = new ArrayList<>();
 		ListOfStrings.add("onetsring");
		commaDelimString = UtilsTrends.stringListToCommaDelimString(ListOfStrings);
		Assert.assertEquals("onetsring", commaDelimString);
     
 		ListOfStrings = new ArrayList<>();
 		ListOfStrings.add("string1");
 		ListOfStrings.add("string2");
		commaDelimString = UtilsTrends.stringListToCommaDelimString(ListOfStrings);
		Assert.assertEquals("string1,string2", commaDelimString);
    }

	@Test
    public void testUtilsCommaDelimStringToStringList(){
    	
 		List<String> ListOfStrings = new ArrayList<>();
 		String commaDelimString = UtilsTrends.stringListToCommaDelimString(ListOfStrings);  //so we should have an empty string
 		Assert.assertEquals("", commaDelimString);
  		ListOfStrings = UtilsTrends.commaDelimStringToStringList(commaDelimString);   
  		Assert.assertTrue(ListOfStrings.isEmpty()  );

 		ListOfStrings = UtilsTrends.commaDelimStringToStringList(null);
 		Assert.assertTrue( ListOfStrings.isEmpty()  );
        
        commaDelimString = "singleString";
 		ListOfStrings = UtilsTrends.commaDelimStringToStringList(commaDelimString);   
 		Assert.assertFalse(ListOfStrings.isEmpty()  );
        Assert.assertEquals(1, ListOfStrings.size());
        Assert.assertEquals("singleString", ListOfStrings.get(0));

        commaDelimString = "firststr,secstr";
 		ListOfStrings = UtilsTrends.commaDelimStringToStringList(commaDelimString);   
 		Assert.assertFalse( ListOfStrings.isEmpty()  );
        Assert.assertEquals(2, ListOfStrings.size());
        Assert.assertEquals("firststr", ListOfStrings.get(0));
        Assert.assertEquals("secstr", ListOfStrings.get(1));

        commaDelimString = "firststr,secstr,3rdstr";
 		ListOfStrings = UtilsTrends.commaDelimStringToStringList(commaDelimString);   
 		Assert.assertFalse( ListOfStrings.isEmpty()  );
        Assert.assertEquals(3, ListOfStrings.size());
        Assert.assertEquals("firststr", ListOfStrings.get(0));
        Assert.assertEquals("secstr", ListOfStrings.get(1));
        Assert.assertEquals("3rdstr", ListOfStrings.get(2));
    }	
    
	@Test
    public void testCommaDelimStringToSortedStringArray(){
    	Comparator<Object> reverse = Collections.reverseOrder();
    	Assert.assertArrayEquals( new String[]{"b","a"}, UtilsTrends.commaDelimStringToSortedStringArray("a,b", reverse));
    	Assert.assertArrayEquals( new String[]{"c","b","a"}, UtilsTrends.commaDelimStringToSortedStringArray("a,c,b", reverse));
    	Assert.assertArrayEquals( new String[]{"c","b","a"}, UtilsTrends.commaDelimStringToSortedStringArray(" a  , c,  b", reverse));
    	Assert.assertArrayEquals( new String[]{"x"}, UtilsTrends.commaDelimStringToSortedStringArray("x", reverse));
    	Assert.assertArrayEquals( new String[]{}, UtilsTrends.commaDelimStringToSortedStringArray(null, reverse));
    	Assert.assertArrayEquals( new String[]{}, UtilsTrends.commaDelimStringToSortedStringArray("", reverse));
    	Assert.assertArrayEquals( new String[]{}, UtilsTrends.commaDelimStringToSortedStringArray("  ", reverse));
    	Assert.assertArrayEquals( new String[]{"3","1","02" }, UtilsTrends.commaDelimStringToSortedStringArray("1,02,3", reverse));
    }
     
	
	@Test
    public void teststringContainsHtmlTags(){
        Assert.assertFalse("iDontHaveAnyHtml", UtilsTrends.stringContainsHtmlTags("iDontHaveAnyHtml"));
        Assert.assertFalse(" >x<y ", UtilsTrends.stringContainsHtmlTags(" >x<y "));        
        Assert.assertFalse("<<x<y", UtilsTrends.stringContainsHtmlTags("<<x<y"));        
        Assert.assertFalse("><\\<x<y", UtilsTrends.stringContainsHtmlTags("><\\<x<y"));        
        Assert.assertFalse("></", UtilsTrends.stringContainsHtmlTags("></"));        
        Assert.assertTrue("<iDoHaveHtml>", UtilsTrends.stringContainsHtmlTags("<iDoHaveHtml>"));
        Assert.assertTrue("jj<x>", UtilsTrends.stringContainsHtmlTags("jj<x>"));
		Assert.assertTrue("<a href='http://l:83/t/D_R%20_370.html'>R370</a>",
				UtilsTrends.stringContainsHtmlTags("<a href='http://l:83/t/D_R%20_370.html'>R370</a>"));
		Assert.assertTrue("stackoverflow extract",	UtilsTrends.stringContainsHtmlTags(
				"<a href=\"https://stackoverflow.com\" class=\"s-topbar--logo js-gps-track data-gps-track=\"top_nav.click"
				+ "({is_current:false, location:2, destination:8})\"><span class=\"-img _glyph\">Stack Overflow</span></a>"));
        Assert.assertTrue("theage", UtilsTrends.stringContainsHtmlTags("this: <a href='http://theage.com.au'>TheAge</a>)"));		
    }
	
}