package com.mark59.dsl.samples.seleniumDSL.pageElements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.mark59.dsl.samples.seleniumDSL.core.ElementalUsingVariablePolling;
import com.mark59.dsl.samples.seleniumDSL.core.FluentWaitFactory;

public class PageTextElementVariablePolling extends ElementalUsingVariablePolling {
	
	public PageTextElementVariablePolling(WebDriver driver, String id) {
		this(driver, By.id(id));
	}

	public PageTextElementVariablePolling(WebDriver driver, By by) {
		super(driver, by, FluentWaitFactory.DEFAULT_TIMEOUT, FluentWaitFactory.DEFAULT_VARIABLE_POLLING);
	}
	
	public String getText(){
		WebElement webElement = super.waitForAndFindElement();
		if (webElement != null) {
			return webElement.getText();
		}
		return null;		
	}

}
