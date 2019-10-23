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

package com.mark59.datahunter.functionalTest.dsl.pageElements;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class HtmlTable {

	private WebElement htmlTable;
	
	public HtmlTable(WebDriver driver, String id){
	 	htmlTable = null;
		List<WebElement> listOfElementsForGivenId = driver.findElements(By.id(id));
		if (listOfElementsForGivenId.size() > 0){
			htmlTable = listOfElementsForGivenId.get(0);
		}
	}

	
	/**
	 * returns a grouping of webelements for each row of a html table.  Does NOT return the heading row (assumed to be the first row of the table) 
	 */
	public List<HtmlTableRow> getHtmlTableRows(){
		return getHtmlTableRows(false);
	}
	
	
	public List<HtmlTableRow> getHtmlTableRows(boolean includeHeadingRow){
		List<HtmlTableRow> tableRows = new ArrayList<>();
		
		if (htmlTable != null ){
			List<WebElement> tableRowElements = htmlTable.findElements(By.tagName("tr"));
			for (WebElement webElement : tableRowElements) {
				tableRows.add(new HtmlTableRow(webElement));
			}
		}
		
		if (tableRows.size() > 0  && !includeHeadingRow ){
			tableRows.remove(0); 		
		}
		return tableRows;
	}
	
}
