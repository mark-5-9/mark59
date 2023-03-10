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

package com.mark59.core.utils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Defines the Mark59 Logging Directory and log names formatter.
 * A Singleton pattern is used so initialization and the log names formatter are only executed oncee
 * 
 * @author Michael Cohen
 * @author Philip Webb Written: Australian Winter 2019
 */
public class Mark59LoggingConfig {
	private static final Logger LOG = LogManager.getLogger(Mark59LoggingConfig.class);

	private static final String DEFAULT_LOGNAMES_FORMAT = Mark59Constants.THREAD_NAME;

	private final File logDirectory;
	private String logNamesFormat = DEFAULT_LOGNAMES_FORMAT;

	private static final Mark59LoggingConfig instance;

	static {
		try {
			instance = new Mark59LoggingConfig();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error creating a Mark59LoggingConfig Singleton");
		}
	}

	
	/**
	 * Setup up and clear log directory and configure log names formatter.  
	 * A private constructor for singleton pattern.
	 */
	private Mark59LoggingConfig() throws IOException {
		logDirectory = initialiseLogDirectory();
		configureLogNamesFormatter();
	}

	
	/**
	 * @return Mark59LoggingConfig singleton instance
	 */
	public static Mark59LoggingConfig getInstance() {
		return instance;
	}

	
	@SuppressWarnings("deprecation")
	private File initialiseLogDirectory() throws IOException {
		File logDirectory = null;
		String logDirectoryPathname;
		try {
			logDirectoryPathname = PropertiesReader.getInstance().getProperty(PropertiesKeys.MARK59_PROP_LOG_DIRECTORY);
			
			if (StringUtils.isBlank(logDirectoryPathname)){
				
				logDirectoryPathname = PropertiesReader.getInstance().getProperty(PropertiesKeys.MARK59_PROP_SCREENSHOT_DIRECTORY);
				if (StringUtils.isNotBlank(logDirectoryPathname)){
					LOG.warn(PropertiesKeys.MARK59_PROP_SCREENSHOT_DIRECTORY + " is deprecated and will be removed in a future release."
							+ " Please use "	+ PropertiesKeys.MARK59_PROP_LOG_DIRECTORY + " instead.");
					System.out.println(PropertiesKeys.MARK59_PROP_SCREENSHOT_DIRECTORY + " is deprecated and will be removed in a future release."
							+ " Please use "	+ PropertiesKeys.MARK59_PROP_LOG_DIRECTORY + " instead.");					
				}
			} 
			
		} catch (IOException e) {
			LOG.warn("Failed to read Mark59.properties while trying to obtain screenshot directory from config "
					+ "(property " + PropertiesKeys.MARK59_PROP_LOG_DIRECTORY + " not set");
			return null;
		}

		if (StringUtils.isNotBlank(logDirectoryPathname)) {

			String directorySuffixFormat = PropertiesReader.getInstance()
					.getProperty(PropertiesKeys.MARK59_PROP_LOG_DIRECTORY_SUFFIX);

			if (StringUtils.isBlank(directorySuffixFormat)) {
				LOG.info("Property " + PropertiesKeys.MARK59_PROP_LOG_DIRECTORY_SUFFIX + " not set. '"
						+ Mark59Constants.DATE + "' will be assumed.");
				logDirectoryPathname += File.separator + LocalDate.now();
			} else if (Mark59Constants.DATE_TIME.equalsIgnoreCase(directorySuffixFormat.trim())) {
				logDirectoryPathname += File.separator	+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss"));
			} else if (Mark59Constants.DATE.equalsIgnoreCase(directorySuffixFormat.trim())) {
				logDirectoryPathname += File.separator + LocalDate.now();
			} else {
				LOG.warn("Property " + PropertiesKeys.MARK59_PROP_LOG_DIRECTORY_SUFFIX
						+ " shoud be set as either " + Mark59Constants.DATE + " or " + Mark59Constants.DATE_TIME
						+ " (was " + directorySuffixFormat + "). '" + Mark59Constants.DATE + "' will be assumed.");
				logDirectoryPathname += File.separator + LocalDate.now();
			}

			logDirectory = new File(logDirectoryPathname);
			LOG.info("Clearing any existing data from Mark59 log directory " + logDirectory.getPath());
			FileUtils.deleteDirectory(logDirectory);
		} else {
			LOG.warn("As no Mark59 log directory has been set, attempts to write logs will fail.");
		}
		return logDirectory;
	}

	
	private void configureLogNamesFormatter() {
		String mark59PropLognameFormat;
		try {
			mark59PropLognameFormat = PropertiesReader.getInstance().getProperty(PropertiesKeys.MARK59_PROP_LOGNAME_FORMAT);
		} catch (IOException e) {
			LOG.warn("Failed to read Mark59.properties while trying to obtain log name formatter from config "
					+ "(property " + PropertiesKeys.MARK59_PROP_LOGNAME_FORMAT + " was not set). '"
					+ logNamesFormat + "' assumed.");
			return;
		}

		List<String> mark59PropLognameFormatList = Mark59Utils.commaDelimStringToStringList(mark59PropLognameFormat);

		// set logname format to default if:  property is empty or not set, or an invalid formatter has been passed

		if (mark59PropLognameFormatList.size() == 0) {
			LOG.info("As property " + PropertiesKeys.MARK59_PROP_LOGNAME_FORMAT + " was not set, '"
					+ logNamesFormat + "' is assumed.");
			return;
		}

		String validatedLognameFormatOptions = "|";
		for (String lognameFormatOption : mark59PropLognameFormatList) {
			if (Mark59Constants.LOGNAME_FORMAT_OPTIONS.contains(lognameFormatOption.trim().toUpperCase(Locale.ROOT))) {
				validatedLognameFormatOptions += lognameFormatOption.trim().toUpperCase(Locale.ROOT) + "|";
			} else {
				LOG.warn("option '" + lognameFormatOption + " is invalid for property "
						+ PropertiesKeys.MARK59_PROP_LOGNAME_FORMAT + ".  '" + logNamesFormat
						+ "' will be used for the value of " + PropertiesKeys.MARK59_PROP_LOGNAME_FORMAT);
				return;
			}
		}
		logNamesFormat = validatedLognameFormatOptions;
	}

	
	/**
	 * @return logDirectory  the name of the directory being used to hold Mark59 'screenshot' logs in this run.
	 */
	public File getLogDirectory() {
		return logDirectory;
	}

	
	/**
	 * @return logNamesFormat a comma delimited string indicating the formatting to be used for 'screenshot' logs in this run.
	 */
	public String getLogNamesFormat() {
		return logNamesFormat;
	}

}
