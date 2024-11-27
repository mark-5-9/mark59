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

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mark59.core.JmeterFunctionsImpl;
import com.mark59.core.utils.Mark59LogLevels;
import com.mark59.scripting.AbstractJmeterFunctionsUiCommon;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Tracing;
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
 * <p>From JMeter, Playwright scripts (that extend {@link PlaywrightAbstractJavaSamplerClient}) have been provisioned to have transaction-level 
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

	private Page page;


	/**
	 * @param context the JMeter JavaSamplerContext 
	 * @param jmeterRuntimeArgumentsMap used to override default state of Mark59 log output
	 */
	public JmeterFunctionsForPlaywrightScripts(JavaSamplerContext context,
			Map<String, String> jmeterRuntimeArgumentsMap) {
		super(context, jmeterRuntimeArgumentsMap);
	}
	
		
	/**
	 * @return page a playwright page 
	 */
	public Page getPage() {
		return page;
	}


	/**
	 * @param page a playwright page
	 */
	public void setPage(Page page) {
		this.page = page;
	}


	/**
	 * (Playwright Only) As per {@link #writeScreenshot(String)}, but allows user to pass any desired 
	 * Playwright Page and optionally ScreenshotOptions.
	 * 
	 * @param page a Playwright Page object 
	 * @param imageName last part of the log filename (but excluding extension - which is set as '.jpg')
	 * @param options Playwright ScreenshotOptions   
	 */
	public void writePageScreenshot(Page page, String imageName, ScreenshotOptions options) {
		writeLog(imageName,"jpg", page.screenshot(options));
	}	
	
	
	/**
	 * (Playwright Only) As perAs per {@link #bufferScreenshot(String)}, but allows user to pass any desired 
	 * Playwright Page and optionally set ScreenshotOptions.
	 * <p>If you want to immediately write a screenshot to file, use {@link #writeScreenshot(String)}  instead.
	 * 
	 * @param page a Playwright Page object 
	 * @param imageName last part of the log filename (but excluding extension - which is set as '.jpg') 
	 * @param options Playwright ScreenshotOptions  
	 * 
	 * @see JmeterFunctionsImpl#writeBufferedArtifacts()
	 * @see PlaywrightAbstractJavaSamplerClient#UiScriptExecutionAndExceptionsHandling(JavaSamplerContext, Map, String)
	 */
	public void bufferScreenshot(Page page, String imageName, ScreenshotOptions options) {
		bufferLog(imageName,"jpg", page.screenshot(options));	
	}
	
	
	/**
	 * Capture and immediately output one or more screenshot (.jpg) logs. Use with caution in a Performance 
	 * and Volume test as misuse of this method may produce many more screenshots than intended.
	 * <p>Instead, you could use {@link #bufferScreenshot(String)} and {@link #writeBufferedArtifacts()}.
	 * <p>All pages associated with the {@link PlaywrightAbstractJavaSamplerClient#browser}, are screenshot, so 
	 * more than one file can potentially be written when invoking this method.
	 *   
	 * @param imageName last part of the log filename (but excluding extension - which is set as '.jpg')  
	 */
	@Override	
	public void writeScreenshot(String imageName) {
		List<Page> ctxPages = listBrowerCtxPages();
		for (int i = 0; i < ctxPages.size(); i++) {
			writeLog(unique(imageName,ctxPages,i), "jpg", ctxPages.get(i).screenshot());
		} 
	}	


	/**
	 * Stores one or more screenshot (.jpg) logs in memory, ready to be written to file later.  
	 * If you want to immediately write a screenshot to file, use {@link #writeScreenshot(String)} instead.
	 * <p>All pages associated with the {@link PlaywrightAbstractJavaSamplerClient#browser} are screenshot, so 
	 * more than one file can potentially be buffered when invoking this method.
	 *    
	 * @param imageName last part of the log filename (but excluding extension - which is set as '.jpg')
	 *  
	 * @see JmeterFunctionsImpl#writeBufferedArtifacts()
	 * @see PlaywrightAbstractJavaSamplerClient#UiScriptExecutionAndExceptionsHandling(JavaSamplerContext, Map, String)
	 */
	@Override
	public void bufferScreenshot(String imageName) {
		List<Page> ctxPages = listBrowerCtxPages();
		for (int i = 0; i < ctxPages.size(); i++) {
			bufferLog(unique(imageName,ctxPages,i), "jpg", ctxPages.get(i).screenshot());
		}	
	}
	
	
	/**
	 * Capture and immediately output a page source (.html) log. Use with caution in a Performance and Volume 
	 * test as misuse of this method may produce many more screenshots than intended. 
	 * <p>Instead, you could use {@link #bufferPageSource(String)} and {@link #writeBufferedArtifacts()}.
	 * <p>All pages associated with the {@link PlaywrightAbstractJavaSamplerClient#browser}, are have their
	 * page source taken, so more than one file can potentially be written when invoking this method.
	 * 
	 * @param imageName last part of the log filename (but excluding extension - which is set as '.html')
	 */
	public void writePageSource(String imageName) {
		List<Page> ctxPages = listBrowerCtxPages();
		for (int i = 0; i < ctxPages.size(); i++) {
			writeLog(unique(imageName,ctxPages,i), "html", captureCurrentUrlAndtHtmlPageSource(ctxPages.get(i)).getBytes());
		}
	}	

	
	/**
	 * Stores a page source (.html) log in memory, ready to be written to file later.  
	 * If you want to immediately write a screenshot to file, use {@link #writePageSource(String)} instead.
	 * <p>All pages associated with the {@link PlaywrightAbstractJavaSamplerClient#browser}, are have their
	 * page source taken, so more than one file can potentially be buffered when invoking this method.
	 * 
	 * @param imageName last part of the log filename (but excluding extension - which is set as '.html')   
	 */
	public void bufferPageSource(String imageName) {
		List<Page> ctxPages = listBrowerCtxPages();
		for (int i = 0; i < ctxPages.size(); i++) {
			bufferLog(imageName, "html", captureCurrentUrlAndtHtmlPageSource(ctxPages.get(i)).getBytes());
		}
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
	 * Convenience method to start a Playwright trace, with StartOptions set to true.
	 * <br>Only intended to be used when debugging a script.  Use with extreme care in a performance test!  
	 * <p><code>
	 * page.context().tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true).setSources(true));
	 * </code>
	 * @see #stopPlayWrightTrace(String)
	 */
	public void startPlayWrightTrace() {
		page.context().tracing()
				.start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true).setSources(true));
	}

	
	/**
	 * Convenience method to stop a Playwright trace, and set location of the trace zip file. 
	 * <p><b>Sample Usage:</b><br>
	 * <code>jm.stopPlayWrightTrace("C:/Test/trace.zip");</code>
	 * <p>This would put the trace.zip file in you project root when running from Eclipse:<br>
	 * <code>jm.stopPlayWrightTrace("trace.zip");</code>
	 * @param tracePath  location of the trace zip file
	 * @see #startPlayWrightTrace()
	 */
	public void stopPlayWrightTrace(String tracePath) {
		page.context().tracing().stop(new Tracing.StopOptions().setPath(Paths.get(tracePath)));	
	}
	
	
	/**
	 * @return returns a string describing the url and full contents of the current page
	 */
	private String captureCurrentUrlAndtHtmlPageSource(Page ctxPage) {
		String currentURL = ctxPage.url();
		String pageSource = ctxPage.content();
		return "<!--  Page CurrentUrl : " + currentURL + " --> \n" +  pageSource;
	}
	

	private List<Page> listBrowerCtxPages() {
		List<Page> ctxPages = new ArrayList<Page>(); 
		List<BrowserContext> browserContexts =  new ArrayList<BrowserContext>(); 
		try {
			browserContexts = page.context().browser().contexts();
		} catch (Exception e) { // just return the original page object
			ctxPages.add(page);
			return ctxPages; 
		}
		
		for (BrowserContext browserContext : browserContexts){
			for (Page cxtPage: browserContext.pages()) {
				ctxPages.add(cxtPage);
			}
		}
		return ctxPages;
	}

	
	/**
	 * @param imageName imageName
	 * @param ctxPages list of pages
	 * @param ctxPageIx current index on ctxPages 
	 * @return a unique image name, using a counter and the page title (first 20 chars)
	 */
	private String unique(String imageName, List<Page> ctxPages, int ctxPageIx) {
		String uniqueImageName = imageName;
		if (ctxPages.size()>1) {
			uniqueImageName += "_" + (ctxPageIx+1) + "_" + 
					StringUtils.left(Objects.toString(ctxPages.get(ctxPageIx).title(),"noTitle").replaceAll("[^a-zA-Z0-9]",""), 20);
		}
		return uniqueImageName;
	}

}