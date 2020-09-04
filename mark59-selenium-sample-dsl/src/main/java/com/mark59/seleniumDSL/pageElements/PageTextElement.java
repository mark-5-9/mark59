package com.mark59.seleniumDSL.pageElements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.mark59.seleniumDSL.core.Elemental;
import com.mark59.seleniumDSL.core.FluentWaitFactory;
import com.mark59.seleniumDSL.core.SafeSleep;

;

public class PageTextElement extends Elemental {
	

	public PageTextElement(WebDriver driver, String id) {
		this(driver, By.id(id));
	}

	public PageTextElement(WebDriver driver, By by) {
		super(driver, by, FluentWaitFactory.DEFAULT_TIMEOUT, FluentWaitFactory.DEFAULT_POLLING);
	}
	
	public PageTextElement waitUntilClickable() {
		return (PageTextElement) super.waitUntilClickable();
	}

	public PageTextElement waitUntilClickable(Elemental elemental) {
		waitUntilCondition(ExpectedConditions.elementToBeClickable(elemental.getBy()));
		return  this;
	}
	
	public PageTextElement click() {
		return (PageTextElement) super.click();
	}

	public PageTextElement waitUntilTextPresent(String expectedText) {
		super.waitUntilTextPresentInElement(this, expectedText);
		return this;
	}

	public Elemental waitUntilTextPresentInElement(Elemental elemental, String expectedText) {
		return super.waitUntilTextPresentInElement(elemental, expectedText);
	}
	
	public PageTextElement thenSleep() {
		thenSleep(1000);
		return this;
	}
	
	public PageTextElement thenSleep(long sleepDuration){
		SafeSleep.sleep(sleepDuration);
		return this;
	}
	
	
	public String getText(){
		return super.waitForAndFindElement().getText();
	}


	public By getBy() {
		return by;
	}

	public void setBy(By by) {
		this.by = by;
	}

}
