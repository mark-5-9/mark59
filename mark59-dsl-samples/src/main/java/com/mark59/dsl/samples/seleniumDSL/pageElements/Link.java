package com.mark59.dsl.samples.seleniumDSL.pageElements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.mark59.dsl.samples.seleniumDSL.core.Elemental;
import com.mark59.dsl.samples.seleniumDSL.core.FluentWaitFactory;


public class Link extends Elemental {


	public Link(WebDriver driver, String linkText) {
		this(driver, By.linkText(linkText));
	}	
	
	public Link(WebDriver driver, By by) {
		super(driver, by, FluentWaitFactory.DEFAULT_TIMEOUT, FluentWaitFactory.DEFAULT_POLLING);
	}
	
}