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
		return new HtmlTable(page.locator("id=printSelectedPoliciesTable"));
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
	

	class HtmlTable {

		Locator htmlTable;
		
		public HtmlTable(Locator htmlTable) {
			this.htmlTable = htmlTable;
		}

		/**
		 * returns the values by row and column of a HTML table - remove header row.  
		 */
		public List<List<String>> getHtmlTableRows(){
			return getHtmlTableRows(false);
		}	
		
		public List<List<String>> getHtmlTableRows(boolean includeHeadingRow){
			List<List<String>> tableRows = new ArrayList<List<String>>();
			if (htmlTable != null ){
				int rowCount = htmlTable.locator("tr").count();
			    for (int i = 0; i < rowCount; i++) {
			    	Locator tablerow = htmlTable.locator("tr").nth(i);
			    	List<String> tableRowVals = new ArrayList<String>();
			    	
				    for (int j = 0; j < tablerow.locator("td").count(); j++) {
				    	tableRowVals.add(tablerow.locator("td").nth(j).innerText());
				    }
				    tableRows.add(tableRowVals);
			    } 
			}
			if (tableRows.size() > 0  && !includeHeadingRow ){
				tableRows.remove(0); 		
			}	
			return tableRows;
		}	
		
		
		public String getColumnNumberOfExpectedColumns(List<String> tableRowVals,  int columnNumber, int ofExpectedNumberOfColumns ){
			if (tableRowVals.size() < columnNumber ){
				throw new RuntimeException("HtmlTableRow : not enough columns in row!  [" + tableRowVals + "], requested col number " + columnNumber );
			}	

			if (tableRowVals.size() != ofExpectedNumberOfColumns ){
				throw new RuntimeException("HtmlTableRow : invalid columns number for row [" + tableRowVals + "], "
						+  tableRowVals.size() + " cols exist in row, but expected was " + ofExpectedNumberOfColumns );
			}	
			return tableRowVals.get(columnNumber);
		}
		
	} // HtmlTable	
	
}
