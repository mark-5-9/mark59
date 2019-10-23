package com.mark59.datahunter.functionalTest.dsl.pageElements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */

public class Link {

	private WebElement webelement;
	private By by;	
			
	public Link(WebDriver driver, String linkText) {
		by = By.linkText(linkText);
		webelement = driver.findElement (By.linkText(linkText) );
	}
	
	public String getText(){
		return webelement.getText();
	}

	public void click() {
		webelement.click();
		
	}

	public By getBy() {
		return by;
	}

	public void setBy(By by) {
		this.by = by;
	}

	
}

