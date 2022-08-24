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

package com.mark59.trends.application;

import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.TestCase;


public class UtilsTrendsTest   extends TestCase  {
	
    public void testUtilsStringListToCommaDelimString()
    {
 		List<String> ListOfStrings = new ArrayList<>();
		String commaDelimString = UtilsTrends.stringListToCommaDelimString(ListOfStrings);
        assertEquals(0, commaDelimString.length());
        assertNotNull(commaDelimString);
                
 		ListOfStrings = new ArrayList<>();
 		ListOfStrings.add("onetsring");
		commaDelimString = UtilsTrends.stringListToCommaDelimString(ListOfStrings);
        assertEquals("onetsring", commaDelimString);
     
 		ListOfStrings = new ArrayList<>();
 		ListOfStrings.add("string1");
 		ListOfStrings.add("string2");
		commaDelimString = UtilsTrends.stringListToCommaDelimString(ListOfStrings);
        assertEquals("string1,string2", commaDelimString);
    }

    
    public void testUtilsCommaDelimStringToStringList()
    {
 		List<String> ListOfStrings = new ArrayList<>();
 		String commaDelimString = UtilsTrends.stringListToCommaDelimString(ListOfStrings);  //so we should have an empty string
        assertEquals("", commaDelimString);
  		ListOfStrings = UtilsTrends.commaDelimStringToStringList(commaDelimString);   
        assertTrue( ListOfStrings.isEmpty()  );

 		ListOfStrings = UtilsTrends.commaDelimStringToStringList(null);
        assertTrue( ListOfStrings.isEmpty()  );
        
        commaDelimString = "singleString";
 		ListOfStrings = UtilsTrends.commaDelimStringToStringList(commaDelimString);   
        assertFalse( ListOfStrings.isEmpty()  );
        assertEquals(1, ListOfStrings.size());
        assertEquals("singleString", ListOfStrings.get(0));

        commaDelimString = "firststr,secstr";
 		ListOfStrings = UtilsTrends.commaDelimStringToStringList(commaDelimString);   
        assertFalse( ListOfStrings.isEmpty()  );
        assertEquals(2, ListOfStrings.size());
        assertEquals("firststr", ListOfStrings.get(0));
        assertEquals("secstr", ListOfStrings.get(1));

        commaDelimString = "firststr,secstr,3rdstr";
 		ListOfStrings = UtilsTrends.commaDelimStringToStringList(commaDelimString);   
        assertFalse( ListOfStrings.isEmpty()  );
        assertEquals(3, ListOfStrings.size());
        assertEquals("firststr", ListOfStrings.get(0));
        assertEquals("secstr", ListOfStrings.get(1));
        assertEquals("3rdstr", ListOfStrings.get(2));
    }	

    
    
    public void testCommaDelimStringToSortedStringArray()
    {
    	Comparator<Object> reverse = Collections.reverseOrder();
		assertArrayEquals( new String[]{"b","a"}, UtilsTrends.commaDelimStringToSortedStringArray("a,b", reverse));
		assertArrayEquals( new String[]{"c","b","a"}, UtilsTrends.commaDelimStringToSortedStringArray("a,c,b", reverse));
		assertArrayEquals( new String[]{"c","b","a"}, UtilsTrends.commaDelimStringToSortedStringArray(" a  , c,  b", reverse));
		assertArrayEquals( new String[]{"x"}, UtilsTrends.commaDelimStringToSortedStringArray("x", reverse));
		assertArrayEquals( new String[]{}, UtilsTrends.commaDelimStringToSortedStringArray(null, reverse));
		assertArrayEquals( new String[]{}, UtilsTrends.commaDelimStringToSortedStringArray("", reverse));
		assertArrayEquals( new String[]{}, UtilsTrends.commaDelimStringToSortedStringArray("  ", reverse));
		assertArrayEquals( new String[]{"3","1","02" }, UtilsTrends.commaDelimStringToSortedStringArray("1,02,3", reverse));
    }
   
    
    
    
    
    
    
    
    
    
}