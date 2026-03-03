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
package com.mark59.sample.security.utilities;

import com.mark59.core.utils.SimpleAES;


/**
 * ----------------------------------------------------------------------------------------
 * The class this Utility invokes, SimpleEAE is NOT LONGER IN USE. 
 * ALL REFERENCES HAVE BEEN CHANGED TO USE SecureAES
 * 
 * <p>The only use case would be to 'decrypt' an existing string when you have lost 
 * its actual value.  
 *   
 * <p>Please refer to the JavaDoc in SecureAES or SecureAESUtility for details 
 * ----------------------------------------------------------------------------------------
 * 
 * <p>The only use case if that you may need to 'decrypt' an existing password if you 
 * have lost its actual value.   
 *   
 * @author Philip Webb
 * Written: Australian Summer 2025
 */
public class SimpleAESUtility {

	/**
	 * Main method for encrypt/decrypt a string
	 */
	public static void main(String[] args) {
		String originalString = "My test string!";
		String encryptedString = SimpleAES.encrypt(originalString);
		String decryptedString = SimpleAES.decrypt(encryptedString);

		System.out.println(originalString);
		System.out.println(encryptedString);
		System.out.println(decryptedString);
	}

}