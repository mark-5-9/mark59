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

import com.mark59.core.utils.SecureAES;


/**
 * 
 * <p>Use this utility to create encrypt a string or generate an Encryption Key 
 *   
 * <p><b>USAGE</b></p>
 *
 * <p><b>Creating an Encryption Key</b></p>
 * Run SecureAESUtility in mark59-core or SecureAESUtility (this class) that ships in the 
 * mark59-scripting-samples project. The output will include a few lines that contain a value
 * that can be used as the key:
 * 
 * <p><code>Here's a newly generated encryption key you may choose to use :<br>
 * ------------------------------------------------<br>
 * sGLy3YkDaIZfp1BbqjDkvzDow+0FIAKXcozAJ1i3pzg=<br>
 * ------------------------------------------------</code>
 * 
 * <p><b>Setting the Encryption Key</b></p>
 * Note: if you choose not to set an encryption key, a default value will be used 
 * (not recommended for a production environment).<br>  
 * <br>#### Option A: Environment Variable (Recommended)<br>
 * Linux/Mac: bash<br>
 * <code>export MARK59_ENCRYPTION_KEY="your-generated-key-here"</code><br>
 * Windows (PowerShell):<br>
 * <code>$env:MARK59_ENCRYPTION_KEY="your-generated-key-here"</code><br>
 * Windows (CMD):<br>
 * <code>set MARK59_ENCRYPTION_KEY=your-generated-key-here</code><br>
 * <br>#### Option B: System Property<br>
 * Add to JVM startup parameters:
 * <code>-DMARK59_ENCRYPTION_KEY="your-generated-key-here"</code><br>
 * <br>#### Option C: Application Server Configuration<br>
 * For Tomcat, add to `setenv.sh` or `setenv.bat`:<br>
 * <code>JAVA_OPTS="$JAVA_OPTS -DMARK59_ENCRYPTION_KEY=your-generated-key-here"</code>
 *
 * <p><b>Creating an Encrypted String</b></p>
 * #### Option A: Programmatically<br>
 * Run SecureAESUtility in mark59-core (this class) or SecureAESUtility that ships in the 
 * mark59-scripting-samples project, setting <code>originalString</code>to the value being encoded. 
 * Ensure you have set the encryption key to the same value as the target environment(s).  
 * The output contains the encrypted value :<br><br>
 * <code>MyTestPasswordStr1ng<br>
 * LLPjn6afqUe4xL6snAYhA05pJHtmZ+qTyD7uW+LT1tkLrbAh3Qt06Z1IsiYS3lwk1uTucrmK/BJsR+FsO2IEgw==<br>
 * MyTestPasswordStr1ng<br></code><br>
 * 
 * #### Option B: Using Server Metrics API<br>
 * If you have an implementation of the Mark59 Metrics application running, you can use the 
 * an api request to obtain an encrypted value in the response.  Just check the server running the 
 * Metrics application is using the intended encryption key.  Here's the format for the 
 * shipped mark59-metrics application running locally :<br>
 * <br><code>http://localhost:8085/mark59-metrics/api/cipher?pwd=stringtoencrypt</code><br>
 * 
 * <p><b>Encrypted String Usage within Mark59</b></p>
 * The follow list are places where options to encrypt/decrypt are implemented<br>
 * <ul>
 *   <li>TrendsLoad: encrypted MySql or Postgres database password, cmd line 'y' option</li>
 *   <li>Metrics: 'Create Cipher' option when creating/editing a NIX or WMIC remote 
 *   server connection</li>
 *   <li>Metrics API: /api/pwd=stringtoencrypt as described above</li>
 * </ul> 
 * 
 * <p><b>Migration from SimpleAES</b></p>
 * Versions of Mark59 prior to 6.5 used a weak encryption class SimpleAES. Encryptions created by 
 * this class will NOT work with SecureAES. Use can use the SimpleAES class in mark59-core or 
 * SimpleAESUtility that ships in the mark59-scripting-samples project if you need to recover the original string
 * for re-encryption.<br><br> 
 * 

 *
 * @author Philip Webb
 * Written: Australian Summer 2025
 */
public class SecureAESUtility {

	/**
	 * Main method for encrypt/decrypt a string, and printing out a new encryption key 
	 */
	public static void main(String[] args) {
		String originalString = "My test string!";
		String encryptedString = SecureAES.encrypt(originalString);
		String decryptedString = SecureAES.decrypt(encryptedString);

		System.out.println(originalString);
		System.out.println(encryptedString);
		System.out.println(decryptedString);
		
		System.out.println();
		System.out.println();
		String newKey = SecureAES.generateSecureKey();
		System.out.println("Here's a newly generated encryption key"
				+ " you may choose to use :");
		System.out.println("------------------------------------------------");
		System.out.println(newKey);
		System.out.println("------------------------------------------------");		
		System.out.println();
	}

}