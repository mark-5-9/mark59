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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */

public class SubmitBtn {

	private WebElement webelement;

	public SubmitBtn(WebDriver driver, String id) {
		webelement = driver.findElement(By.id(id));
	}


	/**
	 * https://stackoverflow.com/questions/833032/submit-is-not-a-function-error-in-javascript
	 * https://stackoverflow.com/questions/17530104/selenium-webdriver-submit-vs-click
	 * webelement.submit();
	 * 
	 * obviously a waitfor condition will be required for more complex (JS/async) page loads  		
	 */
	public void submit(){
		webelement.click();
	}

}
