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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mark59.core.JmeterFunctionsImpl;


/**
 * A simple encryption program to allow for non clear-case entry of passwords in scripts. <br>
 * It is <b>NOT</b> meant to be used when a high level of security is required.<br>
 * It is <b>NEVER TO BE USED IN A PRODUCTION IMPLEMENTATION</b><br>
 * Reference: https://howtodoinjava.com/security/java-aes-encryption-example
 *
 *
 * <p><b>This class does not handle modern cryptography standards.</b> As of version 5.6, 
 * the preferred way to handle this is via the new {@link SecureAES} class.
 * 
 * <p>The only use case would be to 'decrypt' an existing string when you have lost 
 * its actual value.
 * 
 * <p>This class <b>will be removed in the next release</b>, as it may become problematic for 
 * automated security scans in future. 
 * @deprecated
 *
 *
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
@Deprecated(since = "5.6", forRemoval = true)
public class SimpleAES {

	private static final Logger LOG = LogManager.getLogger(JmeterFunctionsImpl.class);

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 * This class contains only static methods and should not be instantiated.
	 */
	private SimpleAES() {
		// Utility class - no instances should be created
	}

	/**
	 * <b>DO NOT USE</b> when a anything other than the use of non clear case password
	 * level of security is required.<br>
	 * It is <b>NEVER TO BE USED IN A PRODUCTION IMPLEMENTATION</b><br>
	 * @param strToEncrypt string to encrypt
	 * @return encrypted string
	 */
	public static synchronized String encrypt(String strToEncrypt) {
		try {
			// set key
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			byte[] key = sha.digest(Mark59Constants.REFERENCE.getBytes(StandardCharsets.UTF_8));
			SecretKeySpec secretKey = new SecretKeySpec(Arrays.copyOf(key, 16), "AES");
			// encrypt
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception e) {
			LOG.error("Error while encrypting: " + e.toString());
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * return decrypted string
	 * @param strToDecrypt  string to decrypt
	 * @return string
	 * @throws RuntimeException if decryption fails
	 */
	public static synchronized String decrypt(String strToDecrypt) {
		try {
			// set key
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			byte[] key = sha.digest(Mark59Constants.REFERENCE.getBytes(StandardCharsets.UTF_8));
			SecretKeySpec secretKey = new SecretKeySpec(Arrays.copyOf(key, 16), "AES");
			// decrypt
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
		} catch (Exception e) {
			LOG.error("Error while decrypting: " + e.toString());
			e.printStackTrace();
			// throw exception back to calling application
			throw new RuntimeException("failure decrypting '" + strToDecrypt + "'");
		}
	}


	/**
	 * Main method for encrypt/decrypt a string
	 * @param args  none required
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