package com.mark59.seleniumDSL.pageElements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.mark59.seleniumDSL.core.Elemental;
import com.mark59.seleniumDSL.core.FluentWaitFactory;


public class CheckBox extends Elemental {
	
	public CheckBox(WebDriver driver, String id) {
		this(driver, By.id(id));
	}

	public CheckBox(WebDriver driver, By by) {
		super(driver, by, FluentWaitFactory.DEFAULT_TIMEOUT, FluentWaitFactory.DEFAULT_POLLING);

	}

	public boolean isChecked() {
		return super.waitForAndFindElement().isSelected();
	}
	
}