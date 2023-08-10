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
package com.mark59.dsl.samples.seleniumDSL.core;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.internal.Require;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.mark59.core.utils.SafeSleep;

/**
 * A custom version of Selenium's {@link FluentWait}.  Instead of using a constant interval between 
 * condition re-tries, a list of polling interval times (in milliseconds) are used, which are iterated
 * through to set intervals between the condition test, until the condition is met, or timeout. 
 * <p>Once the last entry on the list is reached all subsequent intervals will be set to that value.  
 *
 * @author Philip Webb Written: Australian Winter 2023
 *
 * @param <T> The input type for each condition used with this instance.
 */
public class FluentWaitVariablePolling<T> implements Wait<T> {

	protected static final long DEFAULT_SLEEP_TIMEOUT = 500;

	private List<Long> pollingFreqsMs = FluentWaitFactory.DEFAULT_VARIABLE_POLLING;
	private static final Duration DEFAULT_WAIT_DURATION = Duration.ofMillis(DEFAULT_SLEEP_TIMEOUT);

	private final T input;
	private final java.time.Clock clock;

	private Duration timeout = DEFAULT_WAIT_DURATION;

	private Supplier<String> messageSupplier = () -> null;

	private List<Class<? extends Throwable>> ignoredExceptions = new ArrayList<>();

	/**
	 * @param input The input value to pass to the evaluated conditions.
	 */
	public FluentWaitVariablePolling(T input) {
		this(input, Clock.systemDefaultZone(), Arrays.asList(DEFAULT_SLEEP_TIMEOUT));
	}

	/**
	 * @param input   The input value to pass to the evaluated conditions.
	 * @param clock   The clock to use when measuring the timeout.
	 * @param sleeper Used to put the thread to sleep between evaluation loops.
	 */
	public FluentWaitVariablePolling(T input, java.time.Clock clock, List<Long> pollingFreqsMs) {
		this.input = Require.nonNull("Input", input);
		this.clock = Require.nonNull("Clock", clock);
		this.pollingFreqsMs = Require.nonNull("pollingFreqsMs", pollingFreqsMs);
	}

	/**
	 * Sets how long to wait for the evaluated condition to be true. The default
	 * timeout is {@link #DEFAULT_WAIT_DURATION}.
	 *
	 * @param timeout The timeout duration.
	 * @return A self reference.
	 */
	public FluentWaitVariablePolling<T> withTimeout(Duration timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * Sets the message to be displayed when time expires.
	 *
	 * @param message to be appended to default.
	 * @return A self reference.
	 */
	public FluentWaitVariablePolling<T> withMessage(final String message) {
		this.messageSupplier = () -> message;
		return this;
	}

	/**
	 * Sets the message to be evaluated and displayed when time expires.
	 *
	 * @param messageSupplier to be evaluated on failure and appended to default.
	 * @return A self reference.
	 */
	public FluentWaitVariablePolling<T> withMessage(Supplier<String> messageSupplier) {
		this.messageSupplier = messageSupplier;
		return this;
	}

	/**
	 * Sets how often the condition should be evaluated.  Unlike Selenium's {@link FluentWait} class, 
	 * which uses a constant interval, here a list of millisecond intervals are passed.  
	 *
	 * @param pollingFreqsMs the list of polling intervals (in milliseconds).
	 * @return A self reference.
	 */
	public FluentWaitVariablePolling<T> withPollingFrequencies(List<Long> pollingFreqsMs) {
		this.pollingFreqsMs = pollingFreqsMs;
		return this;
	}

	/**
	 * Configures this instance to ignore specific types of exceptions while waiting
	 * for a condition. Any exceptions not whitelisted will be allowed to propagate,
	 * terminating the wait.
	 *
	 * @param types The types of exceptions to ignore.
	 * @param <K>   an Exception that extends Throwable
	 * @return A self reference.
	 */
	public <K extends Throwable> FluentWaitVariablePolling<T> ignoreAll(Collection<Class<? extends K>> types) {
		ignoredExceptions.addAll(types);
		return this;
	}

	/**
	 * @param exceptionType exception to ignore
	 * @return a self reference
	 * @see #ignoreAll(Collection)
	 */
	public FluentWaitVariablePolling<T> ignoring(Class<? extends Throwable> exceptionType) {
		return this.ignoreAll(ImmutableList.<Class<? extends Throwable>>of(exceptionType));
	}

	/**
	 * @param firstType  exception to ignore
	 * @param secondType another exception to ignore
	 * @return a self reference
	 * @see #ignoreAll(Collection)
	 */
	public FluentWaitVariablePolling<T> ignoring(Class<? extends Throwable> firstType,
			Class<? extends Throwable> secondType) {

		return this.ignoreAll(ImmutableList.of(firstType, secondType));
	}

	/**
	 * Repeatedly applies this instance's input value to the given function until
	 * one of the following occurs:
	 *
	 * <ol>
	 * <li>the function returns neither null nor false
	 * <li>the function throws an unignored exception
	 * <li>the timeout expires
	 * <li>the current thread is interrupted
	 * </ol>
	 *
	 * Unlike Selenium's {@link FluentWait} class, which uses a constant interval, 
	 * here a list of millisecond intervals is used.
	 *
	 * @param isTrue the parameter to pass to the {@link ExpectedCondition}
	 * @param <V>    The function's expected return type.
	 * @return The function's return value if the function returned something
	 *         different from null or false before the timeout expired.
	 * @throws TimeoutException If the timeout expires.
	 */
	@Override
	public <V> V until(Function<? super T, V> isTrue) {
//		System.out.println("at FluentWaitVariablePolling until" );
//		System.out.println("Input   : " + input.getClass());
//		System.out.println("Function: " + isTrue.getClass());
		
		Instant end = clock.instant().plus(timeout);
		Throwable lastException;
		int nextFreqIx = 0;

		while (true) {
			try {
				V value = isTrue.apply(input);
				if (value != null && (Boolean.class != value.getClass() || Boolean.TRUE.equals(value))) {
					return value;
				}
				lastException = null;
			} catch (Throwable e) {
				lastException = propagateIfNotIgnored(e);
			}

			if (end.isBefore(clock.instant())) {
				String message = messageSupplier != null ? messageSupplier.get() : null;

				String timeoutMessage = String.format(
						"Expected condition failed: %s (tried for %d second(s) with %d Ms last interval)",
						message == null ? "waiting for " + isTrue : 
							message, timeout.getSeconds(),pollingFreqsMs.get(nextFreqIx));
				throw timeoutException(timeoutMessage, lastException);
			}
			System.out.println(">> nextFreqIx="+nextFreqIx+", Ms="+pollingFreqsMs.get(nextFreqIx));

			SafeSleep.sleep(pollingFreqsMs.get(nextFreqIx));
			
			if (nextFreqIx < (pollingFreqsMs.size() - 1)) {
				nextFreqIx++;
			}
		}
	}

	private Throwable propagateIfNotIgnored(Throwable e) {
		for (Class<? extends Throwable> ignoredException : ignoredExceptions) {
			if (ignoredException.isInstance(e)) {
				return e;
			}
		}
		Throwables.throwIfUnchecked(e);
		throw new RuntimeException(e);
	}

	/**
	 * Throws a timeout exception. This method may be overridden to throw an
	 * exception that is idiomatic for a particular test infrastructure, such as an
	 * AssertionError in JUnit4.
	 *
	 * @param message       The timeout message.
	 * @param lastException The last exception to be thrown and subsequently
	 *                      suppressed while waiting on a function.
	 * @return Nothing will ever be returned; this return type is only specified as
	 *         a convenience.
	 */
	protected RuntimeException timeoutException(String message, Throwable lastException) {
		throw new TimeoutException(message, lastException);
	}
}