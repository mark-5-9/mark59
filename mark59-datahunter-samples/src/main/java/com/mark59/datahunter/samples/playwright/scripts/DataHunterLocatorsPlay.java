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

package com.mark59.datahunter.samples.playwright.scripts;


import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebElement;

import com.mark59.dsl.samples.seleniumDSL.pageElements.HtmlTableRow;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitUntilState;


/**
 * A simple, raw DSL is used in the Playwright datahunter sample scripts.
 * 
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class DataHunterLocatorsPlay  {
	
	Page page;
	
	/** Doesn't actually perform the click - basically the equivalent of 'waitUntilClickable' in the Selenium sample scripts */
	Locator.ClickOptions waitUntilClickable = new Locator.ClickOptions().setTrial(true); 
	
	/** Delay the script for a second - similar outcome to ".thenSleep()" in the Selenium sample scripts */
	Locator.ClickOptions andsleep = new Locator.ClickOptions().setDelay(1000); 
	
	Page.NavigateOptions domContentLoaded = new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED);

	
	public DataHunterLocatorsPlay(Page page) {
		this.page = page;
	}
    
	// 	Common element Locators (used on multiple pages)
	
	public Locator application() {
		return page.locator("id=application");
	}
	public Locator applicationStartsWithOrEqualsList() {
		return page.locator("id=applicationStartsWithOrEquals");
	}
	public Locator identifier() {
		return page.locator("id=identifier");
	}
	public Locator lifecycle() {
		return page.locator("id=lifecycle");
	}
	public Locator useabilityList() {
		return page.locator("id=useability");
	}
	public Locator otherdata() {
		return page.locator("id=otherdata");
	}
	public Locator epochtime() {
		return page.locator("id=epochtime");
	}
	public Locator selectOrderList() {
		return page.locator("id=selectOrder");
	}	
	public Locator submitBtn() {
		return page.locator("id=submit");
	}

	// 	Manage Multiple Items (selection and actions)

	public Locator manangeMultipleItems_addItemLink() {
		return page.locator("a#AddPolicyLink");
	}	
	public Locator manangeMultipleItems_deleteSelectedItemsLink() {
		return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Delete Selected Items"));
	}	
	
	public HtmlTable printSelectedPoliciesTable() {
		return new HtmlTable(page, page.locator("id=printSelectedPoliciesTable"));
	}
	
	
	
	// 	Count Items Breakdown

	public int countItemsBreakdown_count(String application, String lifecycle, String useability){
		String elementCountId = (application + "_" + lifecycle + "_" + useability + "_count").replace(" ", "_").replace(".", "_");
		page.locator("id="+elementCountId);
		return Integer.parseInt(page.locator("id="+elementCountId).innerText());
	}

	
	
	// _Action Results Elements
	
	public Locator sql() {
		return page.locator("id=sql");
	}
	public Locator sqlResult() {
		return page.locator("id=sqlResult");
	}
	public Locator rowsAffected() {
		return page.locator("id=rowsAffected");
	}
	public Locator sqlResultText() {
		return page.locator("id=sqlResultText");
	}
	public Locator backLink() {
		return page.locator("//a[text()='Back']");
	}	
	public String formatResultsMessage(String tag){
		return  "DataHunter " +  sqlResultText().innerText() + " at " + tag + ": "
				+ ", SQL statement [" + sql().innerText() + "]"
				+ ", rows affected [" +  rowsAffected().innerText() + "]"				
				+ ", details [" + sqlResultText().innerText() + "]";		
	}
	
	
	
	// Navigation Panel
	
	public Locator navOverviewLink() {
		return page.locator("//a[text()='Overview']");
	}	
	public Locator navMainMenuLink() {
		return page.locator("//a[text()='Main Menu']");
	}	
	public Locator navItemsBreakdownLink() {
		return page.locator("//a[text()='Items Breakdown']");
	}
	public Locator navManageMultipleItemsLink() {
		return page.locator("//a[text()='Manage Multiple Items']");
	}	
	public Locator navAddItemLink() {
		// as 'Add Item' text also appears on select_multiple_policies_action page: 
		return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add Item")).first();
	}	
	public Locator navCountItemsLink() {
		return page.locator("//a[text()='Count Items']");
	}	
	public Locator navDisplayItemLink() {
		return page.locator("//a[text()='Display Item']");
	}	
	public Locator navDeleteItemLink() {
		return page.locator("//a[text()='Delete Item']");
	}		
	public Locator navUseNextItemLink() {
		return page.locator("//a[text()='Use Next Item']");
	}	
	public Locator navLookupNextItemLink() {
		return page.locator("//a[text()='Lookup Next Item']");
	}	
	public Locator navUpdateUseStatesLink() {
		return page.locator("//a[text()='Update Use States']");
	}	
	public Locator navAsyncMsgAnalyzerLink() {
		return page.locator("//a[text()='Async Msg Analyzer']");
	}
	
	
	
	// TODO 
	class HtmlTable {

		Page page;
		Locator htmlTable;
		
		public HtmlTable(Page page, Locator htmlTable) {
			this.page = page;
		}
		
		/**
		 * returns a list of web elements, each representing a row of a html table.  
		 * Doesn't return the heading row (assumed to be the first row of the table):<br> 
		 */
		public List<HtmlTableRow> getHtmlTableRows(){
			return getHtmlTableRows(false);
		}	
		
		public List<HtmlTableRow> getHtmlTableRows(boolean includeHeadingRow){
			List<WebElement> tableRows = new ArrayList<>();
			if (htmlTable != null ){
				//tableRows = htmlTable.findElements(By.tagName("tr"));
				//////////////////////////page.l 
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
	
	
	
}
