/*
 *  Copyright 2019 Insurance Australia Group Limited
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mark59.selenium.drivers;

import java.io.File;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;

import com.mark59.core.ScreenshotEnabledDriverWrapper;
import com.mark59.core.utils.ScreenshotLoggingHelper;

/**
 * @author Michael Cohen
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public abstract class SeleniumDriverWrapper extends ScreenshotEnabledDriverWrapper<WebDriver> {
	
	private static final Logger LOG = Logger.getLogger(SeleniumDriverWrapper.class);
	
	public static final String LOG_SCREENSHOTS_AT_START_OF_TRANSACTIONS = "Log_Screenshots_At_Start_Of_Transactions";
	public static final String LOG_SCREENSHOTS_AT_END_OF_TRANSACTIONS 	= "Log_Screenshots_At_End_Of_Transactions";
	public static final String LOG_PAGE_SOURCE_AT_START_OF_TRANSACTIONS = "Log_Page_Source_At_Start_Of_Transactions";
	public static final String LOG_PAGE_SOURCE_AT_END_OF_TRANSACTIONS	= "Log_Page_Source_At_End_Of_Transactions";
	public static final String LOG_PERF_LOG_AT_END_OF_TRANSACTIONS 		= "Log_Perf_Log_At_End_Of_Transactions";
	
	public static final String DEFAULT = "default";
	public static final String WRITE   = "write";
	public static final String BUFFER  = "buffer";	
	public static final String OFF     = "off";	

	public SeleniumDriverWrapper(WebDriver dataPackage) {
		super(dataPackage);
	}
	

	@Override
	public byte[] driverTakeScreenshot() {
		
		byte[] screenshot = "(screenshot failure)".getBytes();
		
		try {
			screenshot = Base64.decodeBase64(((TakesScreenshot) this.getDriverPackage()).getScreenshotAs(OutputType.BASE64));
		} catch (Exception e) {
			LOG.debug("Screenshot failure ("  + e.getClass().getName() + ")  Message : " + e.getMessage()); 
			LOG.warn("Screenshot failure ("  + e.getClass().getName() + ")  Message line 1 : " + e.getMessage().split("\\r?\\n")[0]); 
			screenshot = ("Screenshot failure ("  + e.getClass().getName() + ")  Message : " + e.getMessage()).getBytes();
		} 
		return screenshot;
	}

	@Override
	public void driverDispose() {
		// adding close() before quit() appears to help chromeDriver cleanup its temp directories
		// https://stackoverflow.com/questions/43289035/chromedriver-not-deleting-scoped-dir-in-temp-folder-after-test-is-complete/
		// However, close() closes the driver connection in Firefox, so wrapping the quit() in a try-catch to bypass Firefox SoSuchSessionException.  
		
		this.getDriverPackage().close();

		try {
			this.getDriverPackage().quit();
		} catch (Exception e) {
			LOG.debug("attempting driver quit() : " + e.getMessage());
		}
	}
	
	
	@Override
	public void documentExceptionState(Exception e) {
		super.documentExceptionState(e);
		writeDriverLogs();
		writePageSource("source_at_EXCEPTION");
	}
	
	public void writeDriverLogs() {
		writeDriverLogs("PERFLOG");
	}

	
	public void writePageSource(String htmlFileName) {
		if (LOG.isDebugEnabled()) LOG.debug(Thread.currentThread().getName() + " : writing html source, (partial) filename " + htmlFileName );		
		
		String sourceWithUrlComment = getCurrentUrlAndtHtmlPageSource(this.getDriverPackage());		
		ScreenshotLoggingHelper.writeScreenshotLog(new File(ScreenshotLoggingHelper.buildFullyQualifiedImageName(htmlFileName, "html")), sourceWithUrlComment.getBytes());
	}

	
	public void bufferPageSource(String htmlFileName){
		String sourceWithUrlComment = getCurrentUrlAndtHtmlPageSource(this.getDriverPackage());				
		bufferedArtifacts.put(ScreenshotLoggingHelper.buildFullyQualifiedImageName(htmlFileName, "html"), sourceWithUrlComment.getBytes() );
	};			

	
	private String getCurrentUrlAndtHtmlPageSource(WebDriver driver) {
		String currentURL = " Unknown ";
		try {
			currentURL = driver.getCurrentUrl();
		} catch (UnhandledAlertException e) {
			LOG.debug("UnhandledAlertException.  Message : " + e.getMessage() );
			LOG.warn("UnhandledAlertException thrown.  Message line 1 : " + e.getMessage().split("\\r?\\n")[0]); 
			currentURL = "URL is not availale.  An UnhandledAlertException Exception has been thrown : " + e.getMessage();
		}
		
		String pageSource = " Not Available "; 
		try {
			pageSource = driver.getPageSource();
		} catch (Exception e ) {
			LOG.debug("Page Source Not Available  ("  + e.getClass().getName() + ")  .  Message : " + e.getMessage()); 
			LOG.warn("Page Source Not Available  ("  + e.getClass().getName() + ")  .  Message line 1 : " + e.getMessage().split("\\r?\\n")[0]); 
			pageSource = "Page Source Not Available.  Error message : " + e.getMessage();
		}

		return "<!--  Driver CurrentUrl : " + currentURL + " --> \n" +  pageSource;
	}

	
	
	public abstract void writeDriverLogs(String textFileName);
		
	public abstract void bufferDriverLogs(String textFileName);	
	
}
