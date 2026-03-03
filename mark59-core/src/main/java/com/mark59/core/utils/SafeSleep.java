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

/**
 * Uninterruptible wrapper for Thread.sleep that ensures the full sleep duration
 * is completed even if the thread is interrupted during the sleep.
 *
 * @author Michael Cohen
 * Written: Australian Winter 2019
 */
public class SafeSleep {

    private SafeSleep() {}

    /**
     * Pause the running thread for a given number of milliseconds.
     * This method ensures the full duration is slept even if interrupted.
     *
     * <p><strong>Important:</strong> This method restores the thread's interrupt
     * status after completing the sleep, allowing calling code to detect that
     * an interruption occurred.
     *
     * @param sleepDuration sleep duration in milliseconds (non-positive values are ignored)
     */
    public static void sleep(long sleepDuration) {

        if (sleepDuration <= 0) {
            return; // No need to sleep
        }

        long remainingTime = sleepDuration;
        long startTime = System.currentTimeMillis();
        boolean wasInterrupted = false;

        while (remainingTime > 0) {
            try {
                Thread.sleep(remainingTime);
                break; // Sleep completed successfully
            } catch (InterruptedException e) {
                wasInterrupted = true;
                long currentTime = System.currentTimeMillis();
                long elapsed = currentTime - startTime;
                remainingTime = sleepDuration - elapsed;
                startTime = currentTime;
            }
        }

        // Restore interrupt status if we were interrupted
        if (wasInterrupted) {
            Thread.currentThread().interrupt();
        }
    }
}