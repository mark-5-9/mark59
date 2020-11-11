package com.mark59.seleniumDSL.pageElements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.mark59.seleniumDSL.core.Elemental;
import com.mark59.seleniumDSL.core.FluentWaitFactory;
import com.mark59.seleniumDSL.core.SafeSleep;



public class Link extends Elemental {


	public Link(WebDriver driver, String linkText) {
		this(driver, By.linkText(linkText));
	}	
	
	public Link(WebDriver driver, By by) {
		super(driver, by, FluentWaitFactory.DEFAULT_TIMEOUT, FluentWaitFactory.DEFAULT_POLLING);
	}

	public Link waitUntilClickable() {
		return (Link) super.waitUntilClickable();
	}
	
	public Link waitUntilClickable(Elemental elemental) {
		super.waitUntilCondition(ExpectedConditions.elementToBeClickable(elemental.getBy()));
		return  this;
	}
	
	public Link click() {
		return (Link) super.click();
	}

	public Link waitUntilStale(Elemental elemental) {
		return (Link) super.waitUntilStale(elemental);
	}

	public Link waitUntilTextPresent(String expectedText) {
		super.waitUntilTextPresentInElement(this, expectedText);
		return this;
	}

	public Elemental waitUntilTextPresentInElement(Elemental elemental, String expectedText) {
		return super.waitUntilTextPresentInElement(elemental, expectedText);
	}

	public Link thenSleep() {
		thenSleep(1000);
		return this;
	}
	
	public Link thenSleep(long sleepDuration){
		SafeSleep.sleep(sleepDuration);
		return this;
	}

	
}