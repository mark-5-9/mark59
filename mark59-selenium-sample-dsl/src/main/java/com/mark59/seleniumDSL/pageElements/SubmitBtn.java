package com.mark59.seleniumDSL.pageElements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.mark59.seleniumDSL.core.Elemental;
import com.mark59.seleniumDSL.core.FluentWaitFactory;


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
	
	public Elemental submit() {
		return super.click();
	}

}