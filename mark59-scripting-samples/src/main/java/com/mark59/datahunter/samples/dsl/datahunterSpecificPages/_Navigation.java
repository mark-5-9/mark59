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

package com.mark59.datahunter.samples.dsl.datahunterSpecificPages;

import org.openqa.selenium.WebDriver;
import com.mark59.dsl.samples.seleniumDSL.pageElements.Link;


/**
 * @author Philip Webb
 * Written: Australian Summer 2023/24
 */
public class _Navigation {
	
	protected WebDriver driver;
	
	public _Navigation(WebDriver driver) {
		this.driver = driver;
	}

	public Link overviewLink() {
		return new Link(driver, "Overview");
	}	
	public Link mainMenuLink() {
		return new Link(driver, "Main Menu");
	}	
	public Link itemsBreakdownLink() {
		return new Link(driver, "Items Breakdown");
	}
	public Link manageMultipleItemsLink() {
		return new Link(driver, "Manage Multiple Items");
	}	
	public Link addItemLink() {
		return new Link(driver, "Add Item");
	}	
	public Link countItemsLink() {
		return new Link(driver, "Count Items");
	}	
	public Link displayItemLink() {
		return new Link(driver, "Display Item");
	}	
	public Link deleteItemLink() {
		return new Link(driver, "Delete Item");
	}		
	public Link useNextItemLink() {
		return new Link(driver, "Use Next Item");
	}	
	public Link lookupNextItemLink() {
		return new Link(driver, "Lookup Next Item");
	}	
	public Link updateUseStatesLink() {
		return new Link(driver, "Update Use States");
	}	
	public Link asyncMsgAnalyzerLink() {
		return new Link(driver, "Async Msg Analyzer");
	}	
	
}
