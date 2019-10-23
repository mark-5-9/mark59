package com.mark59.seleniumDSL.pageElements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import com.mark59.seleniumDSL.core.Elemental;
import com.mark59.seleniumDSL.core.FluentWaitFactory;
import com.mark59.seleniumDSL.core.SafeSleep;



public class DropdownList extends Elemental {

	public DropdownList(WebDriver driver, String id) {
		this(driver, By.id(id));
	}

	public DropdownList(WebDriver driver, String id, String value) {
		this(driver, By.cssSelector("#" + id + " option[value='" + value + "']"));
	}

	public DropdownList(WebDriver driver, By by) {
		super(driver, by, FluentWaitFactory.DEFAULT_TIMEOUT, FluentWaitFactory.DEFAULT_POLLING);
	}

	public DropdownList waitUntilClickable() {
		return (DropdownList) super.waitUntilClickable();
	}

	public DropdownList waitUntilClickable(Elemental elemental) {
		super.waitUntilCondition(ExpectedConditions.elementToBeClickable(elemental.getBy()));
		return  this;
	}
	
	public DropdownList click() {
		return (DropdownList) super.click();
	}
	
	public String getSelectedOptionValue() {
		WebElement element = FluentWaitFactory.getFluentWait(driver, FluentWaitFactory.DEFAULT_TIMEOUT)
				.until(ExpectedConditions.elementToBeClickable(by));

		Select dropdown = new Select(element);
		return dropdown.getFirstSelectedOption().getAttribute("value");
	}

	public DropdownList selectByVisibleTextAndConfirmExpectedValue(String visibleText, String expectedOptionValue) {
		selectByVisibleText(visibleText);
		SafeSleep.sleep(50);
		FluentWaitFactory.getFluentWait(driver, FluentWaitFactory.DEFAULT_TIMEOUT)
				.until(ExpectedConditions.attributeContains(by, "value", expectedOptionValue));

		return this;
	}

	public DropdownList selectByVisibleText(String visibleText) {
		WebElement webElement = FluentWaitFactory.getFluentWait(driver, FluentWaitFactory.DEFAULT_TIMEOUT)
				.until(ExpectedConditions.elementToBeClickable(by));

		Select dropdown = new Select(webElement);
		dropdown.selectByVisibleText(visibleText);

		return this;
	}

	public DropdownList waitUntilTextPresent(String expectedText) {
		super.waitUntilTextPresentInElement(this, expectedText);
		return this;
	}

	public Elemental waitUntilTextPresentInElement(Elemental elemental, String expectedText) {
		return super.waitUntilTextPresentInElement(elemental, expectedText);
	}
	
	public DropdownList thenSleep() {
		thenSleep(1000);
		return this;
	}
	
	public DropdownList thenSleep(long sleepDuration){
		SafeSleep.sleep(sleepDuration);
		return this;
	}
	
}
