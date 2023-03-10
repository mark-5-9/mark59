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

package com.mark59.dsl.samples.seleniumDSL.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.mark59.dsl.samples.seleniumDSL.core._GenericPage;
import com.mark59.dsl.samples.seleniumDSL.pageElements.CheckBox;
import com.mark59.dsl.samples.seleniumDSL.pageElements.DropdownList;
import com.mark59.dsl.samples.seleniumDSL.pageElements.HtmlTable;
import com.mark59.dsl.samples.seleniumDSL.pageElements.InputTextElement;
import com.mark59.dsl.samples.seleniumDSL.pageElements.Link;
import com.mark59.dsl.samples.seleniumDSL.pageElements.OptionButton;
import com.mark59.dsl.samples.seleniumDSL.pageElements.PageTextElement;
import com.mark59.dsl.samples.seleniumDSL.pageElements.SubmitBtn;

/**
 * A sample showing how to construct a 'page' object using the sample DSL page elements 
 * 
 * @author Philip Webb
 * Written: Australian Spring 2019
 */
public class SamplePage extends _GenericPage  {
	
	public SamplePage(WebDriver driver) {
		super(driver);
	}

	
	public CheckBox someCheckBox() {
		return new CheckBox(driver, By.id("CheckBoxID"));
	}

    public DropdownList someDropdownList() {
		return new DropdownList(driver, By.id("DropdownListId"));
	}

    /**
	 * see com.mark59.datahunter.samples.scripts.DataHunterLifecyclePvtScript for usage.
	 * @return HtmlTable
	 */
	public HtmlTable someBasicHtmlTable() {
		return new HtmlTable(driver, "someBasicHtmlTableID");
	}

    public InputTextElement someInputTextElement() {
		return new InputTextElement(driver, By.id("InputTextElementId"));
	}

    public Link somePageLink() {
		return new Link(driver, "LinkText");
	}

    public Link somePageLinkById() {
		return new Link(driver, By.id("LinkbyIdRatherAlternativeToLinkText"));
	}

    public OptionButton someOptionsButton() {
		return new OptionButton(driver, By.id("OptionButtonId"));
	}

    public PageTextElement someTextElement() {
		return new PageTextElement(driver, By.id("PageTextElementId"));
	}

    public SubmitBtn submit() {
		return new SubmitBtn(driver, By.id("SubmitBtnId"));
	}

}
