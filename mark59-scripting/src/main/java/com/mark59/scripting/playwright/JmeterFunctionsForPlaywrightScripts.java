/*
 *  Copyright 2019 Mark59.com
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

package com.mark59.scripting.playwright;

import java.util.Map;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mark59.core.JmeterFunctionsImpl;
import com.mark59.core.utils.Mark59LogLevels;
import com.mark59.scripting.AbstractJmeterFunctionsUiCommon;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.ScreenshotOptions;

/**
 * Playwright flavored extension of the Mark59 class {@link AbstractJmeterFunctionsUiCommon} ( whose primary purpose to to handle transaction
 *  results,implemented in Mark59 by the use of 'sub-results' within a {@link SampleResult} )    
 * 
 * <p>This class is designed to additionally implement Playwright related functions within Mark59, in particular logging.
 * 
 * <p>At instantiation, transaction level logging usage is set, based on the log4j level.  This can be over-ridden via JMeter parameters 
 * and/or directly calling the methods in this class from the script.  
 * Please refer to {@link #overrideTxnLoggingBehaviourUsingJmeterParameters(Map)}</p>
 * 
 * <p>From JMeter, Selenium scripts (that extend {@link PlaywrightAbstractJavaSamplerClient}) have been provisioned to have transaction-level 
 * 'logging settings' available.<br>
 * 
 * <p>Current default outputs setting are :
 *  
 * <p> Trace Level:
 *  <ul>
 *  <li> 	write Screenshots At Start and End OfTransactions
 *  <li> 	write Page Source At Start and End OfTransactions	    
 *  </ul>
 *  <p> Debug Level:
 *  <ul>
 *  <li>	write Screenshots End Of Transactions
 *  <li> 	write Page Source End Of Transactions	    
 * </ul>
 * <p>No screenshot/log transaction level at Info or above, except for exception handling, which occurs regardless of log4j level 
 *   
 * <p>For debugging a troublesome script during execution, note that 'buffered' logs can be set in script 
 *   (For example see {@link #logAllLogsAtEndOfTransactions(Mark59LogLevels)} )
 * 
 * <p>Finer grained control within a script can be achieved using methods to setting the individual logging flags.
 * 
 * <p>An example of transaction-level logging in use:  
 * <pre><code>
 * if (LOG.isInfoEnabled()) jm.logScreenshotsAtStartOfTransactions(Mark59LogLevels.WRITE);
 * jm.startTransaction("Some-transaction-whose-screenshot-i-want-to-see-at-the-start-when-at-log4j-INFO-level");
 *       :
 * //OK, now just go back to the default behavior for the current Log4j level..  	 
 * if (LOG.isInfoEnabled()) jm.logScreenshotsAtStartOfTransactions(Mark59LogLevels.DEFAULT);
 * </code></pre>
 * 
 * @see Mark59LogLevels
 * @see JmeterFunctionsImpl
 * @see PlaywrightAbstractJavaSamplerClient#scriptExceptionHandling(JavaSamplerContext, Map, Throwable)
 * 
 * @author Philip Webb
 * Written: Australian Summer 2023/24  
 */
public class JmeterFunctionsForPlaywrightScripts extends AbstractJmeterFunctionsUiCommon {

	/** log4J class logger */
	public static final Logger LOG = LogManager.getLogger(JmeterFunctionsForPlaywrightScripts.class);

	private final Page page;
	

	/**
	 * @param context the JMeter JavaSamplerContext 
	 * @param page  the Playwright page (intended to be the Page object used by within a script)  
	 * @param jmeterRuntimeArgumentsMap used to override default state of Mark59 log output
	 */
	public JmeterFunctionsForPlaywrightScripts(JavaSamplerContext context, Page page, 
			Map<String, String> jmeterRuntimeArgumentsMap) {		
		super(context, jmeterRuntimeArgumentsMap);
		this.page = page;
	}
	
	
	/**
	 * Capture and immediately output a screenshot (.jpg) log. Use with caution in a Performance and Volume 
	 * test as misuse of this method may produce many more screenshots than intended. 
	 * <p>Instead, you could use {@link #bufferScreenshot(String)} and {@link #writeBufferedArtifacts()}.
	 * @param imageName last part of the log filename (but excluding extension - which is set as '.jpg')  
	 */
	public void writeScreenshot(String imageName) {
		writeLog(imageName,"jpg", page.screenshot());
	}	

	
	/**
	 * As per {@link #writeScreenshot(String)}, but allows user to pass desired Playwright ScreenshotOptions
	 * @param imageName last part of the log filename (but excluding extension - which is set as '.jpg')
	 * @param options Playwright ScreenshotOptions   
	 */
	public void writeScreenshot(String imageName, ScreenshotOptions options) {
		writeLog(imageName,"jpg", page.screenshot(options));
	}	
	
	
	/**
	 * Stores a screenshot (.jpg) log in memory, ready to be written to file later.  
	 * If you want to immediately write a screenshot to file, use {@link #writeScreenshot(String)}  instead.
	 * @param imageName last part of the log filename (but excluding extension - which is set as '.jpg')   
	 */
	public void bufferScreenshot(String imageName) {
		bufferLog(imageName,"jpg", page.screenshot());	
	}

	
	/**
	 * As per {@link #bufferScreenshot(String)}, but allows user to pass desired Playwright ScreenshotOptions
	 * If you want to immediately write a screenshot to file, use {@link #writeScreenshot(String)}  instead.
	 * @param imageName last part of the log filename (but excluding extension - which is set as '.jpg') 
	 * @param options Playwright ScreenshotOptions   
	 */
	public void bufferScreenshot(String imageName, ScreenshotOptions options) {
		bufferLog(imageName,"jpg", page.screenshot(options));	
	}
	
	
	
	/**
	 * Capture and immediately output a page source (.html) log. Use with caution in a Performance and Volume 
	 * test as misuse of this method may produce many more screenshots than intended. 
	 * <p>Instead, you could use {@link #bufferPageSource(String)} and {@link #writeBufferedArtifacts()}.
	 * @param imageName last part of the log filename (but excluding extension - which is set as '.html')
	 */
	public void writePageSource(String imageName) {
		writeLog(imageName, "html", captureCurrentUrlAndtHtmlPageSource().getBytes());
	}	

	
	/**
	 * Stores a page source (.html) log in memory, ready to be written to file later.  
	 * If you want to immediately write a screenshot to file, use {@link #writePageSource(String)} instead.
	 * @param imageName last part of the log filename (but excluding extension - which is set as '.html')   
	 */
	public void bufferPageSource(String imageName) {
		bufferLog(imageName, "html", captureCurrentUrlAndtHtmlPageSource().getBytes());
	}

	
	/**
	 * Note Performance logging is only implemented for selenium scripts
	 * @param textFileName last part of the log filename (excluding extension)
	 */
	public void writeDriverPerfLogs(String textFileName) {
		// no-op
	}

	
	/**
	 * Note Performance logging is only implemented for selenium scripts
	 * @param textFileName last part of the log filename (excluding extension) 
	 */
	public void bufferDriverPerfLogs(String textFileName) {
		// no-op
	}
	
	
	/**
	 * @return returns a string describing the url and full contents of the current page
	 */
	private String captureCurrentUrlAndtHtmlPageSource() {
		String currentURL = page.url();
		String pageSource = page.content();
		return "<!--  Page CurrentUrl : " + currentURL + " --> \n" +  pageSource;
	}

}