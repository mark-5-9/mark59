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

package com.mark59.test.core;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.apache.jmeter.samplers.SampleResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.mark59.core.JmeterFunctionsImpl;
import com.mark59.core.Outcome;

/**
* @author Michael Cohen
* Written: Australian Winter 2019 
*/
public class JmeterFunctionsTest {
	
	@Before
	public final void log4jXmlConfig() {
		//not required, just stops the 'no log4j config' ERROR getting printed
		System.setProperty("log4j.configurationFile", "./log4j2.xml");
	}

	@SuppressWarnings("deprecation")
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public final void TearDown_OnlyMainResultWhichIsIncomplete_MainResultCompletedInSuccessState() {
		JmeterFunctionsImpl t = getJmeterFunctions();
		t.tearDown();
		assert (t.getMainResult().isSuccessful());
	}
	
	@Test
	public final void CheckReturnedSampleResultFromEndTransaction() {
		JmeterFunctionsImpl t = getJmeterFunctions();
		t.startTransaction("simpleTransaction01");
		t.startTransaction("simpleTransaction03");	
		t.endTransaction("simpleTransaction01");
		t.startTransaction("simpleTransaction02");
		t.endTransaction("simpleTransaction02");		
		t.endTransaction("simpleTransaction03");
		t.startTransaction("simpleTransaction04Fail");
		t.endTransaction("simpleTransaction04Fail", Outcome.FAIL ); 
		t.startTransaction("simpleTransaction05Pass");
		t.endTransaction("simpleTransaction05Pass", Outcome.PASS ); 
		t.startTransaction("simpleTransaction06Fail");
		t.endTransaction("simpleTransaction06Fail", Outcome.FAIL, "oops"); 
		
		t.tearDown();
		SampleResult mainResult =  t.getMainResult();
		
		SampleResult[] subrsArray = mainResult.getSubResults();
		for (SampleResult subrs : subrsArray) {
			if ("simpleTransaction01".equals(subrs.getSampleLabel())) {
				assert "PASS".equals(subrs.getResponseMessage());
			} else if ("simpleTransaction02".equals(subrs.getSampleLabel())) {
				assert "PASS".equals(subrs.getResponseMessage());
			} else if ("simpleTransaction02".equals(subrs.getSampleLabel())) {
				assert "PASS".equals(subrs.getResponseMessage());
			} else if ("simpleTransaction03".equals(subrs.getSampleLabel())) {
				assert "PASS".equals(subrs.getResponseMessage());
			} else if ("simpleTransaction04Fail".equals(subrs.getSampleLabel())) {
				assert "FAIL".equals(subrs.getResponseMessage());
			} else if ("simpleTransaction05Pass".equals(subrs.getSampleLabel())) {
				assert "PASS".equals(subrs.getResponseMessage());
			} else if ("simpleTransaction06Fail".equals(subrs.getSampleLabel())) {
				assert "FAIL".equals(subrs.getResponseMessage());
				assert "oops".equals(subrs.getResponseCode());
			} else {
				fail("Unexpected transaction " + subrs.getSampleLabel());
			}
		}
		assert subrsArray.length == 6;
		assert (!t.getMainResult().isSuccessful());
	}

	@Test
	public final void CheckReturnedSampleResultFromSetTransaction() {
		JmeterFunctionsImpl t = getJmeterFunctions();
		t.setTransaction("simpleTransaction01", 666);	
		t.startTransaction("simpleTransaction04NormalTxn");
		t.setTransaction("simpleTransaction02fail", 177, false);	
		t.setTransaction("simpleTransaction03pass", 888, true);	
		t.endTransaction("simpleTransaction04NormalTxn");	
		t.setTransaction("simpleTransaction05fail", 0, false, "badCode");
		
		t.tearDown();
		SampleResult mainResult =  t.getMainResult();
		
		SampleResult[] subrsArray = mainResult.getSubResults();
		for (SampleResult subrs : subrsArray) {
			if ("simpleTransaction01".equals(subrs.getSampleLabel())) {
				assert "PASS".equals(subrs.getResponseMessage());
				assert 666 == subrs.getTime();
			} else if ("simpleTransaction02fail".equals(subrs.getSampleLabel())) {
				assert "FAIL".equals(subrs.getResponseMessage());
				assert 177 == subrs.getTime();
			} else if ("simpleTransaction03pass".equals(subrs.getSampleLabel())) {
				assert "PASS".equals(subrs.getResponseMessage());
				assert 888 == subrs.getTime();
			} else if ("simpleTransaction04NormalTxn".equals(subrs.getSampleLabel())) {
				assert "PASS".equals(subrs.getResponseMessage());
			} else if ("simpleTransaction05fail".equals(subrs.getSampleLabel())) {
				assert "FAIL".equals(subrs.getResponseMessage());
				assert 0 == subrs.getTime();
				assert "badCode".equals(subrs.getResponseCode());
			} else {
				fail("Unexpected transaction " + subrs.getSampleLabel());
			}
		}
		assert subrsArray.length == 5;
		assert (!t.getMainResult().isSuccessful());
	}
	
		
	@Test
	public final void TearDown_OnlyMainResultWhichIsIncompleteButIsForcedToFail_MainResultCompletedInFailState() {
		JmeterFunctionsImpl t = getJmeterFunctions();
		t.failTest();
		t.tearDown();
		assert (!t.getMainResult().isSuccessful());
	}

	@Test
	public final void TearDown_WithOneSubSampleThatIsComplete_MainResultCompletedInSuccessState() {
		JmeterFunctionsImpl t = getJmeterFunctions();
		t.startTransaction("transaction_will_end");
		t.endTransaction("transaction_will_end");
		t.tearDown();
		assert (t.getMainResult().isSuccessful());

	}

	@Test
	public final void TearDown_WithOneSubSampleThatIsNotComplete_MainResultCompletedInFailState() {
		JmeterFunctionsImpl t = getJmeterFunctions();
		t.startTransaction("transaction_wont_end");
		t.tearDown();
		assert (!t.getMainResult().isSuccessful());

	}

	@Test
	public final void TearDown_WithOneSubSampleThatIsCompleteButIsFocedToFail_MainResultCompletedInFailState() {
		JmeterFunctionsImpl t = getJmeterFunctions();
		t.startTransaction("transaction_will_end");
		t.endTransaction("transaction_will_end");
		t.failTest();
		t.tearDown();
		assert (!t.getMainResult().isSuccessful()
				&& Arrays.stream(t.getMainResult().getSubResults())
						.allMatch(SampleResult::isSuccessful));

	}

	@Test
	public final void StartTransaction_BlankTransactionLabel_ThrowIllegalArgumentException() {
		JmeterFunctionsImpl t = getJmeterFunctions();

		String transactionLabel = "";

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("transactionLabel cannot be null or empty");
		t.startTransaction(transactionLabel);
	}

	@Test
	public final void StartTransaction_NULLTransactionLabel_ThrowIllegalArgumentException() {
		JmeterFunctionsImpl t = getJmeterFunctions();

		String transactionLabel = null;

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("transactionLabel cannot be null or empty");
		t.startTransaction(transactionLabel);
	}

	@Test
	public final void StartTransaction_StartTransactionWithSameTransactionLabelAsAnExistingTransaction_ThrowIllegalArgumentException() {
		JmeterFunctionsImpl t = getJmeterFunctions();

		String transactionLabel = "iggy";
		t.startTransaction(transactionLabel);

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(
				"Error -  a transaction using the passed transaction name appears to be currently in use (running) in this script : "
						+ transactionLabel);

		t.startTransaction(transactionLabel);
	}

	@Test
	public final void StartTransaction_StartJustOneTransaction_TransactionIsAddedWithNoEndTime() {
		JmeterFunctionsImpl t = getJmeterFunctions();

		String transactionLabel = "iggy";

		assert (t.getSampleResultWithLabel(transactionLabel) == null);

		t.startTransaction(transactionLabel);

		assert (t.getSampleResultWithLabel(transactionLabel).getEndTime() <= 0);

	}

	@Test
	public final void StartTransaction_StartTwoTransaction_TransactionsAreAddedWithNoEndTime() {
		JmeterFunctionsImpl t = getJmeterFunctions();

		String transactionLabel1 = "iggy";
		String transactionLabel2 = "boo";

		assert (t.getSampleResultWithLabel(transactionLabel1) == null
				&& t.getSampleResultWithLabel(transactionLabel2) == null);

		t.startTransaction(transactionLabel1);
		t.startTransaction(transactionLabel2);

		assert (t.getSampleResultWithLabel(transactionLabel1).getEndTime() <= 0
				&& t.getSampleResultWithLabel(transactionLabel2).getEndTime() <= 0);

	}

	@Test
	public final void EndTransactionString_EndTransactionWithBlankLabel_ThrowIllegalArgumentException() {
		JmeterFunctionsImpl t = getJmeterFunctions();

		String transactionLabel = null;

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("transactionLabel cannot be null or empty");

		t.endTransaction(transactionLabel);
	}

	@Test
	public final void EndTransactionString_EndNonExistantTransaction_ThrowIllegalArgumentException() {
		JmeterFunctionsImpl t = getJmeterFunctions();

		String transactionLabel = "iggy";

		thrown.expect(NoSuchElementException.class);
		thrown.expectMessage(
				"Could not find a transactionn to end matching the passed label : "	+ transactionLabel);

		t.endTransaction(transactionLabel);
	}
	
	@Test
	public final void EndTransactionString_EndExistantTransaction_TransactionAddedToMainResultSubResultInSuccessState() {
		JmeterFunctionsImpl t = getJmeterFunctions();

		String transactionLabel = "iggy";

		t.startTransaction(transactionLabel);
		t.endTransaction(transactionLabel);
		
		assert(t.getSampleResultFromMainResultWithLabel(transactionLabel).stream().findFirst().get().isSuccessful());
	}

	@Test
	public final void EndTransactionStringOutcome_EndExistantTransactionWithForcedFailOutcome_TransactionAddedToMainResultSubResultInFailState() {
		JmeterFunctionsImpl t = getJmeterFunctions();

		String transactionLabel = "iggy";

		t.startTransaction(transactionLabel);
		t.endTransaction(transactionLabel, Outcome.FAIL);
		
		assert(!t.getSampleResultFromMainResultWithLabel(transactionLabel).stream().findFirst().get().isSuccessful());
	}

	@Test
	public final void SetTransactionStringLong_AddFixedValueTransactionWithNULLLabel_ThrowIllegalArgumentException() {
		JmeterFunctionsImpl t = getJmeterFunctions();
		
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("dataPointName cannot be null or empty");
		
		t.setTransaction(null, 0);
		
	}
	
	@Test
	public final void SetTransactionStringLong_AddFixedValueTransactionWithEmptyLabel_ThrowIllegalArgumentException() {
		JmeterFunctionsImpl t = getJmeterFunctions();
		
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("dataPointName cannot be null or empty");
		
		t.setTransaction("", 0);
		
	}
	
	@Test
	public final void SetTransactionStringLong_AddFixedValueTransactionWithSuccessOutcome_TransactionAddedToMainResultInSuccessState() {
		JmeterFunctionsImpl t = getJmeterFunctions();
		
		String transactionLabel = "iggy";
		assert(t.getSampleResultFromMainResultWithLabel(transactionLabel).size() <= 0);
		t.setTransaction(transactionLabel, 0);
		assert (t.getSampleResultFromMainResultWithLabel(transactionLabel).size() == 1
				&& t.getSampleResultFromMainResultWithLabel(transactionLabel).get(0).isSuccessful());
	}

	@Test
	public final void SetTransactionStringLongBoolean_AddFixedValueTransactionWithNULLLabel_ThrowIllegalArgumentException() {
		JmeterFunctionsImpl t = getJmeterFunctions();
		
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("dataPointName cannot be null or empty");
		
		t.setTransaction(null, 0, true);
		
	}
	
	@Test
	public final void SetTransactionStringLongBoolean_AddFixedValueTransactionWithEmptyLabel_ThrowIllegalArgumentException() {
		JmeterFunctionsImpl t = getJmeterFunctions();
		
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("dataPointName cannot be null or empty");
		
		t.setTransaction("", 0, true);
		
	}
	
	@Test
	public final void SetTransactionStringLongBoolean_AddFixedValueTransactionWithSuccessOutcome_TransactionAddedToMainResultInSuccessState() {
		JmeterFunctionsImpl t = getJmeterFunctions();
		
		String transactionLabel = "iggy";
		assert(t.getSampleResultFromMainResultWithLabel(transactionLabel).size() <= 0);
		t.setTransaction(transactionLabel, 0, true);
		assert (t.getSampleResultFromMainResultWithLabel(transactionLabel).size() == 1
				&& t.getSampleResultFromMainResultWithLabel(transactionLabel).get(0).isSuccessful());
	}
	
	@Test
	public final void SetTransactionStringLongBoolean_AddFixedValueTransactionWithFailOutcome_TransactionAddedToMainResultInFailState() {
		JmeterFunctionsImpl t = getJmeterFunctions();
		
		String transactionLabel = "iggy";
		assert(t.getSampleResultFromMainResultWithLabel(transactionLabel).size() <= 0);
		t.setTransaction(transactionLabel, 0, false);
		assert (t.getSampleResultFromMainResultWithLabel(transactionLabel).size() == 1
				&& !t.getSampleResultFromMainResultWithLabel(transactionLabel).get(0).isSuccessful());
	}

	@Test
	public final void UserDataPoint_AddDataPointWithNULLName_ThrowIllegalArgumentException() {
		JmeterFunctionsImpl t = getJmeterFunctions();
		
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("dataPointName cannot be null or empty");
		
		t.userDataPoint(null, 0);
	}
	
	@Test
	public final void UserDataPoint_AddDataPointWithEmptyName_ThrowIllegalArgumentException() {
		JmeterFunctionsImpl t = getJmeterFunctions();
		
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("dataPointName cannot be null or empty");
		
		t.userDataPoint("", 0);
	}
	
	@Test
	public final void UserDataPoint_AddDataPoint_TransactionAddedToMainResultInSuccessState() {
		JmeterFunctionsImpl t = getJmeterFunctions();
		
		String transactionLabel = "iggy";
		assert(t.getSampleResultFromMainResultWithLabel(transactionLabel).size() <= 0);
		t.userDataPoint(transactionLabel, 0);
		assert (t.getSampleResultFromMainResultWithLabel(transactionLabel).size() == 1
				&& t.getSampleResultFromMainResultWithLabel(transactionLabel).get(0).isSuccessful());
	}

	private JmeterFunctionsImpl getJmeterFunctions() {
		return new JmeterFunctionsImpl(null);
	}

}
