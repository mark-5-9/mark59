package com.mark59.seleniumDSL.pageElements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.mark59.seleniumDSL.core.Elemental;
import com.mark59.seleniumDSL.core.FluentWaitFactory;
import com.mark59.seleniumDSL.core.SafeSleep;


public class OptionButton extends Elemental {

	public OptionButton(WebDriver driver, String id) {
		this(driver, By.id(id));
	}
	
	public OptionButton(WebDriver driver, By by) {
		super(driver, by, FluentWaitFactory.DEFAULT_TIMEOUT, FluentWaitFactory.DEFAULT_POLLING);
	}

	public OptionButton waitUntilClickable() {
		return (OptionButton) super.waitUntilClickable();
	}
	
	public OptionButton waitUntilClickable(Elemental elemental) {
		super.waitUntilCondition(ExpectedConditions.elementToBeClickable(elemental.getBy()));
		return  this;
	}
	
	public OptionButton click() {
		return (OptionButton) super.click();
	}
	
	public OptionButton waitUntilStale() {
		return (OptionButton) waitUntilStale(this);
	}

	public OptionButton waitUntilStale(Elemental elemental) {
		return (OptionButton) super.waitUntilStale(elemental);
	}
		
	public Elemental waitUntilTextPresentInElement(Elemental elemental, String expectedText) {
		return super.waitUntilTextPresentInElement(elemental, expectedText);
	}
	

	public OptionButton thenSleep() {
		thenSleep(1000);
		return this;
	}
	
	public OptionButton thenSleep(long sleepDuration){
		SafeSleep.sleep(sleepDuration);
		return this;
	}

}
