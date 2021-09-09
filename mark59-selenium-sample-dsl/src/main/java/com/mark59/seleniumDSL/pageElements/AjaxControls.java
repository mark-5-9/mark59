package com.mark59.seleniumDSL.pageElements;

import java.time.Duration;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * This code or very similar can be found at multiple web sites such as StackOverflow.  It's purpose it to 
 * check for a condition that means a triggered JQuery Ajax call has completed.
 * 
 * <p>While so useful function anymore with the decline of Ajax, this code does gives a template
 * about how to go about creating a WebDriverWait with a custom ExpectedCondition     
 *
 */
public class AjaxControls {
	
	public void waitForAjaxTriggeredViaJQueryToFinish(WebDriver driver){
		
		 new WebDriverWait(driver, Duration.ofSeconds(20), Duration.ofMillis(200)).until(new ExpectedCondition<Boolean>()
		 {
		     public Boolean apply(WebDriver driver) {
		         JavascriptExecutor js = (JavascriptExecutor) driver;
		         Boolean activejQeuryAjax = (Boolean)js.executeScript("return jQuery.active == 0");
		         System.out.println("activejQeuryAjax : " + activejQeuryAjax  );
		         return activejQeuryAjax;
		 }
		 });		
		
	}
}
