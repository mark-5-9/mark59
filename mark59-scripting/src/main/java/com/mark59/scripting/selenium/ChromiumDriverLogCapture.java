package com.mark59.scripting.selenium;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


/**
 * Captures and outputs LOG messages from org.openqa.selenium.chromium.ChromiumDriver.
 * <p>Messages are interrogated to prevent the same message being written multiple times 
 * during a test.
 * 
 * <p>Note the LOG objects here are from the java.util.logging api (not log4j)  
 * 
 * @author Philip Webb
 * Written: Australian Summer 2025/26
 */
public class ChromiumDriverLogCapture {

	private static final Logger LOG = Logger.getLogger(ChromiumDriverLogCapture.class.getName());
	
    private static final String CHROMIUM_DRIVER_LOGGER_NAME = "org.openqa.selenium.chromium.ChromiumDriver";
    private final Set<String> capturedLogs = new HashSet<>();
    private final Logger chromiumLogger;
    private final ImmediateOutputHandler handler;

    
	private static ChromiumDriverLogCapture chromiumDriverLogCaptureInstance;

	/**
	 * Creates a new log capture instance and immediately starts capturing
	 * ChromiumDriver logs at ALL levels.
	 */
	private ChromiumDriverLogCapture(){
		// Get reference to the ChromiumDriver logger
        chromiumLogger = Logger.getLogger(CHROMIUM_DRIVER_LOGGER_NAME);

        // Prevent propagation to parent handlers (suppresses default console output)
        chromiumLogger.setUseParentHandlers(false);

        // Create and add custom handler that outputs immediately
        handler = new ImmediateOutputHandler();
        chromiumLogger.addHandler(handler);

        // Set the level to capture all logs
        chromiumLogger.setLevel(Level.ALL);
        handler.setLevel(Level.ALL);
    }

    /**
     * Custom log handler that captures all level log messages from the referenced logger.
     * <p>Messages are interrogated to prevent the same message being written multiple times 
     * during a test.
     */
    private class ImmediateOutputHandler extends Handler {

		@Override
		public void publish(LogRecord record) {
			if (isLoggable(record)) {

				if (!capturedLogs.contains(record.getMessage())) {

					String logMessage = String.format("[dups ignored] %s: %s",
							record.getSourceClassName(), record.getMessage());
					// System.out.println("**sysout capture** ChromiumDriver: " + logMessage);

					LOG.log(record.getLevel(), logMessage, record.getParameters());
					
					synchronized (capturedLogs) {
						capturedLogs.add(record.getMessage());
					}

					// If there's an exception, output it too
					if (record.getThrown() != null) {
						System.out.println("  Exception: " + record.getThrown().getClass().getName() 
								+ ": " + record.getThrown().getMessage());
						record.getThrown().printStackTrace(System.out);
					}
				}
			}
		}

		
		@Override
        public void flush() {
            System.out.flush();
        }

        @Override
        public void close() throws SecurityException {
            flush();
        }
    }

    /**
     * returns a set of all stored log messages 
     * @return Set of all captured log messages
     */
    public Set<String> getCapturedLogs() {
        synchronized (capturedLogs) {
            return new HashSet<>(capturedLogs);
        }
    }

    /**
     * Clear all captured logs.
     * Thread-safe.
     */
    public void clearLogs() {
        synchronized (capturedLogs) {
            capturedLogs.clear();
        }
    }

    /**
     * Get the count of captured log messages.
     * Thread-safe.
     *
     * @return Number of captured log messages
     */
    public int getLogCount() {
        synchronized (capturedLogs) {
            return capturedLogs.size();
        }
    }

    /**
     * Remove the custom handler and stop capturing logs.
     * Should be called when log capture is no longer needed.
     */
    public void stopCapturing() {
        chromiumLogger.removeHandler(handler);
        handler.close();
    }

    /**
     * Set the log level for capturing.
     *
     * @param level The minimum log level to capture (e.g., Level.WARNING, Level.ALL)
     */
    public void setLogLevel(Level level) {
        chromiumLogger.setLevel(level);
        handler.setLevel(level);
    }
    

	/**
	 * Singleton pattern
	 * return existing or otherwise a new instance of ChromiumDriverLogCapture
	 * @return ChromiumDriverLogCapture instance
	 */
	public static synchronized ChromiumDriverLogCapture getInstance() {
		if (chromiumDriverLogCaptureInstance == null) {
			chromiumDriverLogCaptureInstance = new ChromiumDriverLogCapture();
		}
		return chromiumDriverLogCaptureInstance;
	}
    
    
}
