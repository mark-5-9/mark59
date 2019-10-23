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

package com.mark59.core;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mark59.core.utils.ScreenshotLoggingHelper;

/**
 * Generic Wrapper class that can be used to pass the driver required by a particular test through the layers of mark59.
 * <p>This wrapper is to encapsulate a driver that knows how to take a screenshot accessible through: protected byte[] driverTakeScreenshot()
 *
 * @param <T> Concrete Driver to be wrapped. Driver is expected to have screenshot capability.
 * 
 * @author Michael Cohen
 * @author Philip Webb
 * Written: Australian Winter 2019 
 */
public abstract class ScreenshotEnabledDriverWrapper<T> extends DriverWrapper<T> {

	private static final Logger LOG = Logger.getLogger(ScreenshotEnabledDriverWrapper.class);

	protected Map<String, byte[]> bufferedArtifacts = new HashMap<>();


	public ScreenshotEnabledDriverWrapper(T driverPackage) {
		super(driverPackage);
	}


	protected abstract byte[] driverTakeScreenshot();


	/**
	 * calls: protected byte[] driverTakeScreenshot() for the concrete
	 * implementation of taking a screenshot for a given driver.
	 * 
	 * Increments screenshotCounter to give each screenshot a unique identifier
	 * 
	 * @return captured screenshot as a byte array
	 */
	protected byte[] takeScreenshot() {
		return driverTakeScreenshot();
	}


	/**
	 * Capture and immediately save screenshot. Use with caution! in a Performance
	 * and Volume context, misuse of this method may produce many more screenshots
	 * than intended. Instead, we recommend using bufferScreenshot(String) and
	 * writeBufferedScreenshots() for any threads with interesting behaviour, such
	 * as an exception.
	 * 
	 * @param imageName filename to use for the screenshot
	 * @return this
	 */
	public ScreenshotEnabledDriverWrapper<T> takeScreenshot(String imageName ) {
		if (LOG.isDebugEnabled()) LOG.debug(Thread.currentThread().getName() + " : taking screenshot with (partial) imageName = " + imageName);

		ScreenshotLoggingHelper.writeScreenshotLog(new File(ScreenshotLoggingHelper.buildFullyQualifiedImageName(imageName)), takeScreenshot());
		return this;
	}

	/**
	 * Stores screenshot in memory, ready to be written to file later.
	 * 
	 * If you want to immediately write a screenshot to file, use takeScreenshot
	 * instead.
	 * 
	 * @param imageName filename to use for the screenshot
	 * @return this
	 */
	public ScreenshotEnabledDriverWrapper<T> bufferScreenshot(String imageName) {
		if (LOG.isDebugEnabled()) LOG.debug(MessageFormat.format("Buffering screenshot {0} for thread {1}", imageName,	Thread.currentThread().getName()));

		bufferedArtifacts.put(ScreenshotLoggingHelper.buildFullyQualifiedImageName(imageName), takeScreenshot());
		return this;
	}


	/**
	 * Writes all buffered screenshots to disk
	 * 
	 * @return this
	 */
	public ScreenshotEnabledDriverWrapper<T> writeBufferedArtifacts() {
		LOG.info(MessageFormat.format("Writing {0} buffered data to disk for thread {1}",
				bufferedArtifacts.size(), Thread.currentThread().getName()));

		for (Entry<String, byte[]> bufferedArtifact : bufferedArtifacts.entrySet()) {
			ScreenshotLoggingHelper.writeScreenshotLog(new File(bufferedArtifact.getKey()) , bufferedArtifact.getValue());
		}

		bufferedArtifacts.clear();
		return this;
	}


	
	@Override
	public void documentExceptionState(Exception e) {
		bufferScreenshot("EXCEPTION");
		writeBufferedArtifacts();
		
		ScreenshotLoggingHelper.writeScreenshotLog(new File(ScreenshotLoggingHelper.buildFullyQualifiedImageName("EXCEPTION", "txt")),
														StringUtils.isNotBlank(e.getMessage()) ? e.getMessage().getBytes() : null);
	}


	
	public Map<String, byte[]> getBufferedScreenshots() {
		return bufferedArtifacts;
	}

}
