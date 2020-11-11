package com.mark59.seleniumDSL.pageElements;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AjaxControls {
	
	public void waitForAjaxTriggeredViaJQueryToFinish(WebDriver driver){
		
		 new WebDriverWait(driver, 20).until(new ExpectedCondition<Boolean>()
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
