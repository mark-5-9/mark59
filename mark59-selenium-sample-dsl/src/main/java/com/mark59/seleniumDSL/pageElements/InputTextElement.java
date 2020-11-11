package com.mark59.seleniumDSL.pageElements;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.mark59.seleniumDSL.core.Elemental;
import com.mark59.seleniumDSL.core.FluentWaitFactory;
import com.mark59.seleniumDSL.core.SafeSleep;

public class InputTextElement extends Elemental {


	public InputTextElement(WebDriver driver, String id) {
		this(driver, By.id(id));
	}

	public InputTextElement(WebDriver driver, By by) {
		super(driver, by, FluentWaitFactory.DEFAULT_TIMEOUT, FluentWaitFactory.DEFAULT_POLLING);
	}
	
	public InputTextElement waitUntilClickable() {
		return (InputTextElement) super.waitUntilClickable();
	}

	public InputTextElement waitUntilClickable(Elemental elemental) {
		waitUntilCondition(ExpectedConditions.elementToBeClickable(elemental.getBy()));
		return  this;
	}
	
	public InputTextElement click() {
		return (InputTextElement) super.click();
	}


	public InputTextElement type(String text) {
		return flexiType(text, false);
	}


	public InputTextElement typeSlowly(String text) {
		return flexiType(text, true);
	}

	private InputTextElement flexiType(String text, boolean typeSlow) {
		if (typeSlow) {
			for (char character : text.toCharArray()) {
				typeAndWait(String.valueOf(character));
			}
		} else {
			waitForAndFindElement().sendKeys(text);
		}
		return this;
	}

	private InputTextElement typeAndWait(String text) {
		waitForAndFindElement().sendKeys(text);
		SafeSleep.sleep(500);
		return this;
	}

	public InputTextElement typeAndTabOutToNextField(String text) {
		type(text);
		tabKey();
		return this;
	}

	public InputTextElement tabKey() {
		super.waitForAndFindElement().sendKeys(Keys.TAB);
		return this;
	}

	public InputTextElement clear() {
		waitForAndFindElement().clear();
		return this;
	}	

	public InputTextElement waitUntilStale() {
		return (InputTextElement) waitUntilStale(this);
	}

	public InputTextElement waitUntilStale(Elemental elemental) {
		return (InputTextElement) super.waitUntilStale(elemental);
	}
	
	public InputTextElement waitUntilTextPresent(String expectedText) {
		super.waitUntilTextPresentInElement(this, expectedText);
		return this;
	}

	public Elemental waitUntilTextPresentInElement(Elemental elemental, String expectedText) {
		return super.waitUntilTextPresentInElement(elemental, expectedText);
	}
	
	public String getText() {
		return waitForAndFindElement().getAttribute("value");
	}
	
	public InputTextElement thenSleep() {
		thenSleep(1000);
		return this;
	}
	
	public InputTextElement thenSleep(long sleepDuration){
		SafeSleep.sleep(sleepDuration);
		return this;
	}
	
	public By getBy() {
		return by;
	}

	public void setBy(By by) {
		this.by = by;
	}

}
