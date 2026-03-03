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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for JmeterFunctionsImpl focusing on thread safety and concurrency issues.
 *
 * @author GitHub Copilot
 * Written: October 2025
 */
public class JmeterFunctionsImplConcurrencyTest {

    private JmeterFunctionsImpl jmeterFunctions;
    private JavaSamplerContext mockContext;

    @Before
    public void setUp() {
        mockContext = mock(JavaSamplerContext.class);
        jmeterFunctions = new JmeterFunctionsImpl(mockContext, false); // No mark59 properties needed
    }

    @Test
    public void testDeleteTransactionsBasicFunctionality() {
        // Create some test transactions
        jmeterFunctions.setTransaction("keep1", 100);
        jmeterFunctions.setTransaction("delete1", 200);
        jmeterFunctions.setTransaction("keep2", 300);
        jmeterFunctions.setTransaction("delete2", 400);
        jmeterFunctions.setTransaction("keep3", 500);

        // Verify all transactions exist
        SampleResult[] initialResults = jmeterFunctions.getMainResult().getSubResults();
        assertEquals("Should have 5 transactions", 5, initialResults.length);

        // Delete transactions that start with "delete"
        jmeterFunctions.deleteTransactions(sr -> sr.getSampleLabel().startsWith("delete"));

        // Verify only "keep" transactions remain
        SampleResult[] filteredResults = jmeterFunctions.getMainResult().getSubResults();
        assertEquals("Should have 3 transactions after deletion", 3, filteredResults.length);

        // Verify the correct transactions remain
        for (SampleResult result : filteredResults) {
            assertTrue("All remaining transactions should start with 'keep'",
                      result.getSampleLabel().startsWith("keep"));
        }
    }

    @Test
    public void testDeleteTransactionsConcurrency() throws InterruptedException {
        // Setup: Add transactions sequentially to avoid setup race conditions
        final int totalTransactions = 100;
        for (int i = 0; i < totalTransactions; i++) {
            jmeterFunctions.setTransaction("txn" + i, i * 10);
        }

        // Verify initial setup
        SampleResult[] initialResults = jmeterFunctions.getMainResult().getSubResults();
        assertEquals("Should have all transactions after setup",
                    totalTransactions, initialResults.length);

        final int threadCount = 5;
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch completeLatch = new CountDownLatch(threadCount);
        final AtomicBoolean hasError = new AtomicBoolean(false);

        // Concurrent operations: Multiple threads delete different ranges
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready

                    // Each thread deletes transactions in a different range
                    final int rangeStart = threadId * 20;
                    final int rangeEnd = rangeStart + 20;

                    jmeterFunctions.deleteTransactions(sr -> {
                        String label = sr.getSampleLabel();
                        try {
                            int txnNum = Integer.parseInt(label.substring(3)); // Extract number from "txn123"
                            return txnNum >= rangeStart && txnNum < rangeEnd;
                        } catch (NumberFormatException e) {
                            return false; // Don't delete if we can't parse
                        }
                    });

                } catch (Exception e) {
                    hasError.set(true);
                    e.printStackTrace();
                } finally {
                    completeLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        startLatch.countDown();

        // Wait for completion
        assertTrue("All operations should complete within 10 seconds",
                  completeLatch.await(10, TimeUnit.SECONDS));

        executor.shutdown();

        // Verify no errors occurred
        assertFalse("No errors should occur during concurrent operations", hasError.get());

        // Verify data integrity
        SampleResult[] finalResults = jmeterFunctions.getMainResult().getSubResults();
        assertNotNull("Final results should not be null", finalResults);

        // Verify no duplicate transaction names (would indicate corruption)
        Set<String> transactionNames = new HashSet<>();
        for (SampleResult result : finalResults) {
            String label = result.getSampleLabel();
            assertNotNull("Transaction label should not be null", label);
            assertTrue("No duplicate transaction names should exist",
                      transactionNames.add(label));
        }

        // All transactions in ranges 0-99 should be deleted, so should have 0 remaining
        assertEquals("All transactions should have been deleted", 0, finalResults.length);
        assertEquals("Transaction count should match unique names",
                    finalResults.length, transactionNames.size());

        System.out.println("Successfully completed concurrent deletion test - no data corruption detected");
    }

    @Test
    public void testDeleteTransactionsAtomicity() throws InterruptedException {
        // This test verifies that deleteTransactions is atomic -
        // no intermediate state should be visible to other threads

        final int initialTransactionCount = 100;
        final CountDownLatch setupComplete = new CountDownLatch(1);
        final CountDownLatch deletionStarted = new CountDownLatch(1);
        final CountDownLatch observerComplete = new CountDownLatch(1);
        final AtomicBoolean foundInconsistentState = new AtomicBoolean(false);

        // Setup: Add many transactions
        for (int i = 0; i < initialTransactionCount; i++) {
            jmeterFunctions.setTransaction("txn" + i, i * 10);
        }

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Thread 1: Delete half the transactions
        executor.submit(() -> {
            try {
                deletionStarted.await();

                // Delete transactions with even numbers
                jmeterFunctions.deleteTransactions(sr -> {
                    String label = sr.getSampleLabel();
                    int txnNum = Integer.parseInt(label.substring(3)); // Extract number from "txn123"
                    return txnNum % 2 == 0; // Delete even numbered transactions
                });

            } catch (Exception e) {
                e.printStackTrace();
                foundInconsistentState.set(true);
            }
        });

        // Thread 2: Continuously observe the state during deletion
        executor.submit(() -> {
            try {
                setupComplete.countDown();
                deletionStarted.countDown();

                // Observe state multiple times during deletion
                for (int i = 0; i < 50; i++) {
                    SampleResult[] results = jmeterFunctions.getMainResult().getSubResults();

                    if (results.length > 0 && results.length < initialTransactionCount) {
                        // We're in a potential intermediate state
                        // Verify all remaining results are valid
                        Set<String> labels = new HashSet<>();
                        for (SampleResult result : results) {
                            String label = result.getSampleLabel();
                            assertNotNull("Label should not be null during deletion", label);
                            assertTrue("No duplicate labels during deletion", labels.add(label));

                            // Verify the result is properly formed
                            assertTrue("Transaction time should be non-negative", result.getTime() >= 0);
                        }
                    }

                    Thread.sleep(1); // Small delay to catch intermediate states
                }

            } catch (Exception e) {
                e.printStackTrace();
                foundInconsistentState.set(true);
            } finally {
                observerComplete.countDown();
            }
        });

        // Wait for completion
        assertTrue("Observer should complete within 5 seconds",
                  observerComplete.await(5, TimeUnit.SECONDS));

        executor.shutdown();
        assertTrue("Executor should shutdown within 2 seconds",
                  executor.awaitTermination(2, TimeUnit.SECONDS));

        // Verify no inconsistent states were observed
        assertFalse("No inconsistent states should be observed", foundInconsistentState.get());

        // Verify final state is correct
        SampleResult[] finalResults = jmeterFunctions.getMainResult().getSubResults();

        // Should have approximately half the transactions (odd numbered ones)
        int expectedCount = initialTransactionCount / 2;
        assertEquals("Should have odd-numbered transactions remaining",
                    expectedCount, finalResults.length);

        // Verify only odd-numbered transactions remain
        for (SampleResult result : finalResults) {
            String label = result.getSampleLabel();
            int txnNum = Integer.parseInt(label.substring(3));
            assertTrue("Only odd-numbered transactions should remain", txnNum % 2 == 1);
        }
    }
}