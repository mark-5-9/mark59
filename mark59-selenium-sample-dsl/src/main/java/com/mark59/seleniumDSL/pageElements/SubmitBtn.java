package com.mark59.seleniumDSL.pageElements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.mark59.seleniumDSL.core.Elemental;
import com.mark59.seleniumDSL.core.FluentWaitFactory;
import com.mark59.seleniumDSL.core.SafeSleep;


public class SubmitBtn extends Elemental {

	public SubmitBtn(WebDriver driver, String id) {
		this(driver, By.id(id));
	}
	
	public SubmitBtn(WebDriver driver, By by) {
		super(driver, by, FluentWaitFactory.DEFAULT_TIMEOUT, FluentWaitFactory.DEFAULT_POLLING);
	}

	public SubmitBtn waitUntilClickable() {
		return (SubmitBtn) super.waitUntilClickable();
	}
	
	public SubmitBtn submit() {
		return (SubmitBtn) super.click();
	}

	public SubmitBtn waitUntilClickable(Elemental elemental) {
		super.waitUntilCondition(ExpectedConditions.elementToBeClickable(elemental.getBy()));
		return  this;
	}
	
	public SubmitBtn waitUntilStale() {
		return (SubmitBtn) waitUntilStale(this);
	}

	public SubmitBtn waitUntilStale(Elemental elemental) {
		return (SubmitBtn) super.waitUntilStale(elemental);
	}
	
	public SubmitBtn waitUntilTextPresent(String expectedText) {
		super.waitUntilTextPresentInElement(this, expectedText);
		return this;
	}

	public Elemental waitUntilTextPresentInElement(Elemental elemental, String expectedText) {
		return super.waitUntilTextPresentInElement(elemental, expectedText);
	}
	
	public void waitUntilTextPresentInTitle(String expectedText) {
		super.waitUntilTextPresentInTitle(expectedText);
	}

	public SubmitBtn thenSleep() {
		thenSleep(1000);
		return this;
	}
	
	public SubmitBtn thenSleep(long sleepDuration){
		SafeSleep.sleep(sleepDuration);
		return this;
	}
	

}