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

package com.mark59.test.core;

import java.util.Arrays;
import java.util.NoSuchElementException;

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

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public final void TearDown_OnlyMainResultWhichIsIncomplete_MainResultCompletedInSuccessState() {
		JmeterFunctionsImpl t = getJmeterFunctions();
		t.tearDown();
		assert (t.getMainResult().isSuccessful());
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
				&& Arrays.asList(t.getMainResult().getSubResults()).stream()
						.noneMatch(sr -> !sr.isSuccessful()));

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
				"could not add new SampleResult to transactionMap as it already contains a key matching the supplied value : "
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
				"could not find SampleResult in transactionMap as it does not contain a key matching the expected value : "
						+ transactionLabel);

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
		return new JmeterFunctionsImpl(Thread.currentThread().getName());
	}

}
