package com.mark59.seleniumDSL.pageElements;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

import com.mark59.core.utils.SafeSleep;
import com.mark59.seleniumDSL.core.Elemental;
import com.mark59.seleniumDSL.core.FluentWaitFactory;

public class InputTextElement extends Elemental {


	public InputTextElement(WebDriver driver, String id) {
		this(driver, By.id(id));
	}

	public InputTextElement(WebDriver driver, By by) {
		super(driver, by, FluentWaitFactory.DEFAULT_TIMEOUT, FluentWaitFactory.DEFAULT_POLLING);
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
	
	public String getText() {
		return waitForAndFindElement().getAttribute("value");
	}

}
