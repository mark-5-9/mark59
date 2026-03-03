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

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for JmeterFunctionsImpl tearDown error handling improvements.
 *
 * @author GitHub Copilot
 * Written: October 2025
 */
public class JmeterFunctionsImplTearDownTest {

    private JmeterFunctionsImpl jmeterFunctions;
    private JavaSamplerContext mockContext;

    @Before
    public void setUp() {
        mockContext = mock(JavaSamplerContext.class);
        jmeterFunctions = new JmeterFunctionsImpl(mockContext, false); // No mark59 properties needed
    }

    @Test
    public void testTearDownNormalOperation() {
        // Add some transactions
        jmeterFunctions.setTransaction("normal_txn_1", 100);
        jmeterFunctions.setTransaction("normal_txn_2", 200);

        // Verify initial state
        SampleResult[] initialResults = jmeterFunctions.getMainResult().getSubResults();
        assertEquals("Should have 2 transactions", 2, initialResults.length);

        // Perform tearDown - should complete without exceptions
        jmeterFunctions.tearDown();

        // Verify main result is finalized
        SampleResult mainResult = jmeterFunctions.getMainResult();
        assertNotNull("Main result should not be null", mainResult);
        assertTrue("Main result should be successful", mainResult.isSuccessful());
        assertTrue("Main result should have end time", mainResult.getEndTime() > 0);
        assertEquals("Response message should be PASS", "PASS", mainResult.getResponseMessage());
    }

    @Test
    public void testTearDownWithInFlightTransactions() {
        // Start transactions but don't end them (simulating in-flight transactions)
        jmeterFunctions.startTransaction("in_flight_1");
        jmeterFunctions.startTransaction("in_flight_2");

        // Add a completed transaction too
        jmeterFunctions.setTransaction("completed_txn", 150);

        // Verify we have one completed transaction and two in-flight
        SampleResult[] initialResults = jmeterFunctions.getMainResult().getSubResults();
        assertEquals("Should have 1 completed transaction initially", 1, initialResults.length);

        // Perform tearDown - should handle in-flight transactions gracefully
        jmeterFunctions.tearDown();

        // Verify all transactions are now completed (in-flight ones marked as failed)
        SampleResult[] finalResults = jmeterFunctions.getMainResult().getSubResults();
        assertEquals("Should have 3 transactions after tearDown", 3, finalResults.length);

        // Main result should be marked as failed due to failed in-flight transactions
        SampleResult mainResult = jmeterFunctions.getMainResult();
        assertFalse("Main result should be failed due to in-flight transactions", mainResult.isSuccessful());
        assertEquals("Response message should be FAIL", "FAIL", mainResult.getResponseMessage());
    }

    @Test
    public void testTearDownWithForcedFailure() {
        // Add successful transactions
        jmeterFunctions.setTransaction("success_txn_1", 100);
        jmeterFunctions.setTransaction("success_txn_2", 200);

        // Force test failure
        jmeterFunctions.failTest();

        // Perform tearDown
        jmeterFunctions.tearDown();

        // Verify main result is marked as failed despite successful transactions
        SampleResult mainResult = jmeterFunctions.getMainResult();
        assertFalse("Main result should be failed due to forced failure", mainResult.isSuccessful());
        assertEquals("Response message should be FAIL", "FAIL", mainResult.getResponseMessage());
    }

    @Test
    public void testTearDownWithResultSummaryLogging() {
        // Enable logging flags
        jmeterFunctions.logResultSummary(true);
        jmeterFunctions.printResultSummary(true);

        // Add some transactions
        jmeterFunctions.setTransaction("logged_txn_1", 100);
        jmeterFunctions.setTransaction("logged_txn_2", 200);

        // Perform tearDown - should complete even with logging enabled
        jmeterFunctions.tearDown();

        // Verify main result is still finalized correctly
        SampleResult mainResult = jmeterFunctions.getMainResult();
        assertNotNull("Main result should not be null", mainResult);
        assertTrue("Main result should be successful", mainResult.isSuccessful());
    }

    @Test
    public void testTearDownRobustnessWithMultipleOperations() {
        // Create a complex scenario with various transaction types
        jmeterFunctions.startTransaction("in_flight_txn");
        jmeterFunctions.setTransaction("success_txn", 100);
        jmeterFunctions.setTransaction("failed_txn", 200, false);

        // Enable logging
        jmeterFunctions.logResultSummary(true);
        jmeterFunctions.printResultSummary(true);

        // Perform tearDown
        jmeterFunctions.tearDown();

        // Verify tearDown completed and finalized results
        SampleResult mainResult = jmeterFunctions.getMainResult();
        assertNotNull("Main result should not be null", mainResult);

        // Should have 3 transactions (in-flight converted to failed)
        SampleResult[] finalResults = mainResult.getSubResults();
        assertEquals("Should have 3 transactions", 3, finalResults.length);

        // Main result should be failed due to failed transactions
        assertFalse("Main result should be failed", mainResult.isSuccessful());
        assertTrue("Main result should have end time", mainResult.getEndTime() > 0);
    }

    @Test
    public void testTearDownIdempotency() {
        // Add transactions
        jmeterFunctions.setTransaction("idempotent_txn", 100);

        // Call tearDown multiple times
        jmeterFunctions.tearDown();
        SampleResult firstTearDown = jmeterFunctions.getMainResult();

        jmeterFunctions.tearDown();
        SampleResult secondTearDown = jmeterFunctions.getMainResult();

        jmeterFunctions.tearDown();
        SampleResult thirdTearDown = jmeterFunctions.getMainResult();

        // Verify multiple calls don't break the state
        assertSame("Multiple tearDown calls should not create new main results",
                  firstTearDown, secondTearDown);
        assertSame("Multiple tearDown calls should not create new main results",
                  secondTearDown, thirdTearDown);

        // State should remain consistent
        assertTrue("Main result should remain successful", thirdTearDown.isSuccessful());
    }

    @Test
    public void testTearDownPreservesTransactionData() {
        // Add transactions with specific data
        long txn1Time = 123;
        long txn2Time = 456;
        jmeterFunctions.setTransaction("preserve_txn_1", txn1Time);
        jmeterFunctions.setTransaction("preserve_txn_2", txn2Time);

        // Perform tearDown
        jmeterFunctions.tearDown();

        // Verify transaction data is preserved
        SampleResult[] results = jmeterFunctions.getMainResult().getSubResults();
        assertEquals("Should have 2 transactions", 2, results.length);

        // Find and verify each transaction
        boolean found1 = false, found2 = false;
        for (SampleResult result : results) {
            if ("preserve_txn_1".equals(result.getSampleLabel())) {
                assertEquals("Transaction 1 time should be preserved", txn1Time, result.getTime());
                found1 = true;
            } else if ("preserve_txn_2".equals(result.getSampleLabel())) {
                assertEquals("Transaction 2 time should be preserved", txn2Time, result.getTime());
                found2 = true;
            }
        }

        assertTrue("Should find transaction 1", found1);
        assertTrue("Should find transaction 2", found2);
    }
}