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

package com.mark59.core.utils;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.jmeter.config.Arguments;
import org.junit.Test;

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

	@Test
	public final void Tests_mergeMapWithAnOverrideMap_BasicFunctionality() {
		Map<String, String> jmArgMap = new TreeMap<String, String>();
		Map<String, String> baseMap = new LinkedHashMap<>(Map.of("key1", "base1", "key2", "base2", "key3", "base3"));
		Map<String, String> additionalEntriesMap = new LinkedHashMap<>(Map.of("key2", "over2", "key4", "add4"));

		jmArgMap = Mark59Utils.mergeMapWithAnOverrideMap(baseMap, additionalEntriesMap).getArgumentsAsMap();
		assertTrue( jmArgMap.keySet().equals(new HashSet<String>(Arrays.asList("key1","key2","key3","key4"))));
		assertEquals("base1", jmArgMap.get("key1"));
		assertEquals("over2", jmArgMap.get("key2")); // Overridden value
		assertEquals("base3", jmArgMap.get("key3"));
		assertEquals("add4", jmArgMap.get("key4")); // New value
	}

	@Test
	public final void Tests_mergeMapWithAnOverrideMap_NoConflicts() {
		Map<String, String> baseMap = new LinkedHashMap<>(Map.of("key1", "value1", "key2", "value2"));
		Map<String, String> additionalEntriesMap = new LinkedHashMap<>(Map.of("key3", "value3", "key4", "value4"));

		Arguments result = Mark59Utils.mergeMapWithAnOverrideMap(baseMap, additionalEntriesMap);
		Map<String, String> resultMap = result.getArgumentsAsMap();

		assertEquals(4, resultMap.size());
		assertEquals("value1", resultMap.get("key1"));
		assertEquals("value2", resultMap.get("key2"));
		assertEquals("value3", resultMap.get("key3"));
		assertEquals("value4", resultMap.get("key4"));
	}

	@Test
	public final void Tests_mergeMapWithAnOverrideMap_EmptyMaps() {
		Map<String, String> baseMap = new LinkedHashMap<>();
		Map<String, String> additionalEntriesMap = new LinkedHashMap<>();

		Arguments result = Mark59Utils.mergeMapWithAnOverrideMap(baseMap, additionalEntriesMap);
		Map<String, String> resultMap = result.getArgumentsAsMap();

		assertEquals(0, resultMap.size());
	}

	@Test
	public final void Tests_mergeMapWithAnOverrideMap_EmptyBaseMap() {
		Map<String, String> baseMap = new LinkedHashMap<>();
		Map<String, String> additionalEntriesMap = new LinkedHashMap<>(Map.of("key1", "value1", "key2", "value2"));

		Arguments result = Mark59Utils.mergeMapWithAnOverrideMap(baseMap, additionalEntriesMap);
		Map<String, String> resultMap = result.getArgumentsAsMap();

		assertEquals(2, resultMap.size());
		assertEquals("value1", resultMap.get("key1"));
		assertEquals("value2", resultMap.get("key2"));
	}

	@Test
	public final void Tests_mergeMapWithAnOverrideMap_EmptyAdditionalMap() {
		Map<String, String> baseMap = new LinkedHashMap<>(Map.of("key1", "value1", "key2", "value2"));
		Map<String, String> additionalEntriesMap = new LinkedHashMap<>();

		Arguments result = Mark59Utils.mergeMapWithAnOverrideMap(baseMap, additionalEntriesMap);
		Map<String, String> resultMap = result.getArgumentsAsMap();

		assertEquals(2, resultMap.size());
		assertEquals("value1", resultMap.get("key1"));
		assertEquals("value2", resultMap.get("key2"));
	}

	@Test
	public final void Tests_mergeMapWithAnOverrideMap_SpecialCharacters() {
		Map<String, String> baseMap = new LinkedHashMap<>(Map.of("key@1", "value#1", "key$2", "value%2"));
		Map<String, String> additionalEntriesMap = new LinkedHashMap<>(Map.of("key$2", "override&2", "key^3", "value*3"));

		Arguments result = Mark59Utils.mergeMapWithAnOverrideMap(baseMap, additionalEntriesMap);
		Map<String, String> resultMap = result.getArgumentsAsMap();

		assertEquals(3, resultMap.size());
		assertEquals("value#1", resultMap.get("key@1"));
		assertEquals("override&2", resultMap.get("key$2")); // Overridden
		assertEquals("value*3", resultMap.get("key^3"));
	}

	@Test
	public final void Tests_mergeMapWithAnOverrideMap_NullAndEmptyValues() {
		Map<String, String> baseMap = new LinkedHashMap<>(Map.of("key1", "", "key2", "value2"));
		Map<String, String> additionalEntriesMap = new LinkedHashMap<>(Map.of("key2", "", "key3", "value3"));

		Arguments result = Mark59Utils.mergeMapWithAnOverrideMap(baseMap, additionalEntriesMap);
		Map<String, String> resultMap = result.getArgumentsAsMap();

		assertEquals(3, resultMap.size());
		assertEquals("", resultMap.get("key1"));
		assertEquals("", resultMap.get("key2")); // Overridden with empty value
		assertEquals("value3", resultMap.get("key3"));
	}

	@Test
	public final void Tests_mergeMapWithAnOverrideMap_PreservesInputMaps() {
		// Test that input maps are not modified (side effect test)
		Map<String, String> baseMap = new LinkedHashMap<>(Map.of("key1", "value1", "key2", "value2"));
		Map<String, String> additionalEntriesMap = new LinkedHashMap<>(Map.of("key2", "override2", "key3", "value3"));
		// Store original sizes and values
		int originalBaseSize = baseMap.size();
		int originalAdditionalSize = additionalEntriesMap.size();
		String originalKey2Value = additionalEntriesMap.get("key2");

		Mark59Utils.mergeMapWithAnOverrideMap(baseMap, additionalEntriesMap);

		assertEquals(originalBaseSize, baseMap.size());
		assertEquals(originalAdditionalSize, additionalEntriesMap.size()); 
		assertEquals("value1", baseMap.get("key1"));
		assertEquals("value2", baseMap.get("key2"));
		assertEquals(originalKey2Value, additionalEntriesMap.get("key2"));
		assertEquals("value3", additionalEntriesMap.get("key3"));
	}

	@Test
	public final void Tests_mergeMapWithAnOverrideMap_MultipleOverrides() {
		Map<String, String> baseMap = new LinkedHashMap<>(Map.of("key1", "base1", "key2", "base2", "key3", "base3", "key4", "base4"));
		Map<String, String> additionalEntriesMap = new LinkedHashMap<>(Map.of("key1", "over1", "key2", "over2", "key3", "over3"));

		Arguments result = Mark59Utils.mergeMapWithAnOverrideMap(baseMap, additionalEntriesMap);
		Map<String, String> resultMap = result.getArgumentsAsMap();

		assertEquals(4, resultMap.size());
		assertEquals("over1", resultMap.get("key1")); // Overridden
		assertEquals("over2", resultMap.get("key2")); // Overridden
		assertEquals("over3", resultMap.get("key3")); // Overridden
		assertEquals("base4", resultMap.get("key4")); // Not overridden
	}

	@Test
	public final void Tests_mergeMapWithAnOverrideMap_LargeDataSet() {
		// Performance test with larger dataset
		Map<String, String> baseMap = new LinkedHashMap<>();
		Map<String, String> additionalEntriesMap = new LinkedHashMap<>();
		// Create base map with 100 entries
		for (int i = 0; i < 100; i++) {baseMap.put("baseKey" + i, "baseValue" + i);		}

		// Create additional map with 50 new entries and 25 overrides
		for (int i = 0; i < 50; i++) {additionalEntriesMap.put("additionalKey" + i, "additionalValue" + i);}
		for (int i = 0; i < 25; i++) {additionalEntriesMap.put("baseKey" + i, "overriddenValue" + i);}
		Arguments result = Mark59Utils.mergeMapWithAnOverrideMap(baseMap, additionalEntriesMap);
		Map<String, String> resultMap = result.getArgumentsAsMap();

		assertEquals(150, resultMap.size()); // 100 base + 50 new (25 duplicated entries should not count)
		// Verify some 'duplicated' values
		assertEquals("overriddenValue0", resultMap.get("baseKey0"));
		assertEquals("overriddenValue24", resultMap.get("baseKey24"));
		// Verify some non-overridden values
		assertEquals("baseValue25", resultMap.get("baseKey25"));
		assertEquals("baseValue99", resultMap.get("baseKey99"));
		// Verify new values
		assertEquals("additionalValue0", resultMap.get("additionalKey0"));
		assertEquals("additionalValue49", resultMap.get("additionalKey49"));
	}

}
