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

package com.mark59.test.core;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.mark59.core.utils.Mark59Utils;

/**
* @author Philip Webb
* Written: Australian Autumn 2022 
*/
public class Mark59UtilsTest {
	
	@Test
	public final void Tests_On_CommaDelimStringToStringList() {
		assertArrayEquals( new String[]{}, Mark59Utils.commaDelimStringToStringList(null).toArray());
		assertEquals(0, Mark59Utils.commaDelimStringToStringList(null).size());	
		assertArrayEquals( new String[]{}, Mark59Utils.commaDelimStringToStringList("").toArray());
		assertEquals(0, Mark59Utils.commaDelimStringToStringList("").size());				
		assertArrayEquals( new String[]{}, Mark59Utils.commaDelimStringToStringList("         ").toArray());
		assertEquals(0, Mark59Utils.commaDelimStringToStringList("  ").size());			
		assertArrayEquals( new String[]{}, Mark59Utils.commaDelimStringToStringList("").toArray());
		assertEquals(0, Mark59Utils.commaDelimStringToStringList("").size());
		assertArrayEquals( new String[]{"a"}, Mark59Utils.commaDelimStringToStringList("a").toArray()) ;
		assertEquals(1, Mark59Utils.commaDelimStringToStringList("a").size());		
		assertArrayEquals( new String[]{"a","b"}, Mark59Utils.commaDelimStringToStringList("a,b").toArray());
		assertEquals(2, Mark59Utils.commaDelimStringToStringList("a,b").size());
		assertArrayEquals( new String[]{"b","a"}, Mark59Utils.commaDelimStringToStringList("b,a").toArray());	
		assertArrayEquals( new String[]{"b" ,"a"}, Mark59Utils.commaDelimStringToStringList(" b  , a ").toArray());
		assertArrayEquals( new String[]{"c", "b", "a"}, Mark59Utils.commaDelimStringToStringList(" c , b  , a ").toArray());			
		assertArrayEquals( new String[]{"c", "b", "", "a"}, Mark59Utils.commaDelimStringToStringList(" c ,, b  , , a ").toArray());			
	}

	@Test
	public final void Tests_On_PipeDelimStringToStringList() {
		assertArrayEquals( new String[]{}, Mark59Utils.pipeDelimStringToStringList(null).toArray());
		assertEquals(0, Mark59Utils.pipeDelimStringToStringList(null).size());	
		assertArrayEquals( new String[]{}, Mark59Utils.pipeDelimStringToStringList("").toArray());
		assertEquals(0, Mark59Utils.pipeDelimStringToStringList("").size());				
		assertArrayEquals( new String[]{}, Mark59Utils.pipeDelimStringToStringList("         ").toArray());
		assertEquals(0, Mark59Utils.pipeDelimStringToStringList("  ").size());			
		assertArrayEquals( new String[]{}, Mark59Utils.pipeDelimStringToStringList("").toArray());
		assertEquals(0, Mark59Utils.pipeDelimStringToStringList("").size());
		assertArrayEquals( new String[]{"a"}, Mark59Utils.pipeDelimStringToStringList("a").toArray()) ;
		assertEquals(1, Mark59Utils.pipeDelimStringToStringList("a").size());		
		assertArrayEquals( new String[]{"a,b"}, Mark59Utils.pipeDelimStringToStringList("a,b").toArray());
		assertEquals(1, Mark59Utils.pipeDelimStringToStringList("a,b").size());
		assertArrayEquals( new String[]{"a","b"}, Mark59Utils.pipeDelimStringToStringList("a|b").toArray());
		assertEquals(2, Mark59Utils.pipeDelimStringToStringList("a|b").size());		
		assertArrayEquals( new String[]{"b","a"}, Mark59Utils.pipeDelimStringToStringList("b|a").toArray());	
		assertArrayEquals( new String[]{"b" ,"a"}, Mark59Utils.pipeDelimStringToStringList(" b| a ").toArray());
		assertArrayEquals( new String[]{"c", "b" ,"a"}, Mark59Utils.pipeDelimStringToStringList(" c| b  |a ").toArray());
		assertArrayEquals( new String[]{"c", "b", ""}, Mark59Utils.pipeDelimStringToStringList(" c| b  | ").toArray());
		assertArrayEquals( new String[]{}, Mark59Utils.pipeDelimStringToStringList("||||").toArray());		
		assertArrayEquals( new String[]{"", "", ""}, Mark59Utils.pipeDelimStringToStringList("| ||||   | ").toArray());
	}
	
	
	
}
