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

package com.mark59.core.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.LocalDate;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Static properties file reader, loading the properties file into memory just once per run to reduce disk I/O.
 * 
 * @author Michael Cohen
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class ScreenshotLoggingHelper {
	private static final Logger LOG = LogManager.getLogger(ScreenshotLoggingHelper.class);	
	
	/**
	 * incremented counter used as part of the screenshot name
	 */
	protected static final String SCREENSHOT_COUNTER = "SCREENSHOT_COUNTER";
	
	private static ScreenshotLoggingHelper instance;
	
	private static Path screenshotDirectory = null;
	
	

	/**
	 * private constructor to ensure deleteDirectory can only be executed in this class (to prevent multiple calls to deleteDirectory)
	 */
	private  ScreenshotLoggingHelper() throws IOException {
		String directory;
		try {
			directory = PropertiesReader.getInstance().getProperty(PropertiesKeys.MARK59_PROP_SCREENSHOT_DIRECTORY);
		} catch (IOException e) {
			LOG.info("Failed to obtain screenshot directory from config (property " + PropertiesKeys.MARK59_PROP_SCREENSHOT_DIRECTORY + " not set");
			return;
		}
		
		if (directory != null ) {
			directory += File.separator + LocalDate.now();
			screenshotDirectory = new File(directory).toPath();
			LOG.info( "Clearing any existing data from Screenshots Directory " + screenshotDirectory);
			FileUtils.deleteDirectory(screenshotDirectory.toFile());
		} else {
			LOG.warn("   As no screenshot directory has been set, attempts to write screenshots or performance logs will fail." );
		}
	}

	
	/**
	 * Returns a fully qualified name for the image, including assigning the .jpg file extension
	 * 
	 * <p>Returned names take the pattern {ThreadName}_{Image Number}_{imageName}.jpg</p>
	 * 
	 * @param imageName filename to use for the screenshot
	 * @return fully qualified image name
	 */
	public static String buildFullyQualifiedImageName(String imageName) {
		return buildFullyQualifiedImageName(imageName, "jpg");
	}

	
	/**
	 * Returns a fully qualified name for the image, including assigning an arbitrary file extension.
	 * 
	 * <p>Returned names take the pattern {Directory}/{ThreadName}_{Image Number}_{imageName}.{extension}</p>
	 * 
	 * @param fileNameEnding filename to use for the screenshot
	 * @param extension file extension
	 * @return fully qualified image name
	 */
	public static String buildFullyQualifiedImageName(String fileNameEnding, String extension) {
		
		String fullyQualifiedImageName = MessageFormat.format("{0}{1}{2}_{3}_{4}.{5}", 
											getScreenshotDirectory(),
											File.separator, 
											Thread.currentThread().getName(), 
											String.format("%03d", StaticCounter.readCount(SCREENSHOT_COUNTER)),
											fileNameEnding, 
											extension);

		if (LOG.isTraceEnabled()) LOG.trace(Thread.currentThread().getName() + " : fullyQualifiedImageName = " + fullyQualifiedImageName);
		
		// increment counter ready for next image
		StaticCounter.incrementCount(SCREENSHOT_COUNTER);
		
		return fullyQualifiedImageName;
	}
	
	
	/**
	 * Save the byte[] to the specified file name, creating the parent directory if  missing (ie initial directory creation)
	 * 
	 * @param screenshotLogFilename filename to use for the screenshot
	 * @param screenshotLogFileData the screenshot data 
	 */
	public static void writeScreenshotLog(File screenshotLogFilename, byte[] screenshotLogFileData) {

		//noinspection ResultOfMethodCallIgnored
		new File(screenshotLogFilename.getParent()).mkdirs();

		LOG.info(MessageFormat.format("Writing image to disk: {0}", screenshotLogFilename));
		System.out.println("[" + Thread.currentThread().getName() + "]  Writing image to disk:" + screenshotLogFilename);

		if (screenshotLogFileData == null ) {
			screenshotLogFileData = "(null)".getBytes();
		}
		
		try (OutputStream stream = new FileOutputStream(screenshotLogFilename)) {
			stream.write(screenshotLogFileData);

		} catch (IOException e) {
			LOG.error("Caught " + e.getClass().getName() + " with message: " + e.getMessage());
		}
	}
	
	
	/**
	 * @return path of screenshotDirectory
	 */
	public static String getScreenshotDirectory() {
		if (screenshotDirectory == null)
			return null;
		return screenshotDirectory.toString();
	}

			
	/**
	 * @return an existing or otherwise new ScreenshotLoggingHelper
	 * @throws IOException when trying to read the properties file
	 */
	public static synchronized ScreenshotLoggingHelper initialiseDirectory() throws IOException {
		if (instance == null) {
			instance = new ScreenshotLoggingHelper();
		}
		return instance;
	}
	
	
	/**
	 * Deprecated.  Please use {@link #initialiseDirectory()} <br>
	 * Left for compatibility with mark59 v4.1 and earlier
	 * 
	 * @param pr PropertiesReader
	 * @return an existing or otherwise new ScreenshotLoggingHelper
	 * @throws IOException when trying to read the properties file
	 */
	@Deprecated
	public static synchronized ScreenshotLoggingHelper initialiseDirectory(PropertiesReader pr) throws IOException {
		if(instance == null) {
			instance = new ScreenshotLoggingHelper();
		}
		return instance;
	}
	
}
