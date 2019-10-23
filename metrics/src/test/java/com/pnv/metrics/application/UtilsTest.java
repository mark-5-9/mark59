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

package com.pnv.metrics.application;

import java.util.ArrayList;
import java.util.List;

import com.mark59.metrics.application.Utils;

import junit.framework.TestCase;


public class UtilsTest   extends TestCase  {
	
    public void testUtilsStringListToCommaDelimString()
    {
 		List<String> ListOfStrings = new ArrayList<String>();
		String commaDelimString = Utils.stringListToCommaDelimString(ListOfStrings);
        assertTrue( commaDelimString.length() == 0 );
        assertTrue( commaDelimString != null );
                
 		ListOfStrings = new ArrayList<String>();
 		ListOfStrings.add("onetsring");
		commaDelimString = Utils.stringListToCommaDelimString(ListOfStrings);
        assertTrue( "onetsring".equals(commaDelimString) );
     
 		ListOfStrings = new ArrayList<String>();
 		ListOfStrings.add("string1");
 		ListOfStrings.add("string2");
		commaDelimString = Utils.stringListToCommaDelimString(ListOfStrings);
        assertTrue( "string1,string2".equals(commaDelimString) );        
    }

    
    public void testUtilsCommaDelimStringToStringList()
    {
 		List<String> ListOfStrings = new ArrayList<String>();
 		assertTrue(ListOfStrings.size() == 0 ); 
 		String commaDelimString = Utils.stringListToCommaDelimString(ListOfStrings);  //so we should have an empty string
        assertTrue( "".equals(commaDelimString) );
  		ListOfStrings = Utils.commaDelimStringToStringList(commaDelimString);   
        assertTrue( ListOfStrings.isEmpty()  );  
        assertTrue( ListOfStrings.size() == 0  );  
       
        commaDelimString = null;
 		ListOfStrings = Utils.commaDelimStringToStringList(commaDelimString);   
        assertTrue( ListOfStrings.isEmpty()  );  
        assertTrue( ListOfStrings.size() == 0  );       
        
        commaDelimString = "singleString";
 		ListOfStrings = Utils.commaDelimStringToStringList(commaDelimString);   
        assertFalse( ListOfStrings.isEmpty()  ); 
        assertTrue( ListOfStrings.size() == 1 );          
        assertTrue( "singleString".equals(ListOfStrings.get(0)));          

        commaDelimString = "firststr,secstr";
 		ListOfStrings = Utils.commaDelimStringToStringList(commaDelimString);   
        assertFalse( ListOfStrings.isEmpty()  ); 
        assertTrue( ListOfStrings.size() == 2 );          
        assertTrue( "firststr".equals(ListOfStrings.get(0)));          
        assertTrue( "secstr".equals(ListOfStrings.get(1)));     

        commaDelimString = "firststr,secstr,3rdstr";
 		ListOfStrings = Utils.commaDelimStringToStringList(commaDelimString);   
        assertFalse( ListOfStrings.isEmpty()  ); 
        assertTrue( ListOfStrings.size() == 3 );          
        assertTrue( "firststr".equals(ListOfStrings.get(0)));          
        assertTrue( "secstr".equals(ListOfStrings.get(1)));      
        assertTrue( "3rdstr".equals(ListOfStrings.get(2)));      
        
    }	
	
}