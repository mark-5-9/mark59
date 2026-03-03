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
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Defines the Mark59 Logging Directory and log names formatter.
 *
 * These properties (can be set in the mark59.properties file) are relevant to log file names:
 * <br>mark59.log.directory - sets the base directory to be used for mark59 logging functions
 * <br>mark59.log.directory.suffix - add a date or date/time suffix to the log directory
 * <br>mark59.logname.format - set of options to set set the format of a log filename
 *
 * <p>A Singleton pattern is used so initialization and the log names formatter are only executed once
 *
 * @see PropertiesKeys
 * @see PropertiesKeys#MARK59_PROP_LOG_DIRECTORY
 * @see PropertiesKeys#MARK59_PROP_LOG_DIRECTORY_SUFFIX
 * @see PropertiesKeys#MARK59_PROP_LOGNAME_FORMAT
 *
 * @author Michael Cohen
 * @author Philip Webb Written: Australian Winter 2019
 */
public class Mark59LoggingConfig {
	private static final Logger LOG = LogManager.getLogger(Mark59LoggingConfig.class);
	private static final Mark59LoggingConfig instance;

	private static final String DEFAULT_LOGNAMES_FORMAT = Mark59Constants.THREAD_NAME;
	private static final Pattern DATE_PATTERN 		= Pattern.compile("\\d{4}-\\d{2}-\\d{2}"); 		// 2025-10-06
	private static final Pattern DATETIME_PATTERN 	= Pattern.compile("\\d{4}-\\d{2}-\\d{2}-\\d{6}");	// 2025-10-06-143025
	private final File logDirectory;
	private String logDirectoryPathName = null;  // prevents need to create a File in tests

	private String logNamesFormat = DEFAULT_LOGNAMES_FORMAT;

	static {
		try {
			instance = new Mark59LoggingConfig();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error creating a Mark59LoggingConfig Singleton"+e.getMessage());
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
	 * return Mark59LoggingConfig singleton instance
	 * @return a Mark59LoggingConfig instance
	 */
	public static Mark59LoggingConfig getInstance() {
		return instance;
	}


	/**
	 * Note: only invoke <code>FileUtils.deleteDirectory(logDirectory)</code> in the pattern shown here. This means
	 * there will be a basic sanity check that only directories that have a Mark59 compatible date or date-time lowest
	 * level directory can be deleted.
	 *
	 * @return logDirectory - directory for Mark59 all script logging
	 * @throws IOException
	 */
	private File initialiseLogDirectory() throws IOException {
		File logDirectory = null;
		logDirectoryPathName = PropertiesReader.getInstance().getProperty(PropertiesKeys.MARK59_PROP_LOG_DIRECTORY);

		if (StringUtils.isBlank(logDirectoryPathName)){
			logDirectoryPathName = setLogDirUsingDeprecatedProperty();
		}

		if (StringUtils.isNotBlank(logDirectoryPathName)) {
			logDirectoryPathName = logDirectoryPathName + File.separator + lowestLevelLogDirTemporalFormatter();
			clearLogDir(logDirectoryPathName);
			logDirectory = new File(logDirectoryPathName);
			// Ensure directory exists and is writable
    		if (!logDirectory.exists() && !logDirectory.mkdirs()) {
        		LOG.warn("Failure to create a writable log directory! : " + logDirectoryPathName);
    		}
		} else {
			LOG.warn("As no Mark59 log directory has been set, attempts to write logs will fail.");
		}
		return logDirectory;
	}


	@SuppressWarnings("deprecation")
	private String setLogDirUsingDeprecatedProperty() throws IOException {
		String logDirectoryPathnameDeprecatedProp = PropertiesReader.getInstance().getProperty(PropertiesKeys.MARK59_PROP_SCREENSHOT_DIRECTORY);
		if (StringUtils.isNotBlank(logDirectoryPathnameDeprecatedProp)){
			LOG.warn(PropertiesKeys.MARK59_PROP_SCREENSHOT_DIRECTORY + " is deprecated and will be removed in a future release."
					+ " Please use "	+ PropertiesKeys.MARK59_PROP_LOG_DIRECTORY + " instead.");
			System.out.println(PropertiesKeys.MARK59_PROP_SCREENSHOT_DIRECTORY + " is deprecated and will be removed in a future release."
					+ " Please use "	+ PropertiesKeys.MARK59_PROP_LOG_DIRECTORY + " instead.");
		}
		return logDirectoryPathnameDeprecatedProp;
	}


	private String lowestLevelLogDirTemporalFormatter() throws IOException {
		String directoryTemporalName= null;
		String directorySuffixFormat = PropertiesReader.getInstance()
				.getProperty(PropertiesKeys.MARK59_PROP_LOG_DIRECTORY_SUFFIX);

		if (StringUtils.isBlank(directorySuffixFormat)) {
			LOG.info("Property " + PropertiesKeys.MARK59_PROP_LOG_DIRECTORY_SUFFIX + " not set. '"
					+ Mark59Constants.DATE + "' will be assumed.");
			directoryTemporalName = LocalDate.now().toString();
		} else if (Mark59Constants.DATE_TIME.equalsIgnoreCase(directorySuffixFormat.trim())) {
			directoryTemporalName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss"));
		} else if (Mark59Constants.DATE.equalsIgnoreCase(directorySuffixFormat.trim())) {
			directoryTemporalName = LocalDate.now().toString();
		} else {
			LOG.warn("Property " + PropertiesKeys.MARK59_PROP_LOG_DIRECTORY_SUFFIX
					+ " should be set as either " + Mark59Constants.DATE + " or " + Mark59Constants.DATE_TIME
					+ " (was " + directorySuffixFormat + "). '" + Mark59Constants.DATE + "' will be assumed.");
			directoryTemporalName = LocalDate.now().toString();
		}
		LOG.debug("directoryTemporalName="+directoryTemporalName);

		return directoryTemporalName;
	}


	private void configureLogNamesFormatter() {
		String mark59PropLognameFormat;
		try {
			mark59PropLognameFormat = PropertiesReader.getInstance().getProperty(PropertiesKeys.MARK59_PROP_LOGNAME_FORMAT);
		} catch (IOException e) {
			LOG.info("Failed to read Mark59.properties while trying to obtain log name formatter from config "
					+ "(property " + PropertiesKeys.MARK59_PROP_LOGNAME_FORMAT + " was not set). '"
					+ logNamesFormat + "' assumed.");
			return;
		}

		List<String> mark59PropLognameFormatList = Mark59Utils.commaDelimStringToStringList(mark59PropLognameFormat);

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
	 * Clears the specified log directory by deleting all its contents and subdirectories.
	 * A sanity check is performed to ensure the directory exists and matches the expected Mark59 log directory
	 * format before attempting deletion. (belt and braces approach)
	 *
	 * @param logDirectoryPathName the path to the log directory to be cleared
	 * @throws IOException if an I/O error occurs during directory deletion
	 */
	private void clearLogDir(String logDirectoryPathName) throws IOException {
		String lowestLevelName = Paths.get(logDirectoryPathName).getFileName().toString();

		if (DATE_PATTERN.matcher(lowestLevelName).matches() ||
			DATETIME_PATTERN.matcher(lowestLevelName).matches()) {
			LOG.info("Clearing any existing data from Mark59 log directory " + logDirectoryPathName);
			FileUtils.deleteDirectory(new File(logDirectoryPathName));
		} else {
			LOG.warn("Log directory '" + logDirectoryPathName + "' does not appear to be a Mark59 log directory. "
					+ "It should have a lowest level directory name of either 'yyyy-MM-dd' or 'yyyy-MM-dd-HHmmss'. "
					+ "No deletion of directory contents will be attempted.");
		}
	}


	/**
	 * return logDirectory - the name of the directory being used to hold Mark59 logs in this run.
	 * @return File.
	 */
	public File getLogDirectory() {
		return logDirectory;
	}


	/**
	 * return logDirectoryPathName - the pathname of the directory being used to hold Mark59 logs in this run
	 * @return string.
	 */
	public String getLogDirectoryPathName () {
		return logDirectoryPathName;
	}
	/**
	 * return logNamesFormat - a comma delimited string indicating the formatting to be used for logs in this run.
	 * @return string.
	 */
	public String getLogNamesFormat() {
		return logNamesFormat;
	}

}
