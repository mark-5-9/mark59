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

package com.mark59.dsl.samples.seleniumDSL.pageElements;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.mark59.dsl.samples.seleniumDSL.core.Elemental;
import com.mark59.dsl.samples.seleniumDSL.core.FluentWaitFactory;


/**
 * @author Philip Webb
 * Written: Australian Spring 2019
 */
public class HtmlTable extends Elemental {


	public HtmlTable(WebDriver driver, String id) {
		this(driver, By.id(id));
	}

	public HtmlTable(WebDriver driver, By by) {
		super(driver, by, FluentWaitFactory.DEFAULT_TIMEOUT, FluentWaitFactory.DEFAULT_POLLING);
	}
	
	
	/**
	 * returns a list of web elements, each representing a row of a html table.<br> 
	 * @return a list of HtmlTableRow of for the table
	 */
	public List<HtmlTableRow> getHtmlTableRows(){
		return getHtmlTableRows(false);
	}	
	
	public List<HtmlTableRow> getHtmlTableRows(boolean includeHeadingRow){
		WebElement htmlTable = super.waitForAndFindElement();
		List<WebElement> tableRows = new ArrayList<>();
		if (htmlTable != null ){
			tableRows = htmlTable.findElements(By.tagName("tr"));
		}
		if (tableRows.size() > 0  && !includeHeadingRow ){
			tableRows.remove(0); 		
		}	
	
		List<HtmlTableRow> htmlTableRows = new ArrayList<>();
		for (WebElement tableRowElement : tableRows) {
			htmlTableRows.add(new HtmlTableRow(tableRowElement));
		}
		return htmlTableRows; 
	}	

}
