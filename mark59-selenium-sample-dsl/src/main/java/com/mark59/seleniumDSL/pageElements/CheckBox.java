package com.mark59.seleniumDSL.pageElements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.mark59.seleniumDSL.core.Elemental;
import com.mark59.seleniumDSL.core.FluentWaitFactory;
import com.mark59.seleniumDSL.core.SafeSleep;



public class CheckBox extends Elemental {
	
	public CheckBox(WebDriver driver, String id) {
		this(driver, By.id(id));
	}

	public CheckBox(WebDriver driver, By by) {
		super(driver, by, FluentWaitFactory.DEFAULT_TIMEOUT, FluentWaitFactory.DEFAULT_POLLING);

	}

	public CheckBox waitUntilClickable() {
		return (CheckBox) super.waitUntilClickable();
	}

	public CheckBox waitUntilClickable(Elemental elemental) {
		super.waitUntilCondition(ExpectedConditions.elementToBeClickable(elemental.getBy()));
		return  this;
	}
	
	public CheckBox click() {
		return (CheckBox) super.click();
	}


	public boolean isChecked() {
		return super.waitForAndFindElement().isSelected();
	}

	public CheckBox waitUntilStale() {
		return (CheckBox) waitUntilStale(this);
	}

	public CheckBox waitUntilStale(Elemental elemental) {
		return (CheckBox) super.waitUntilStale(elemental);
	}

	public Elemental waitUntilTextPresentInElement(Elemental elemental, String expectedText) {
		return super.waitUntilTextPresentInElement(elemental, expectedText);
	}
	
	public CheckBox thenSleep() {
		thenSleep(1000);
		return this;
	}
	
	public CheckBox thenSleep(long sleepDuration){
		SafeSleep.sleep(sleepDuration);
		return this;
	}
	
	
	
	
	
}