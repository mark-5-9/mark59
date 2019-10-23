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

package com.mark59.seleniumDSL.pageElements;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;


/**
 * @author Philip Webb
 * Written: Australian Spring 2019
 */
public class HtmlTableRow {

	private WebElement tableRow;
	
	public HtmlTableRow(WebElement tableRow) {
		this.tableRow = tableRow;
	}
	
	public WebElement getColumnNumber(int columnNumber){
		List<WebElement> webElementsForRow = getWebElementsForRow();
		if (webElementsForRow.size() < columnNumber ){
			throw new RuntimeException("HtmlTableRow : not enough columns in row!  " +  webElementsForRow.size() + " cols exist, requested col number " + columnNumber );
		}	
		return webElementsForRow.get(columnNumber-1) ;
	}	
	
	public WebElement getColumnNumberOfExpectedColumns(int columnNumber, int ofExpectedNumberOfColumns ){
		List<WebElement> webElementsForRow = getWebElementsForRow(ofExpectedNumberOfColumns);
		if (webElementsForRow.size() < columnNumber ){
			throw new RuntimeException("HtmlTableRow : not enough columns in row!  " +  webElementsForRow.size() + " cols exist, requested col number " + columnNumber );
		}	
		return webElementsForRow.get(columnNumber-1) ;
	}	
	
	private List<WebElement> getWebElementsForRow(){
		return tableRow.findElements(By.tagName("td"));
	}
	
	private List<WebElement> getWebElementsForRow(int ofExpectedNumberOfColumns){
		List<WebElement> webElementsForRow = getWebElementsForRow();
		if (webElementsForRow.size() != ofExpectedNumberOfColumns ){
			throw new RuntimeException("HtmlTableRow : invalid columns number. " + webElementsForRow.size() + " cols exist in row, but expected was " + ofExpectedNumberOfColumns );
		}	
		return webElementsForRow;
	}

}
