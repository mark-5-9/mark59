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

package com.mark59.selenium.corejmeterimpl;

import java.util.Map;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.log4j.Logger;

import com.mark59.core.JmeterFunctionsImpl;
import com.mark59.core.Outcome;
import com.mark59.selenium.drivers.SeleniumDriverWrapper;

/**
 * Selenium flavored extension of the Mark59 class {@link JmeterFunctionsImpl} ( whose primary purpose to to handle transaction results, implemented in mark59 by the use 
 * of 'sub-results' within a {@link SampleResult} )    
 * 
 * <p>This class is designed to additionally handle Selenium related functions with the framework, in particular logging and screenshots.  
 * 
 * <p>At instantiation, transaction level logging usage is set, based on the log4j level.  This can be over-ridden vai Jmeter parameters and/or directly calling 
 * the methods in this class from the script.</p>
 * 
 * <p>From jmeter, selenium scripts (those extending SeleniumAbstractJavaSamplerClient) have been provisioned to have transaction-level 'logging settings' available.<br>
 * 
 *  <p>Current default outputs setting are :
 *  
 *  <p> Trace Level:
 *  <ul>
 *  <li> 	write Screenshots At Start and End OfTransactions
 *  <li> 	write Page Source At Start and End OfTransactions	    
 *	<li>	write Performance Log At End Of Transactions	
 *  </ul>
 *  <p> Debug Level:
 *  <ul>
 *  <li>	write Screenshots End Of Transactions
 *  <li> 	write Page Source End Of Transactions	    
 *	<li>	write PerformanceLog At End Of Transactions	
 *  </ul>
 *   <p>No screenshot/log transaction level at Info or above, except for exception handling, which occurs regardless of log4j level 
 *   
 *   <p>For debugging a troublesome script during execution, note that 'buffered' log and screenshots can be set in script (For example see logAllLogsAtEndOfTransactions )
 * 
 * 	 <p>Finer grained control within a script can be achieved using methods to setting the individual logging flags.  For example (also using log4j):
 * 
 * <p> An example of transaction-level logging in use:  
 * <pre><code>
 * if (LOG.isInfoEnabled()) jm.logScreenshotsAtStartOfTransactions(ScreenshotLogging.WRITE);
 * jm.startTransaction("Some-transaction-whose-screenshot-i-want-to-see-at-the-start-when-at-log4j-INFO-level");
 *       :
 * //ok, now just go back to the default behaviour for the current Log4j level..  	 
 * if (LOG.isInfoEnabled()) jm.logScreenshotsAtStartOfTransactions(ScreenshotLogging.DEFAULT);
 * </code></pre>
 * 
 * @see Mark59LogLevels
 * @see JmeterFunctionsImpl
 * 
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class JmeterFunctionsForSeleniumScripts extends JmeterFunctionsImpl {

	private static final Logger LOG = Logger.getLogger(JmeterFunctionsForSeleniumScripts.class);

	private SeleniumDriverWrapper seleniumDriverWrapper;
	
	private boolean writeScreenshotsAtStartOfTransactions = false;
	private boolean writeScreenshotsAtEndOfTransactions = false; 	
	private boolean bufferScreenshotsAtStartOfTransactions = false;
	private boolean bufferScreenshotsAtEndOfTransactions = false; 	
	private boolean writePageSourceAtStartOfTransactions = false;
	private boolean writePageSourceAtEndOfTransactions = false; 	
	private boolean bufferPageSourceAtStartOfTransactions = false;
	private boolean bufferPageSourceAtEndOfTransactions = false; 	
	private boolean writePerformanceLogAtEndOfTransactions = false; 		
	private boolean bufferPerformanceLogAtEndOfTransactions = false; 			
	
	
	/**
	 * @param threadName  current thread name
	 * @param seleniumDriverWrapper  see SeleniumDriverWrapper
	 * @param jmeterRuntimeArgumentsMap used to override default state of screenshot/log output
	 */
	public JmeterFunctionsForSeleniumScripts(String threadName, SeleniumDriverWrapper seleniumDriverWrapper, Map<String,String> jmeterRuntimeArgumentsMap) {
		super(threadName);
		this.seleniumDriverWrapper = seleniumDriverWrapper;
		setDefaultScreenShotTxnLoggingBehaviourBasedOnLog4j();
		overrideScreenShotTxnLoggingUsingJmeterParameters(jmeterRuntimeArgumentsMap);
	}

	
	/**
	 *  Sets the default state of screenshot/log output at transaction level, based of the log4j log level. 
	 *  Please see method overrideScreenShotTxnLoggingUsingJmeterParameters for detailed settings. 
	 */
	private void setDefaultScreenShotTxnLoggingBehaviourBasedOnLog4j() {
		logScreenshotsAtStartOfTransactions(Mark59LogLevels.DEFAULT);
		logScreenshotsAtEndOfTransactions(Mark59LogLevels.DEFAULT);
		logPageSourceAtStartOfTransactions(Mark59LogLevels.DEFAULT);
		logPageSourceAtEndOfTransactions(Mark59LogLevels.DEFAULT);
		logPerformanceLogAtEndOfTransactions(Mark59LogLevels.DEFAULT);
	}

	/**
	 *  Allows parameter overriding of the  default state of screenshot/log output at transaction level, based of the log4j log level.  
	 */
	private void overrideScreenShotTxnLoggingUsingJmeterParameters(Map<String,String> jmeterRuntimeArgumentsMap) {
		Mark59LogLevels logging;
		
		logging = Mark59LogLevels.fromString(jmeterRuntimeArgumentsMap.get(SeleniumDriverWrapper.LOG_SCREENSHOTS_AT_START_OF_TRANSACTIONS));
		if (logging != null) {
			logScreenshotsAtStartOfTransactions(logging);
		}
		logging = Mark59LogLevels.fromString(jmeterRuntimeArgumentsMap.get(SeleniumDriverWrapper.LOG_SCREENSHOTS_AT_END_OF_TRANSACTIONS));
		if (logging != null) { 
			logScreenshotsAtEndOfTransactions(logging);
		}
		logging = Mark59LogLevels.fromString(jmeterRuntimeArgumentsMap.get(SeleniumDriverWrapper.LOG_PAGE_SOURCE_AT_START_OF_TRANSACTIONS));
		if (logging != null) { 
			logPageSourceAtStartOfTransactions(logging);
		}
		logging = Mark59LogLevels.fromString(jmeterRuntimeArgumentsMap.get(SeleniumDriverWrapper.LOG_PAGE_SOURCE_AT_END_OF_TRANSACTIONS));
		if (logging != null) {
			logPageSourceAtEndOfTransactions(logging);
		}
		logging = Mark59LogLevels.fromString(jmeterRuntimeArgumentsMap.get(SeleniumDriverWrapper.LOG_PERF_LOG_AT_END_OF_TRANSACTIONS));
		if (logging != null) {
			logPerformanceLogAtEndOfTransactions(logging);
		}
	}

	
	public void startTransaction(String transactionLabel, boolean includeInStartOfTransactionScreenshotLogs) {
		if (includeInStartOfTransactionScreenshotLogs) {
			if 	(bufferScreenshotsAtStartOfTransactions) {
				bufferScreenshot(transactionLabel + "_before" );
			}
			if 	(writeScreenshotsAtStartOfTransactions) {
				writeScreenshot(transactionLabel + "_before" );
			}
			if 	(bufferPageSourceAtStartOfTransactions) {
				bufferPageSource(transactionLabel + "_source_before" );
			}
			if 	(writePageSourceAtStartOfTransactions) {
				writePageSource(transactionLabel + "_source_before" );
			}
		}
		super.startTransaction(transactionLabel);
	}

	@Override
	public void startTransaction(String transactionLabel) {
		startTransaction(transactionLabel, true);	
	}
	
	
	
	public void endTransaction(String transactionLabel, Outcome result, boolean includeInEndOfTransactionScreenshotLogs) {
		
		super.endTransaction(transactionLabel, result);

		String markIfailedTxn = "";
		if (result.getOutcomeText().equals(Outcome.FAIL.getOutcomeText())){
			markIfailedTxn = "_FAILED";
		}
		
		if (includeInEndOfTransactionScreenshotLogs) {
			if 	(bufferScreenshotsAtEndOfTransactions) {
				bufferScreenshot(transactionLabel + markIfailedTxn + "_ends" );
			}
			if 	(writeScreenshotsAtEndOfTransactions) {
				writeScreenshot(transactionLabel + markIfailedTxn + "_ends" );
			}
			
			if 	(bufferPageSourceAtEndOfTransactions) {
				bufferPageSource(transactionLabel + markIfailedTxn + "_source_at_end" );
			}
			if 	(writePageSourceAtEndOfTransactions) {
				writePageSource(transactionLabel + markIfailedTxn + "_source_at_end" );
			}
					
			if 	(bufferPerformanceLogAtEndOfTransactions) {
				seleniumDriverWrapper.bufferDriverLogs(transactionLabel + markIfailedTxn + "_perflog" );
			}
			if 	(writePerformanceLogAtEndOfTransactions) {
				seleniumDriverWrapper.writeDriverLogs(transactionLabel + markIfailedTxn + "_perflog" );
			}			
		}
	}
	
	public void endTransaction(String transactionLabel, boolean includeInEndOfTransactionshots) {
		endTransaction(transactionLabel, Outcome.PASS, includeInEndOfTransactionshots); 
	}
	
	@Override
	public void endTransaction(String transactionLabel, Outcome result) {
		endTransaction(transactionLabel, result, true); 
	}
	
	@Override
	public void endTransaction(String transactionLabel) {
		endTransaction(transactionLabel, Outcome.PASS, true);
	}
	

	@Override
	public void setTransaction(String transactionLabel, long transactionTime){
		setTransaction(transactionLabel, transactionTime, true, true);
	}
	
	@Override
	public void setTransaction(String transactionLabel, long transactionTime, boolean success) {
		setTransaction(transactionLabel, transactionTime, success, true);
	}

	
	public void setTransaction(String transactionLabel, long transactionTime, boolean success, boolean includeInEndOfTransactionshots) {
		
		super.setTransaction(transactionLabel, transactionTime, success);
		
		if (includeInEndOfTransactionshots) {
			if 	(bufferScreenshotsAtEndOfTransactions) {
				bufferScreenshot(transactionLabel + "_set" );
			}
			if 	(writeScreenshotsAtEndOfTransactions) {
				writeScreenshot(transactionLabel + "_set" );
			}
			if 	(bufferPerformanceLogAtEndOfTransactions) {
				seleniumDriverWrapper.bufferDriverLogs(transactionLabel + "_perflog" );
			}
			if 	(writePerformanceLogAtEndOfTransactions) {
				seleniumDriverWrapper.writeDriverLogs(transactionLabel + "_perflog" );
			}				
			
		}
	}
	
	
	
	/**
	 * Capture and immediately save screenshot. Use with caution! in a Performance and Volume context, misuse of this method may produce many more screenshots
	 * than intended. Instead, we recommend using bufferScreenshot(String) and writeBufferedScreenshots() for any threads with interesting behaviour, such
	 * as an exception.
	 * 
	 * @param imageName filename to use for the screenshot
	 */
	public void writeScreenshot(String imageName) {
		seleniumDriverWrapper.takeScreenshot(imageName);
	}	

	
	/**
	 * Stores screenshot in memory, ready to be written to file later.  If you want to immediately write a screenshot to file, use takeScreenshot instead.
	 * 
	 * @param imageName filename to use for the screenshot
	 */
	public void bufferScreenshot(String imageName) {
		seleniumDriverWrapper.bufferScreenshot(imageName);
	}

	
	public void writePageSource(String imageName) {
		seleniumDriverWrapper.writePageSource(imageName);
	}	
	
	
	public void bufferPageSource(String imageName) {
		seleniumDriverWrapper.bufferPageSource(imageName);
	}

		
	/**
	 * Writes all buffered screenshots/logs to disk (ie, all transaction-level logging performed using a Mark59LogLevels of "BUFFER")
	 * @see Mark59LogLevels
	 */
	public void writeBufferedArtifacts() {
		seleniumDriverWrapper.writeBufferedArtifacts();
	}


	public void logScreenshotsAtStartOfTransactions(Mark59LogLevels screenshotLoggingValue) {
		bufferScreenshotsAtStartOfTransactions = false;
		writeScreenshotsAtStartOfTransactions = false;
		
		if ( Mark59LogLevels.BUFFER.equals(screenshotLoggingValue)) {
			bufferScreenshotsAtStartOfTransactions = true; 
		} else if ( Mark59LogLevels.WRITE.equals(screenshotLoggingValue) ) {
			writeScreenshotsAtStartOfTransactions = true;
		} else if ( Mark59LogLevels.DEFAULT.equals(screenshotLoggingValue) ) {
			
			if (LOG.isTraceEnabled() ) {
				writeScreenshotsAtStartOfTransactions = true;
			} 
		}
	}

	public void logScreenshotsAtEndOfTransactions(Mark59LogLevels screenshotLoggingValue) {
		bufferScreenshotsAtEndOfTransactions = false; 
		writeScreenshotsAtEndOfTransactions = false;
		
		if ( Mark59LogLevels.BUFFER.equals(screenshotLoggingValue) ) {
			bufferScreenshotsAtEndOfTransactions = true; 
		} else if ( Mark59LogLevels.WRITE.equals(screenshotLoggingValue) ) {
			writeScreenshotsAtEndOfTransactions = true;
		} else if ( Mark59LogLevels.DEFAULT.equals(screenshotLoggingValue) ) {

			if (LOG.isTraceEnabled() ) {
				writeScreenshotsAtEndOfTransactions = true;
			} else if (LOG.isDebugEnabled()  ) {
				writeScreenshotsAtEndOfTransactions = true;	
			}  
		}
	}
	
	public void logPageSourceAtStartOfTransactions(Mark59LogLevels screenshotLoggingValue) {
		bufferPageSourceAtStartOfTransactions = false; 
		writePageSourceAtStartOfTransactions = false;	
		
		if ( Mark59LogLevels.BUFFER.equals(screenshotLoggingValue) ) {
			bufferPageSourceAtStartOfTransactions = true; 
		} else if ( Mark59LogLevels.WRITE.equals(screenshotLoggingValue) ) {
			writePageSourceAtStartOfTransactions = true;
		} else if ( Mark59LogLevels.DEFAULT.equals(screenshotLoggingValue) ) {
			
			if (LOG.isTraceEnabled() ) {
				writePageSourceAtStartOfTransactions = true;
			} 
		}
	}

	public void logPageSourceAtEndOfTransactions(Mark59LogLevels screenshotLoggingValue) {
		bufferPageSourceAtEndOfTransactions = false; 
		writePageSourceAtEndOfTransactions = false;
		
		if ( Mark59LogLevels.BUFFER.equals(screenshotLoggingValue) ) {
			bufferPageSourceAtEndOfTransactions = true; 
		} else if ( Mark59LogLevels.WRITE.equals(screenshotLoggingValue) ) {
			writePageSourceAtEndOfTransactions = true;
		} else if ( Mark59LogLevels.DEFAULT.equals(screenshotLoggingValue) ) {

			if (LOG.isTraceEnabled() ) {
				writePageSourceAtEndOfTransactions = true;
			} else if (LOG.isDebugEnabled()  ) {
				writePageSourceAtEndOfTransactions = true;	
			} 
		}
	}
	
	public void logPerformanceLogAtEndOfTransactions(Mark59LogLevels screenshotLoggingValue) {
		bufferPerformanceLogAtEndOfTransactions = false; 
		writePerformanceLogAtEndOfTransactions = false;
		
		if ( Mark59LogLevels.BUFFER.equals(screenshotLoggingValue) ) {
			bufferPerformanceLogAtEndOfTransactions = true; 
		} else if ( Mark59LogLevels.WRITE.equals(screenshotLoggingValue) ) {
			writePerformanceLogAtEndOfTransactions = true;
		} else if ( Mark59LogLevels.DEFAULT.equals(screenshotLoggingValue) ) {

			if (LOG.isTraceEnabled() ) {
				writePerformanceLogAtEndOfTransactions = true;
			} else if (LOG.isDebugEnabled()  ) {
				writePerformanceLogAtEndOfTransactions = true;	
			} 
		}

	}
	
	
	/**
	 * A convenience method which can be used to control all transaction-level logging which occurs when a transaction ends.
	 * <p>For example, in a script
	 * <br><pre><code> jm.logAllLogsAtEndOfTransactions(ScreenshotLogging.BUFFER); </code></pre>
	 * <p>will switch all end of transaction logging output to be 'buffered'.  This would generally be considered the most common method for 
	 * debugging a troublesome script in the first instance.   Output will only occur if the transaction fails. The above code fragment sets the debug flags to :
	 * <ul>  	
	 * <li>  	buffer Screenshots End Of Transactions
	 * <li>  	buffer Page Source End Of Transactions	    
	 * <li>		buffer PerformanceLog At End Of Transactions
	 * </ul> 
	 * 
	 * @param screenshotLoggingValue see explanation above	
	 */
	public void logAllLogsAtEndOfTransactions(Mark59LogLevels screenshotLoggingValue) {
		logScreenshotsAtEndOfTransactions(screenshotLoggingValue);
		logPageSourceAtEndOfTransactions(screenshotLoggingValue);
		logPerformanceLogAtEndOfTransactions(screenshotLoggingValue);
		
	}	

	
	/**
	 * A convenience method which can be used to control transaction-level logging which occurs when a transaction starts.
 	 * That is, this method sets the debug flags to control logging at :
	 * <ul>  	
	 * <li>  	Screenshots at start of transactions
	 * <li>  	Page Source at start of transactions	    
	 * </ul>  
	 * 
	 * 	@param screenshotLoggingValue see explanations above		
	 */
	public void logAllLogsAtStartOfTransactions(Mark59LogLevels screenshotLoggingValue) {
		logScreenshotsAtStartOfTransactions(screenshotLoggingValue);
		logPageSourceAtStartOfTransactions(screenshotLoggingValue);
	}	
	
	
	
}
