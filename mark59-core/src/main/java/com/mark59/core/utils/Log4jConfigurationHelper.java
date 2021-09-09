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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;


/**
 * This class is designed to allow log4j messages to be displayed on the consoles at the requested level when testing a script 
 * (in an IDE for example), without the need for any other log4j configuration.<p>
 * It basically replaces the XML configuration described below.  Sample Usage:<p>
 * 
 * <code>Log4jConfigurationHelper.init(Level.DEBUG);</code><p>
 * <code>Log4jConfigurationHelper.init(Level.DEBUG, "%d [%t] %-5level: %msg%n%throwable");</code>  - set log level and pattern layout<p>
 * <code>Log4jConfigurationHelper.init()</code> - the default when no level is specified is INFO<p>
 * 
 * <b>XML Configuration alternative to using this helper class :</b> <p> 
 * 
 * You need to create a log4j2.xml file, configured to use Console target "SYSTEM_OUT". For example : <p>
 * 
 * <code> &lt;Configuration status="INFO"&gt;<br>&lt;Appenders&gt;<br>&nbsp;&lt;Console name="Console" target="SYSTEM_OUT"&gt;
 * <br>&nbsp;&nbsp;&lt;PatternLayout pattern="%{dHH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n" /&gt;
 * <br>&nbsp;&lt;/Console&gt;<br>&lt;/Appenders&gt;<br>&lt;Loggers&gt;<br>&nbsp;&lt;Root level="info"&gt;<br>&nbsp;&nbsp;&lt;AppenderRef ref="Console" /&gt;
 * <br>&nbsp;&lt;Root&gt;<br>&lt;/Loggers&gt;<br>&lt;/Configuration&gt;</code><p>
 * 
 * Also, you need to set a jvm argument, so that log4j is aware of the configuration file.  So in Eclipse for instance, 
 * assuming the log4j config file is in the root of the project,set via  Run Configurations -  Arguments (VM Arguments) tab: <p> 
 *<code>-Dlog4j.configurationFile=./log4j2.xml</code><p>
 *
 * When <b>BOTH</b> a Log4j XML configuration has been set and an <i>init</i> method in this class is called, the log4j configuration for this class will take precedence.  
 *
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class Log4jConfigurationHelper {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(Log4jConfigurationHelper.class);	
	
	/**
	 * Set log4j logging level and pattern layout
	 * @param level  see org.apache.logging.log4j.Level
	 * @param patternLayout  log4j message pattern layout
	 */
	public static void init(Level level, String patternLayout ) {
			
		ConfigurationBuilder<BuiltConfiguration> builder  = ConfigurationBuilderFactory.newConfigurationBuilder();
		AppenderComponentBuilder console   = builder.newAppender("Stdout", "CONSOLE").
				addAttribute("target",  ConsoleAppender.Target.SYSTEM_OUT);
		LayoutComponentBuilder standard  = builder.newLayout("PatternLayout");
		standard.addAttribute("pattern", patternLayout);
		console.add(standard);
		builder.add(console);

		// FYI how to add more loggers 
		// import org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder;
//		LoggerComponentBuilder logger = builder.newLogger("org", Level.DEBUG);
//		logger.add(builder.newAppenderRef("Stdout"));
//		logger.addAttribute("additivity", true);
//		builder.add(logger);

		RootLoggerComponentBuilder rootLogger = builder.newRootLogger(level);
		rootLogger.add(builder.newAppenderRef("Stdout"));
		builder.add(rootLogger);
		
		Configuration customConfiguration = builder.build();
		customConfiguration.start();
		Configurator.initialize(customConfiguration);
		
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		if (ctx == null ) { //just in case..
			ctx = new LoggerContext(patternLayout); 
		}
		ctx.updateLoggers(customConfiguration);
		ctx.start(customConfiguration);
		
		System.out.println("Log4j 2 configuration has been set vai the Mark59 Log4jConfigurationHelper class.");
		
		String log4jConfigurationFile = System.getProperty("log4j.configurationFile");
		if (log4jConfigurationFile != null ) {
			System.out.println("  - this means the Xml configuration set via the JVM parameter 'log4j.configurationFile' has been overwritten");		
		}

	}	

	
	/**
	 * Default logging level is INFO 
	 */
	public static void init() {
		init(Level.INFO);
	}

	
	/**
	 * Set log4j logging level.   Layout pattern defaults to "%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"
	 * @param level  see org.apache.logging.log4j.Level	 
	 */
	public static void init(Level level) {
		init(level, "%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n");
	}
	
	
	
}
