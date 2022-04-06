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
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import com.mark59.core.utils.ScreenshotLoggingHelper;

/**
 * @author Michael Cohen
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class ChromeDriverWrapper extends SeleniumDriverWrapper {
	
	private static final Logger LOG = LogManager.getLogger(ChromeDriverWrapper.class);

	/**
	 * @param dataPackage the WebDrive to be 'packaged'
	 */
	public ChromeDriverWrapper(WebDriver dataPackage) {
		super(dataPackage);
	}

	
	@Override
	public String getDriverLogs() {
		if (!this.getDriverPackage().manage().logs().getAvailableLogTypes().contains(LogType.PERFORMANCE))
			return null;

		List<LogEntry> logs = this.getDriverPackage().manage().logs().get(LogType.PERFORMANCE).getAll();

		StringBuilder compoundLogBuilder = new StringBuilder();

		for (LogEntry entry : logs) {
			compoundLogBuilder.append(entry.toString()).append("\n");
		}

		return compoundLogBuilder.toString();
	}

	
	@Override
	public void clearDriverLogs() {
		if (!this.getDriverPackage().manage().logs().getAvailableLogTypes().contains(LogType.PERFORMANCE))
			return;

		this.getDriverPackage().manage().logs().get(LogType.PERFORMANCE).getAll();
	}

	
	@Override
	public void writeDriverLogs(String textFileName) {
		if (LOG.isTraceEnabled())
			LOG.trace(Thread.currentThread().getName() + " : writing driver log, (partial) name " + textFileName);

		if (this.getDriverLogs() != null) {
			ScreenshotLoggingHelper.writeScreenshotLog(
					new File(ScreenshotLoggingHelper.buildFullyQualifiedImageName(textFileName, "txt")), this.getDriverLogs().getBytes());
		}
	}
	
	
	@Override	
	public void bufferDriverLogs(String textFileName) {
		if (this.getDriverLogs() != null) {
			bufferedArtifacts.put(ScreenshotLoggingHelper.buildFullyQualifiedImageName(textFileName, "txt"), this.getDriverLogs().getBytes() );
		}
	}

}
