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

package com.mark59.core;

/**
 * Wrapper class used to encapsulate an arbitrary Driver object, where the
 * Driver object knows how to perform an arbitrary set of functions necessary to
 * execute the test.
 * 
 * @param <T> Concrete Driver to be wrapped
 * @author Michael Cohen
 * Written: Australian Winter 2019
 */
public abstract class DriverWrapper<T> {

	private final String driverClass;
	private final T driverPackage;

	@SuppressWarnings("unused")
	private DriverWrapper() {
		this.driverPackage = null;
		this.driverClass = null;
	}

	/**
	 * Constructor for the DriverWrapper.
	 * 
	 * @param driverPackage Concrete Driver to be wrapped
	 */
	public DriverWrapper(T driverPackage) {
		this.driverPackage = driverPackage;
		this.driverClass = driverPackage.getClass().getName();
	}

	/**
	 * Returns the class name of the encapsulated Driver.
	 * 
	 * @return String
	 */
	public String getDriverClass() {
		return driverClass;
	}

	/**
	 * Returns the concrete arbitrary driver encapsulated by this
	 * 
	 * @return driverPackage.
	 */
	public T getDriverPackage() {
		return driverPackage;
	}

	/**
	 * Handles any needed cleanup once the driver is finished with, if any cleanup is required.
	 */
	public abstract void driverDispose();
	
	/**
	 * Used to return any logs captured by the Driver.
	 * 
	 * @return String
	 */
	public abstract String getDriverLogs();
	
	/**
	 * Clears logs previously captured by the Driver.
	 * <p>
	 * Useful if the driver is capturing more logs than are needed (for instance,
	 * only being interested in logs for the most recent event, in case of a
	 * failure).
	 * </p>
	 */
	public abstract void clearDriverLogs();
	
	/**
	 * Allows the driver to take steps in response to the supplied exceptions.
	 * Specifically intended for logging or similar actions.
	 * 
	 * @param e Exception message
	 */
	public abstract void documentExceptionState(Exception e);
}
