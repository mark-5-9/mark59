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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe utility counter that can track and increment multiple named counters
 * with high concurrency performance and guaranteed uniqueness across threads.
 *
 * <p>This implementation uses lock-free concurrent data structures to ensure
 * thread safety without performance bottlenecks. All operations are atomic
 * and provide strong consistency guarantees.</p>
 *
 * @author Michael Cohen
 * @author Philp Webb (Claude suggestions on concurrency improvements)
 * Written: Australian Winter 2019
 * Updated: October 2025 - Enhanced thread safety and performance
 */
public final class StaticCounter {

	/**
	 * Thread-safe map of counter names to their atomic long values.
	 * Uses ConcurrentHashMap for lock-free concurrent access.
	 */
	private static final ConcurrentHashMap<String, AtomicLong> counterMap =
		new ConcurrentHashMap<>();

	/**
	 * Private constructor to prevent instantiation of utility class.
	 */
	private StaticCounter() {
		throw new AssertionError("Utility class should not be instantiated");
	}

	/**
	 * Get current count for the specified counter without incrementing.
	 *
	 * @param counter counter name (must not be null)
	 * @return current count (0 if counter doesn't exist)
	 * @throws IllegalArgumentException if counter is null
	 */
	public static long readCount(String counter) {
		validateCounter(counter);
		return counterMap.getOrDefault(counter, new AtomicLong(0)).get();
	}

	/**
	 * Atomically increment and return the new value for the specified counter.
	 * This is the recommended method for getting unique sequential values.
	 *
	 * @param counter counter name (must not be null)
	 * @return the incremented value (starting from 1 for new counters)
	 * @throws IllegalArgumentException if counter is null
	 */
	public static long getNext(String counter) {
		validateCounter(counter);
		return counterMap.computeIfAbsent(counter, k -> new AtomicLong(0))
		                 .incrementAndGet();
	}

	/**
	 * Increment the counter without returning the value.
	 * Note: For getting unique values, prefer {@link #getNext(String)} as it's atomic.
	 *
	 * @param counter counter name (must not be null)
	 * @throws IllegalArgumentException if counter is null
	 */
	public static void incrementCount(String counter) {
		validateCounter(counter);
		counterMap.computeIfAbsent(counter, k -> new AtomicLong(0))
		          .incrementAndGet();
	}

	/**
	 * Reset a specific counter to zero.
	 *
	 * @param counter counter name (must not be null)
	 * @throws IllegalArgumentException if counter is null
	 */
	public static void resetCounter(String counter) {
		validateCounter(counter);
		counterMap.computeIfPresent(counter, (k, v) -> new AtomicLong(0));
	}

	/**
	 * Remove a counter entirely.
	 *
	 * @param counter counter name (must not be null)
	 * @return true if counter existed and was removed
	 * @throws IllegalArgumentException if counter is null
	 */
	public static boolean removeCounter(String counter) {
		validateCounter(counter);
		return counterMap.remove(counter) != null;
	}

	/**
	 * Get all current counter names.
	 *
	 * @return unmodifiable set of counter names
	 */
	public static Set<String> getCounterNames() {
		return Collections.unmodifiableSet(counterMap.keySet());
	}

	/**
	 * Clear all counters.
	 */
	public static void clearAll() {
		counterMap.clear();
	}

	/**
	 * Validates that the counter name is not null.
	 *
	 * @param counter counter name to validate
	 * @throws IllegalArgumentException if counter is null
	 */
	private static void validateCounter(String counter) {
		if (counter == null) {
			throw new IllegalArgumentException("Counter name cannot be null");
		}
	}

	/**
	 * Get the number of active counters (for testing/monitoring).
	 *
	 * @return number of counters currently tracked
	 */
	static int getCounterMapSize() {
		return counterMap.size();
	}

}
