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

import static org.junit.Assert.*;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive test suite for StaticCounter utility class.
 * Tests thread safety, atomic operations, concurrent access, and edge cases.
 *
 * @author GitHub Copilot
 * Written: October 2025
 */
public class StaticCounterTest {

    private static final String TEST_COUNTER = "testCounter";
    private static final String TEST_COUNTER_2 = "testCounter2";

    @Before
    public void setUp() {
        // Clear all counters before each test
        StaticCounter.clearAll();
    }

    @After
    public void tearDown() {
        // Clean up after each test
        StaticCounter.clearAll();
    }

    @Test
    public void testReadCountNewCounter() {
        // New counter should return 0
        assertEquals("New counter should return 0", 0L, StaticCounter.readCount(TEST_COUNTER));
    }

    @Test
    public void testGetNextIncrementsCounter() {
        // First call should return 1
        assertEquals("First getNext should return 1", 1L, StaticCounter.getNext(TEST_COUNTER));

        // Second call should return 2
        assertEquals("Second getNext should return 2", 2L, StaticCounter.getNext(TEST_COUNTER));

        // ReadCount should show current value (2)
        assertEquals("ReadCount should show current value", 2L, StaticCounter.readCount(TEST_COUNTER));
    }

    @Test
    public void testIncrementCount() {
        // Initial read should be 0
        assertEquals("Initial value should be 0", 0L, StaticCounter.readCount(TEST_COUNTER));

        // Increment once
        StaticCounter.incrementCount(TEST_COUNTER);
        assertEquals("After incrementCount, value should be 1", 1L, StaticCounter.readCount(TEST_COUNTER));

        // Increment again
        StaticCounter.incrementCount(TEST_COUNTER);
        assertEquals("After second incrementCount, value should be 2", 2L, StaticCounter.readCount(TEST_COUNTER));
    }

    @Test
    public void testMultipleCounters() {
        // Test that different counters are independent
        assertEquals("Counter 1 initial", 1L, StaticCounter.getNext(TEST_COUNTER));
        assertEquals("Counter 2 initial", 1L, StaticCounter.getNext(TEST_COUNTER_2));

        assertEquals("Counter 1 second", 2L, StaticCounter.getNext(TEST_COUNTER));
        assertEquals("Counter 2 second", 2L, StaticCounter.getNext(TEST_COUNTER_2));

        // Verify they're still independent
        assertEquals("Counter 1 read", 2L, StaticCounter.readCount(TEST_COUNTER));
        assertEquals("Counter 2 read", 2L, StaticCounter.readCount(TEST_COUNTER_2));
    }

    @Test
    public void testResetCounter() {
        // Set up counter with some value
        StaticCounter.getNext(TEST_COUNTER);
        StaticCounter.getNext(TEST_COUNTER);
        assertEquals("Before reset", 2L, StaticCounter.readCount(TEST_COUNTER));

        // Reset counter
        StaticCounter.resetCounter(TEST_COUNTER);
        assertEquals("After reset", 0L, StaticCounter.readCount(TEST_COUNTER));

        // Next increment should start from 1 again
        assertEquals("After reset, getNext should return 1", 1L, StaticCounter.getNext(TEST_COUNTER));
    }

    @Test
    public void testRemoveCounter() {
        // Create counter
        StaticCounter.getNext(TEST_COUNTER);
        assertTrue("Counter should exist", StaticCounter.getCounterNames().contains(TEST_COUNTER));

        // Remove counter
        assertTrue("Remove should return true for existing counter", StaticCounter.removeCounter(TEST_COUNTER));
        assertFalse("Counter should no longer exist", StaticCounter.getCounterNames().contains(TEST_COUNTER));

        // Removing non-existent counter should return false
        assertFalse("Remove should return false for non-existent counter", StaticCounter.removeCounter("nonExistent"));
    }

    @Test
    public void testGetCounterNames() {
        // Initially should be empty
        assertTrue("Initially should be empty", StaticCounter.getCounterNames().isEmpty());

        // Add some counters
        StaticCounter.getNext(TEST_COUNTER);
        StaticCounter.getNext(TEST_COUNTER_2);

        Set<String> names = StaticCounter.getCounterNames();
        assertEquals("Should have 2 counters", 2, names.size());
        assertTrue("Should contain test counter 1", names.contains(TEST_COUNTER));
        assertTrue("Should contain test counter 2", names.contains(TEST_COUNTER_2));

        // Returned set should be unmodifiable
        try {
            names.add("shouldFail");
            fail("Should not be able to modify returned set");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testClearAll() {
        // Create some counters
        StaticCounter.getNext(TEST_COUNTER);
        StaticCounter.getNext(TEST_COUNTER_2);
        assertEquals("Should have 2 counters", 2, StaticCounter.getCounterMapSize());

        // Clear all
        StaticCounter.clearAll();
        assertEquals("Should have 0 counters after clear", 0, StaticCounter.getCounterMapSize());
        assertTrue("Counter names should be empty", StaticCounter.getCounterNames().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullCounterReadCount() {
        StaticCounter.readCount(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullCounterGetNext() {
        StaticCounter.getNext(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullCounterIncrementCount() {
        StaticCounter.incrementCount(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullCounterResetCounter() {
        StaticCounter.resetCounter(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullCounterRemoveCounter() {
        StaticCounter.removeCounter(null);
    }

    @Test
    public void testConcurrentGetNext() throws InterruptedException {
        final int threadCount = 20;
        final int incrementsPerThread = 100;
        final int expectedTotal = threadCount * incrementsPerThread;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(threadCount);

        // Collect all returned values to verify uniqueness
        Set<Long> returnedValues = ConcurrentHashMap.newKeySet();
        AtomicBoolean hasError = new AtomicBoolean(false);

        // Start all threads
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready

                    for (int j = 0; j < incrementsPerThread; j++) {
                        long value = StaticCounter.getNext(TEST_COUNTER);

                        // Check if value is unique
                        if (!returnedValues.add(value)) {
                            hasError.set(true);
                            fail("Duplicate value returned: " + value);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    hasError.set(true);
                } finally {
                    completeLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        startLatch.countDown();

        // Wait for completion
        assertTrue("All threads should complete within 10 seconds",
                  completeLatch.await(10, TimeUnit.SECONDS));

        executor.shutdown();

        assertFalse("No errors should occur during concurrent access", hasError.get());
        assertEquals("Should have unique values", expectedTotal, returnedValues.size());
        assertEquals("Final counter value should match total increments",
                    expectedTotal, StaticCounter.readCount(TEST_COUNTER));

        // Verify all values are in expected range
        for (Long value : returnedValues) {
            assertTrue("Value should be positive", value > 0);
            assertTrue("Value should not exceed total", value <= expectedTotal);
        }
    }

    @Test
    public void testConcurrentMultipleCounters() throws InterruptedException {
        final int threadCount = 10;
        final int countersPerThread = 5;
        final int incrementsPerCounter = 20;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch completeLatch = new CountDownLatch(threadCount);

        AtomicBoolean hasError = new AtomicBoolean(false);

        // Each thread works with different counters
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int counter = 0; counter < countersPerThread; counter++) {
                        String counterName = "thread" + threadId + "_counter" + counter;

                        for (int increment = 0; increment < incrementsPerCounter; increment++) {
                            StaticCounter.getNext(counterName);
                        }

                        // Verify final value
                        long finalValue = StaticCounter.readCount(counterName);
                        if (finalValue != incrementsPerCounter) {
                            hasError.set(true);
                            fail("Counter " + counterName + " has incorrect final value: " + finalValue);
                        }
                    }
                } catch (Exception e) {
                    hasError.set(true);
                    e.printStackTrace();
                } finally {
                    completeLatch.countDown();
                }
            });
        }

        assertTrue("All threads should complete within 10 seconds",
                  completeLatch.await(10, TimeUnit.SECONDS));

        executor.shutdown();

        assertFalse("No errors should occur", hasError.get());

        // Verify total number of counters
        int expectedCounters = threadCount * countersPerThread;
        assertEquals("Should have expected number of counters",
                    expectedCounters, StaticCounter.getCounterNames().size());
    }

    @Test
    public void testPerformanceBaseline() {
        final int iterations = 100000;

        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            StaticCounter.getNext(TEST_COUNTER);
        }

        long duration = System.nanoTime() - startTime;
        double operationsPerSecond = (iterations * 1_000_000_000.0) / duration;

        System.out.println("Performance: " + String.format("%.0f", operationsPerSecond) + " operations/second");

        // Verify correctness
        assertEquals("Final counter should match iterations", iterations, StaticCounter.readCount(TEST_COUNTER));

        // Performance assertion (should be > 1M ops/sec on modern hardware)
        assertTrue("Should achieve at least 100,000 operations per second", operationsPerSecond > 100_000);
    }

    @Test
    public void testEdgeCasesEmptyStrings() {
        // Empty string should be valid counter name
        assertEquals("Empty string counter should work", 1L, StaticCounter.getNext(""));
        assertEquals("Empty string counter read", 1L, StaticCounter.readCount(""));
    }

    @Test
    public void testResetNonExistentCounter() {
        // Resetting non-existent counter should not throw exception
        StaticCounter.resetCounter("nonExistent");
        // Should still return 0 when read
        assertEquals("Non-existent counter should return 0", 0L, StaticCounter.readCount("nonExistent"));
    }
}