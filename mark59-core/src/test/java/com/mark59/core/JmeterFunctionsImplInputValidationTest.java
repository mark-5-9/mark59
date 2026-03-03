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

package com.mark59.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mark59.core.utils.Mark59LoggingConfig;

/**
 * Test suite for JmeterFunctionsImpl input validation improvements in log naming methods.
 * Tests the sanitization and validation of inputs to formLeadingPartOfLogNames and buildFullyQualifiedLogName.
 *
 * @author GitHub Copilot
 * Written: October 2025
 */
public class JmeterFunctionsImplInputValidationTest {

    private JmeterFunctionsImpl jmeterFunctions;
    private JavaSamplerContext mockContext;
    private JMeterContext mockJMeterContext;
    private AbstractThreadGroup mockThreadGroup;
    private Sampler mockSampler;
    private Mark59LoggingConfig mockLoggingConfig;

    @Before
    public void setUp() {
        mockContext = mock(JavaSamplerContext.class);
        mockJMeterContext = mock(JMeterContext.class);
        mockThreadGroup = mock(AbstractThreadGroup.class);
        mockSampler = mock(Sampler.class);
        mockLoggingConfig = mock(Mark59LoggingConfig.class);

        // Set up mock relationships
        when(mockContext.getJMeterContext()).thenReturn(mockJMeterContext);
        when(mockJMeterContext.getThreadGroup()).thenReturn(mockThreadGroup);
        when(mockJMeterContext.getCurrentSampler()).thenReturn(mockSampler);

        // Set up mock logging config to return non-null values
        when(mockLoggingConfig.getLogDirectoryPathName()).thenReturn("mark59-logs-pathname");
        when(mockLoggingConfig.getLogNamesFormat()).thenReturn("ThreadName");

        // Create JmeterFunctionsImpl with mark59 properties enabled
        jmeterFunctions = new JmeterFunctionsImpl(mockContext, true);

        // Inject the mock loggingConfig using reflection
        try {
            Field loggingConfigField = JmeterFunctionsImpl.class.getDeclaredField("loggingConfig");
            loggingConfigField.setAccessible(true);
            loggingConfigField.set(jmeterFunctions, mockLoggingConfig);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock logging config", e);
        }
    }

    @After
    public void tearDown() {
        if (jmeterFunctions != null) {
            jmeterFunctions.tearDown();
        }
    }

    /**
     * Helper method to inject mock logging config into a JmeterFunctionsImpl instance
     */
    private void injectMockLoggingConfig(JmeterFunctionsImpl instance) {
        try {
            Field loggingConfigField = JmeterFunctionsImpl.class.getDeclaredField("loggingConfig");
            loggingConfigField.setAccessible(true);
            loggingConfigField.set(instance, mockLoggingConfig);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock logging config", e);
        }
    }

    @Test
    public void testReserveFullyQualifiedLogNameWithValidInputs() {
        // Test normal valid inputs
        String logName = "test_image";
        String suffix = "png";

        String result = jmeterFunctions.reserveFullyQualifiedLogName(logName, suffix);

        assertNotNull("Result should not be null - if it is the mock setup is incorrect!", result);
        assertTrue("Result should contain log name", result.contains(logName));
        assertTrue("Result should contain suffix", result.endsWith("." + suffix));
    }

    @Test
    public void testReserveFullyQualifiedLogNameWithInvalidCharacters() {
        // Test with characters that are invalid in filenames
        String invalidLogName = "test<>:\"|?*image";
        String validSuffix = "png";

        String result = jmeterFunctions.reserveFullyQualifiedLogName(invalidLogName, validSuffix);

        assertNotNull("Result should not be null", result);
        // Invalid characters should be replaced with underscores
        assertFalse("Result should not contain invalid characters",
                   result.matches(".*[<>:\"|?*].*"));
        assertTrue("Result should contain sanitized name",
                  result.contains("test_______image"));
    }

    @Test
    public void testReserveFullyQualifiedLogNameWithNullInputs() {
        // Test with null inputs - should use defaults
        String result = jmeterFunctions.reserveFullyQualifiedLogName(null, null);

        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain default image name", result.contains("defaultImage"));
        assertTrue("Result should contain default suffix", result.endsWith(".txt"));
    }

    @Test
    public void testReserveFullyQualifiedLogNameWithEmptyInputs() {
        // Test with empty inputs - should use defaults
        String result = jmeterFunctions.reserveFullyQualifiedLogName("", "");

        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain default image name", result.contains("defaultImage"));
        assertTrue("Result should contain default suffix", result.endsWith(".txt"));
    }

    @Test
    public void testReserveFullyQualifiedLogNameWithWhitespaceInputs() {
        // Test with whitespace-only inputs - should use defaults
        String result = jmeterFunctions.reserveFullyQualifiedLogName("   ", "   ");

        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain default image name", result.contains("defaultImage"));
        assertTrue("Result should contain default suffix", result.endsWith(".txt"));
    }

    @Test
    public void testReserveFullyQualifiedLogNameWithPathTraversalAttempts() {
        // Test with path traversal attempts - should be sanitized
        String maliciousLogName = "../../../etc/passwd";
        String maliciousSuffix = "../conf";

        String result = jmeterFunctions.reserveFullyQualifiedLogName(maliciousLogName, maliciousSuffix);



        assertNotNull("Result should not be null", result);
        // Path separators should be replaced with underscores
        assertFalse("Result should not contain path separators",
                   result.contains("/") || result.contains("\\"));
        assertTrue("Result should sanitize path traversal",
                  result.contains("______etc_passwd"));
    }

    @Test
    public void testReserveFullyQualifiedLogNameWithVeryLongInputs() {
        // Test with very long inputs that exceed filesystem limits
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            longName.append("verylongname");
        }

        String result = jmeterFunctions.reserveFullyQualifiedLogName(longName.toString(), "txt");

        assertNotNull("Result should not be null", result);
        // The sanitized portion should not exceed reasonable length limits
        // Note: full path might be longer due to directory and counter components
        assertTrue("Result should be reasonable length", result.length() < 1000);
    }

    @Test
    public void testReserveFullyQualifiedLogNameWithSpecialCharactersInSuffix() {
        // Test with invalid characters in suffix
        String validName = "test_image";
        String invalidSuffix = "p<n>g|exe";

        String result = jmeterFunctions.reserveFullyQualifiedLogName(validName, invalidSuffix);

        assertNotNull("Result should not be null", result);
        // Invalid characters in suffix should be removed
        assertFalse("Result should not contain invalid suffix characters",
                   result.matches(".*\\.[<>|]*"));
        assertTrue("Result should contain valid name", result.contains(validName));
    }

    @Test
    public void testThreadGroupNameSanitization() {
        // Test that thread group names from JMeter context are properly sanitized
        String maliciousThreadGroupName = "ThreadGroup<script>alert('xss')</script>";
        when(mockThreadGroup.getName()).thenReturn(maliciousThreadGroupName);

        // Create a new instance to trigger the log name formation
        JmeterFunctionsImpl testInstance = new JmeterFunctionsImpl(mockContext, true);

        // Inject mock logging config into test instance
        injectMockLoggingConfig(testInstance);

        String result = testInstance.reserveFullyQualifiedLogName("test", "txt");

        assertNotNull("Result should not be null", result);
        // Malicious characters should be sanitized
        assertFalse("Result should not contain malicious characters",
                   result.contains("<script>") || result.contains("</script>"));

        testInstance.tearDown();
    }

    @Test
    public void testSamplerNameSanitization() {
        // Test that sampler names from JMeter context are properly sanitized
        String maliciousSamplerName = "Sampler|rm -rf /|dangerous";
        when(mockSampler.getName()).thenReturn(maliciousSamplerName);

        // Create a new instance to trigger the log name formation
        JmeterFunctionsImpl testInstance = new JmeterFunctionsImpl(mockContext, true);

        // Inject mock logging config into test instance
        injectMockLoggingConfig(testInstance);

        String result = testInstance.reserveFullyQualifiedLogName("test", "txt");

        assertNotNull("Result should not be null", result);
        // Malicious characters should be sanitized
        assertFalse("Result should not contain pipe characters", result.contains("|"));
        // The result should contain the test name we passed in
        assertTrue("Result should contain test name", result.contains("test"));

        testInstance.tearDown();
    }

    @Test
    public void testLogNameGenerationWithNullContext() {
        // Test behavior when JMeter context is null
        when(mockContext.getJMeterContext()).thenReturn(null);

        JmeterFunctionsImpl testInstance = new JmeterFunctionsImpl(mockContext, true);

        // Inject mock logging config into test instance
        injectMockLoggingConfig(testInstance);

        String result = testInstance.reserveFullyQualifiedLogName("test", "txt");

        assertNotNull("Result should not be null even with null context", result);
        assertTrue("Result should contain test name", result.contains("test"));

        testInstance.tearDown();
    }

    @Test
    public void testLogNameGenerationWithNullThreadGroup() {
        // Test behavior when thread group is null
        when(mockJMeterContext.getThreadGroup()).thenReturn(null);

        JmeterFunctionsImpl testInstance = new JmeterFunctionsImpl(mockContext, true);

        // Inject mock logging config into test instance
        injectMockLoggingConfig(testInstance);

        String result = testInstance.reserveFullyQualifiedLogName("test", "txt");

        assertNotNull("Result should not be null even with null thread group", result);
        assertTrue("Result should contain test name", result.contains("test"));

        testInstance.tearDown();
    }

    @Test
    public void testLogNameGenerationWithNullSampler() {
        // Test behavior when sampler is null
        when(mockJMeterContext.getCurrentSampler()).thenReturn(null);

        JmeterFunctionsImpl testInstance = new JmeterFunctionsImpl(mockContext, true);

        // Inject mock logging config into test instance
        injectMockLoggingConfig(testInstance);

        String result = testInstance.reserveFullyQualifiedLogName("test", "txt");

        assertNotNull("Result should not be null even with null sampler", result);
        assertTrue("Result should contain test name", result.contains("test"));

        testInstance.tearDown();
    }

    @Test
    public void testConsistentLogNaming() {
        // Test that log names are consistent and unique
        String logName = "consistent_test";
        String suffix = "log";

        String result1 = jmeterFunctions.reserveFullyQualifiedLogName(logName, suffix);
        String result2 = jmeterFunctions.reserveFullyQualifiedLogName(logName, suffix);

        assertNotNull("First result should not be null", result1);
        assertNotNull("Second result should not be null", result2);

        // Results should be different due to counter increment
        assertNotEquals("Results should be unique due to counter", result1, result2);

        // Both should contain the log name
        assertTrue("First result should contain log name", result1.contains(logName));
        assertTrue("Second result should contain log name", result2.contains(logName));
    }

    @Test
    public void testInputValidationPreventsSQLInjectionPatterns() {
        // Test that common injection patterns are safely handled
        String injectionAttempt = "test'; DROP TABLE users; --";
        String suffix = "txt";

        String result = jmeterFunctions.reserveFullyQualifiedLogName(injectionAttempt, suffix);



        assertNotNull("Result should not be null", result);
        // SQL injection patterns should be sanitized
        assertFalse("Result should not contain SQL patterns",
                   result.contains("DROP TABLE") || result.contains("--"));
    }
}